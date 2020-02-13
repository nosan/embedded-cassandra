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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.github.nosan.embedded.cassandra.commons.FileUtils;
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
			Path archiveFile = downloadDir.resolve(String.format("apache-cassandra-%s-bin.tar.gz", version));
			if (!Files.exists(archiveFile)) {
				Files.createDirectories(downloadDir);
				download(httpClient, version, downloadDir, archiveFile);
			}
			extract(version, downloadDir, archiveFile, cassandraDirectory);
		}
		return cassandraDirectory;
	}

	private static void download(HttpClient httpClient, Version version, Path downloadDir, Path archiveFile)
			throws IOException {
		List<URI> uris = getDownloadUris(version);
		List<HttpResponse> errors = new ArrayList<>();
		Path tempFile = Files.createTempFile(downloadDir, "",
				String.format("-apache-cassandra-%s-bin.tar.gz", version));
		try {
			for (URI uri : uris) {
				try (HttpResponse response = httpClient.send(new HttpRequest(uri))) {
					if (response.getStatus() == 200) {
						LOGGER.info("Downloading Apache Cassandra {0} from {1}. It takes a while...", version,
								response.getUri());
						long totalBytes = response.getHeaders().getFirst("Content-Length").map(Long::parseLong)
								.orElse(-1L);
						try (InputStream is = response.getInputStream();
								OutputStream os = Files.newOutputStream(tempFile)) {
							byte[] buffer = new byte[8192];
							long readBytes = 0;
							long percent;
							long lastPercent = 0;
							int read;
							while ((read = is.read(buffer)) != -1) {
								os.write(buffer, 0, read);
								if (totalBytes > 0) {
									readBytes += read;
									percent = readBytes * 100 / totalBytes;
									if (percent - lastPercent >= 10) {
										LOGGER.info("{0} / {1} {2}%", readBytes, totalBytes, percent);
										lastPercent = percent;
									}
								}
							}
						}
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
				String.format("Apache Cassandra '%s' is not found. Errors: %s", version, errors));
	}

	private static List<URI> getDownloadUris(Version version) {
		List<URI> uris = new ArrayList<>(2);
		uris.add(URI.create(String.format("https://apache.org/dyn/closer.cgi?action=download"
				+ "&filename=cassandra/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
		uris.add(URI.create(String.format("https://archive.apache.org/dist/cassandra/%1$s/"
				+ "apache-cassandra-%1$s-bin.tar.gz", version)));
		return uris;
	}

	private static void extract(Version version, Path downloadDir, Path archiveFile, Path cassandraDirectory)
			throws IOException {
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

	}

	private static Path findCassandraHome(Path directory) throws IOException {
		try (Stream<Path> stream = Files.find(directory, 5, WebCassandraDirectoryProvider::isCassandraHome)) {
			return stream.findFirst().orElseThrow(() -> new IllegalStateException(
					"Could not find Apache Cassandra directory in '" + directory + "'"));
		}
	}

	private static boolean isCassandraHome(Path path, BasicFileAttributes attributes) {
		if (attributes.isDirectory()) {
			return Files.isDirectory(path.resolve("bin")) && Files.isDirectory(path.resolve("lib"))
					&& Files.isDirectory(path.resolve("conf"));
		}
		return false;
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

}
