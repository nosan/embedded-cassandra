/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.github.nosan.embedded.cassandra.commons.FileUtils;
import com.github.nosan.embedded.cassandra.commons.StreamUtils;
import com.github.nosan.embedded.cassandra.commons.UserHomeDirectorySupplier;
import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;
import com.github.nosan.embedded.cassandra.commons.httpclient.HttpClient;
import com.github.nosan.embedded.cassandra.commons.httpclient.HttpRequest;
import com.github.nosan.embedded.cassandra.commons.httpclient.HttpResponse;
import com.github.nosan.embedded.cassandra.commons.httpclient.JdkHttpClient;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;

/**
 * The implementation of {@link CassandraDirectoryProvider} which downloads and extracts Cassandra archive from the
 * well-known URLs into the provided directory.
 * <p>
 * If the directory already exists then it will be immediately used skipping downloading and extracting steps.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class WebCassandraDirectoryProvider implements CassandraDirectoryProvider {

	private static final Logger LOGGER = Logger.get(WebCassandraDirectoryProvider.class);

	private final HttpClient httpClient;

	private final IOSupplier<? extends Path> downloadDirectorySupplier;

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with {@link JdkHttpClient} and {@link
	 * UserHomeDirectorySupplier}.
	 */
	public WebCassandraDirectoryProvider() {
		this(new JdkHttpClient(), new UserHomeDirectorySupplier());
	}

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with provided {@link HttpClient} and {@link
	 * UserHomeDirectorySupplier}.
	 *
	 * @param httpClient http client to use
	 */
	public WebCassandraDirectoryProvider(HttpClient httpClient) {
		this(httpClient, new UserHomeDirectorySupplier());
	}

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with {@link JdkHttpClient} and provided download directory
	 * supplier.
	 *
	 * @param downloadDirectorySupplier the download directory supplier
	 */
	public WebCassandraDirectoryProvider(IOSupplier<? extends Path> downloadDirectorySupplier) {
		this(new JdkHttpClient(), downloadDirectorySupplier);
	}

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with provided {@link HttpClient} and download directory
	 * supplier.
	 *
	 * @param httpClient http client to use
	 * @param downloadDirectorySupplier the download directory supplier
	 */
	public WebCassandraDirectoryProvider(HttpClient httpClient, IOSupplier<? extends Path> downloadDirectorySupplier) {
		Objects.requireNonNull(httpClient, "HTTP Client must not be null");
		Objects.requireNonNull(downloadDirectorySupplier, "Download Directory Supplier must not be null");
		this.httpClient = httpClient;
		this.downloadDirectorySupplier = downloadDirectorySupplier;
	}

	@Override
	public Path getDirectory(Version version) throws IOException {
		HttpClient httpClient = this.httpClient;
		Path downloadDir = this.downloadDirectorySupplier.get()
				.resolve(".embedded-cassandra").resolve("cassandra").resolve(version.toString());
		Path cassandraDirectory = downloadDir.resolve(String.format("apache-cassandra-%s", version));
		if (!Files.exists(cassandraDirectory)) {
			LOGGER.info("Cassandra directory: ''{0}'' is not found. Initializing...", cassandraDirectory);
			Path archiveFile = downloadDir.resolve(String.format("apache-cassandra-%s-bin.tar.gz", version));
			if (!Files.exists(archiveFile)) {
				Files.createDirectories(downloadDir);
				LOGGER.info("Cassandra archive file: ''{0}'' is not found. Downloading...", archiveFile);
				download(httpClient, version, downloadDir, archiveFile);
			}
			extract(version, downloadDir, archiveFile, cassandraDirectory);
			LOGGER.info("Cassandra directory: ''{0}'' is initialized.", cassandraDirectory);
		}
		return cassandraDirectory;
	}

	List<Artifact> getArtifacts(Version version) {
		List<Artifact> artifacts = new ArrayList<>();
		artifacts.add(createArtifact(version, "https://downloads.apache.org/cassandra"
				+ "/%1$s/apache-cassandra-%1$s-bin.tar.gz"));
		artifacts.add(createArtifact(version, "https://archive.apache.org/dist/cassandra/%1$s/"
				+ "apache-cassandra-%1$s-bin.tar.gz"));
		return artifacts;
	}

	private void download(HttpClient httpClient, Version version, Path downloadDir, Path archiveFile)
			throws IOException {
		List<Artifact> artifacts = getArtifacts(version);
		List<HttpResponse> errors = new ArrayList<>();
		Path tempFile = Files.createTempFile(downloadDir, "apache-cassandra-",
				String.format("-%s-bin.tar.gz", version));
		try {
			for (Artifact artifact : artifacts) {
				try (HttpResponse response = httpClient.send(new HttpRequest(artifact.uri))) {
					if (response.getStatus() == 200) {
						LOGGER.info("Downloading Apache Cassandra: ''{0}'' archive file from URI: ''{1}''."
								+ " It takes a while...", version, response.getUri());
						long totalBytes = response.getHeaders().getFirst("Content-Length").map(Long::parseLong)
								.orElse(-1L);
						Progress progress = new Progress(totalBytes);
						try (InputStream is = new ProgressInputStream(response.getInputStream(), progress);
								OutputStream os = Files.newOutputStream(tempFile)) {
							StreamUtils.copy(is, os);
						}
						LOGGER.info("Apache Cassandra: ''{0}'' archive file is downloaded.", version);
						LOGGER.info("Verifying checksum...");
						artifact.verify(tempFile);
						tryMove(tempFile, archiveFile);
						return;
					}
					errors.add(response);
				}
			}
		}
		finally {
			deleteSilently(tempFile);
		}
		throw new FileNotFoundException(
				String.format("Could not download Apache Cassandra: '%s'. Errors: %s", version, errors));
	}

	private void extract(Version version, Path downloadDir, Path archiveFile, Path cassandraDirectory)
			throws IOException {
		LOGGER.info("Extracting Cassandra archive file: ''{0}''...", archiveFile);
		Path tempDirectory = Files.createTempDirectory(downloadDir, String.format("apache-cassandra-%s-", version));
		try (ArchiveInputStream archiveInputStream = new TarArchiveInputStream(
				new GzipCompressorInputStream(Files.newInputStream(archiveFile)))) {
			ArchiveEntry entry;
			while ((entry = archiveInputStream.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					Files.createDirectories(tempDirectory.resolve(entry.getName()));
				}
				else {
					Path file = tempDirectory.resolve(entry.getName());
					Path parent = file.getParent();
					if (parent != null && !Files.exists(parent)) {
						Files.createDirectories(parent);
					}
					Files.copy(archiveInputStream, file, StandardCopyOption.REPLACE_EXISTING);
				}
			}
			tryMove(findCassandraHome(tempDirectory), cassandraDirectory);
		}
		finally {
			deleteSilently(tempDirectory);
		}
		LOGGER.info("Cassandra archive file: ''{0}'' is extracted into directory: ''{1}''", archiveFile,
				cassandraDirectory);

	}

	private Path findCassandraHome(Path directory) throws IOException {
		try (Stream<Path> stream = Files.find(directory, 5, this::isCassandraHome)) {
			return stream.findFirst().orElseThrow(() -> new IllegalStateException(
					"Could not find Apache Cassandra directory in '" + directory + "'"));
		}
	}

	private boolean isCassandraHome(Path path, BasicFileAttributes attributes) {
		if (attributes.isDirectory()) {
			return Files.isDirectory(path.resolve("bin")) && Files.isDirectory(path.resolve("lib"))
					&& Files.isDirectory(path.resolve("conf"));
		}
		return false;
	}

	private Artifact createArtifact(Version version, String uri) {
		return new Artifact(this.httpClient, URI.create(String.format(uri, version)));
	}

	private static void deleteSilently(Path path) {
		try {
			FileUtils.delete(path);
		}
		catch (Exception ex) {
			//ignore
		}
	}

	private static void tryMove(Path src, Path dest) throws IOException {
		try {
			Files.move(src, dest);
		}
		catch (FileAlreadyExistsException ex) {
			//ignore
		}
	}

	static final class Artifact {

		private static final String[] ALGO = {"SHA-512", "SHA-256", "SHA-1", "MD5"};

		private final HttpClient httpClient;

		private final URI uri;

		Artifact(HttpClient httpClient, URI uri) {
			this.httpClient = httpClient;
			this.uri = uri;
		}

		void verify(Path file) throws IOException {
			try {
				doVerify(file);
			}
			catch (NoSuchAlgorithmException ex) {
				throw new IllegalStateException(ex);
			}
		}

		private void doVerify(Path file) throws IOException, NoSuchAlgorithmException {
			List<HttpResponse> errors = new ArrayList<>();
			for (String algo : ALGO) {
				URI uri = getUri(algo);
				HttpResponse response = this.httpClient.send(new HttpRequest(uri));
				if (response.getStatus() == 200) {
					String expected;
					try (InputStream stream = response.getInputStream()) {
						expected = StreamUtils.toString(stream, Charset.defaultCharset()).trim();
					}
					LOGGER.info("Checksum has been downloaded from URI: ''{0}''", uri);
					LOGGER.info("Computes checksum for downloaded file...");
					String actual = FileUtils.hash(file, algo);
					if (!actual.equals(expected)) {
						throw new IllegalStateException(String.format("Checksum mismatch. "
								+ "Actual: '%s' Expected: '%s'", actual, expected));
					}
					LOGGER.info("Checksums are identical");
					return;
				}
				else {
					errors.add(response);
				}
			}
			throw new FileNotFoundException(String.format("Could not download checksums. Errors: %s", errors));
		}

		private URI getUri(String algo) {
			return URI.create(String.format("%s.%s", this.uri, algo.toLowerCase(Locale.ENGLISH).replace("-", "")));
		}

	}

	private static final class Progress implements IntConsumer {

		private final long totalBytes;

		private long readBytes;

		private int lastPercent;

		Progress(long totalBytes) {
			this.totalBytes = totalBytes;
		}

		@Override
		public synchronized void accept(int read) {
			if (this.totalBytes > 0) {
				this.readBytes += read;
				int percent = (int) (this.readBytes * 100 / this.totalBytes);
				if (percent - this.lastPercent >= 10) {
					LOGGER.info("{0} / {1} {2}%", this.readBytes, this.totalBytes, percent);
					this.lastPercent = percent;
				}
			}
		}

	}

	private static final class ProgressInputStream extends FilterInputStream {

		private final IntConsumer consumer;

		protected ProgressInputStream(InputStream in, IntConsumer consumer) {
			super(in);
			this.consumer = consumer;
		}

		@Override
		public int read() throws IOException {
			int read = super.read();
			if (read != -1) {
				this.consumer.accept(read);
			}
			return read;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int read = super.read(b, off, len);
			if (read != -1) {
				this.consumer.accept(read);
			}
			return read;
		}

	}

}
