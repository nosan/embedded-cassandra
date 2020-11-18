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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.github.nosan.embedded.cassandra.commons.FileLock;
import com.github.nosan.embedded.cassandra.commons.FileUtils;
import com.github.nosan.embedded.cassandra.commons.StreamUtils;
import com.github.nosan.embedded.cassandra.commons.StringUtils;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;
import com.github.nosan.embedded.cassandra.commons.web.HttpClient;
import com.github.nosan.embedded.cassandra.commons.web.HttpRequest;
import com.github.nosan.embedded.cassandra.commons.web.HttpResponse;
import com.github.nosan.embedded.cassandra.commons.web.JdkHttpClient;

/**
 * The implementation of {@link CassandraDirectoryProvider}, that downloads and extracts Cassandra archive from the
 * well-known URLs into the download directory.
 * <p>
 * If the Cassandra directory have already existed and initialized then it will be used, skipping downloading and
 * extracting steps.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class WebCassandraDirectoryProvider implements CassandraDirectoryProvider {

	protected static final String[] ALGORITHMS = {"SHA-512", "SHA-256", "SHA-1", "MD5"};

	private static final Logger LOGGER = Logger.get(WebCassandraDirectoryProvider.class);

	private final HttpClient httpClient;

	private final Path downloadDirectory;

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with {@link JdkHttpClient} and {@code user.home} directory.
	 */
	public WebCassandraDirectoryProvider() {
		this(new JdkHttpClient(), Paths.get(System.getProperty("user.home")));
	}

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with provided {@link HttpClient}  and {@code user.home}
	 * directory.
	 *
	 * @param httpClient http client to use
	 */
	public WebCassandraDirectoryProvider(HttpClient httpClient) {
		this(httpClient, Paths.get(System.getProperty("user.home")));
	}

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with {@link JdkHttpClient} and provided download directory.
	 *
	 * @param downloadDirectory the download directory
	 */
	public WebCassandraDirectoryProvider(Path downloadDirectory) {
		this(new JdkHttpClient(), downloadDirectory);
	}

	/**
	 * Creates a new {@link WebCassandraDirectoryProvider} with provided {@link HttpClient} and download directory.
	 *
	 * @param httpClient http client to use
	 * @param downloadDirectory the download directory
	 */
	public WebCassandraDirectoryProvider(HttpClient httpClient, Path downloadDirectory) {
		Objects.requireNonNull(httpClient, "HTTP Client must not be null");
		Objects.requireNonNull(downloadDirectory, "Download Directory must not be null");
		this.httpClient = httpClient;
		this.downloadDirectory = downloadDirectory;
	}

	@Override
	public final Path getDirectory(Version version) throws IOException {
		Objects.requireNonNull(version, "Version must not be null");
		Path downloadDirectory = this.downloadDirectory.resolve(".embedded-cassandra").resolve("cassandra")
				.resolve(version.toString());

		Path successFile = downloadDirectory.resolve(".success");
		Path cassandraDirectory = downloadDirectory.resolve(String.format("apache-cassandra-%s", version));

		if (Files.exists(successFile) && Files.exists(cassandraDirectory)) {
			return cassandraDirectory;
		}
		LOGGER.info("Cassandra directory: ''{0}'' is not found. Initializing...", cassandraDirectory);
		Files.createDirectories(downloadDirectory);
		Path lockFile = downloadDirectory.resolve(".lock");

		try (FileLock fileLock = FileLock.of(lockFile)) {
			LOGGER.info("Acquires a lock to the file ''{0}''...", lockFile);
			if (!tryLock(fileLock)) {
				throw new IOException(String.format("Unable to provide Cassandra Directory for a version: '%s'."
						+ " File lock could not be acquired for a file: '%s'", version, lockFile));
			}

			if (Files.exists(successFile) && Files.exists(cassandraDirectory)) {
				return cassandraDirectory;
			}

			List<CassandraPackage> cassandraPackages = getCassandraPackages(version);
			if (cassandraPackages.isEmpty()) {
				throw new FileNotFoundException(String.format("Unable to provide Cassandra Directory"
						+ " for a version: '%s'. No Packages!", version));
			}
			List<Exception> failures = new ArrayList<>();
			for (CassandraPackage cassandraPackage : cassandraPackages) {
				try {
					downloadAndExtract(version, downloadDirectory, cassandraDirectory, cassandraPackage);
					if (!Thread.currentThread().isInterrupted()) {
						Files.write(successFile, Collections.singleton(ZonedDateTime.now().toString()));
					}
					LOGGER.info("Cassandra directory: ''{0}'' is initialized.", cassandraDirectory);
					return cassandraDirectory;
				}
				catch (Exception ex) {
					failures.add(ex);
				}
			}
			StringBuilder builder = new StringBuilder("Unable to provide Cassandra Directory for a version: '")
					.append(version).append("'").append(System.lineSeparator());
			for (Exception failure : failures) {
				StringWriter writer = new StringWriter();
				failure.printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString()).append(System.lineSeparator());
			}
			throw new IOException(builder.substring(0, builder.length() - System.lineSeparator().length()));
		}
	}

	/**
	 * Gets Cassandra packages to download.
	 * <p>Subclasses may override this method and return their packages to download.
	 *
	 * @param version Cassandra version
	 * @return the list of packages
	 */
	protected List<CassandraPackage> getCassandraPackages(Version version) {
		List<CassandraPackage> packages = new ArrayList<>();
		packages.add(createPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				String.format("https://downloads.apache.org/cassandra"
						+ "/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
		packages.add(createPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				String.format("https://archive.apache.org/dist/cassandra/%1$s/"
						+ "apache-cassandra-%1$s-bin.tar.gz", version)));
		return packages;
	}

	/**
	 * Acquires an exclusive lock on the file.
	 * <p>Subclasses may override this method to change {@code tryLock} timeout.
	 *
	 * @param fileLock the file lock
	 * @return true if lock has been acquired otherwise false
	 * @throws IOException If some other I/O error occurs
	 */
	protected boolean tryLock(FileLock fileLock) throws IOException {
		return fileLock.tryLock(5, TimeUnit.MINUTES);
	}

	/**
	 * Downloads the archive file from the provided URI and writes it into the provided output stream.
	 * <p>Subclasses may override this method and implement their logic for downloading.
	 *
	 * @param os the output stream to write from URI
	 * @param version Cassandra version
	 * @param httpClient Http client to use
	 * @param uri the URI to the file to download
	 * @throws IOException an I/O error occurs or if it is not possible to download.
	 */
	protected void download(HttpClient httpClient, Version version, URI uri, OutputStream os) throws IOException {
		try (HttpResponse response = httpClient.send(new HttpRequest(uri))) {
			if (response.getStatus() == 200) {
				LOGGER.info("Downloading Apache Cassandra: ''{0}'' file from URI: ''{1}''."
						+ " It takes a while...", version, response.getUri());
				long totalBytes = response.getHeaders().getFirst("Content-Length")
						.map(Long::parseLong).orElse(-1L);
				long readBytes = 0;
				int lastPercent = 0;
				byte[] buffer = new byte[8192];
				try (InputStream is = response.getInputStream()) {
					int read;
					while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
						if (totalBytes > 0) {
							readBytes += read;
							int percent = (int) (readBytes * 100 / totalBytes);
							if (percent - lastPercent >= 10 || percent == 100) {
								LOGGER.info("{0} / {1} {2}%", readBytes, totalBytes, percent);
								lastPercent = percent;
							}
						}
					}
				}
			}
			else {
				throw new FileNotFoundException(String.format("Could not download a file. Error: %s", response));
			}
		}
	}

	/**
	 * Extracts the given archive file into the given destination directory.
	 * <p>Subclasses may override this method and implement their logic for extraction.
	 *
	 * @param archiveFile the archive file to extract
	 * @param destination the directory to which to extract the files (already created)
	 * @throws IOException an I/O error occurs
	 */
	protected void extract(Path archiveFile, Path destination) throws IOException {
		try (ArchiveInputStream archiveInputStream = createArchiveInputStream(archiveFile)) {
			ArchiveEntry entry;
			while ((entry = archiveInputStream.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					Files.createDirectories(destination.resolve(entry.getName()).normalize()).toAbsolutePath();
				}
				else {
					Path file = destination.resolve(entry.getName()).normalize().toAbsolutePath();
					Path parent = file.getParent();
					if (!Files.exists(parent)) {
						Files.createDirectories(parent);
					}
					Files.copy(archiveInputStream, file, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	/**
	 * Creates the ArchiveInputStream for a given archive file.
	 *
	 * @param archiveFile the archive file
	 * @return the input stream to use
	 * @throws IOException an I/O error occurs
	 */
	protected ArchiveInputStream createArchiveInputStream(Path archiveFile) throws IOException {
		return new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(archiveFile)));
	}

	private void downloadAndExtract(Version version, Path downloadDirectory, Path cassandraDirectory,
			CassandraPackage cassandraPackage) throws IOException, NoSuchAlgorithmException {
		Path downloadFile = Files.createTempFile(downloadDirectory, "", "-" + cassandraPackage.getName());
		try (OutputStream outputStream = Files.newOutputStream(downloadFile, StandardOpenOption.WRITE)) {
			download(this.httpClient, version, cassandraPackage.getUri(), outputStream);
			verifyChecksums(this.httpClient, downloadFile, cassandraPackage);
			Path extractDirectory = Files.createTempDirectory(downloadDirectory,
					String.format("apache-cassandra-%s-", version));
			try {
				LOGGER.info("Extracting...");
				extract(downloadFile, extractDirectory);
				Path cassandraHome = findCassandraHome(extractDirectory);
				FileUtils.copy(cassandraHome, cassandraDirectory, StandardCopyOption.REPLACE_EXISTING);
			}
			finally {
				deleteSilently(extractDirectory);
			}
		}
		finally {
			deleteSilently(downloadFile);
		}
	}

	private void verifyChecksums(HttpClient httpClient, Path archiveFile, CassandraPackage cassandraPackage)
			throws IOException, NoSuchAlgorithmException {
		LOGGER.info("Verifying checksum...");
		List<HttpResponse> failures = new ArrayList<>();
		Map<String, URI> checksums = cassandraPackage.getChecksums();
		if (checksums.isEmpty()) {
			LOGGER.warn("Skipping checksum verifying. No available checksums");
			return;
		}
		for (Map.Entry<String, URI> checksum : checksums.entrySet()) {
			String algo = checksum.getKey();
			URI uri = checksum.getValue();
			HttpResponse response = httpClient.send(new HttpRequest(uri));
			if (response.getStatus() == 200) {
				String expected;
				try (InputStream stream = response.getInputStream()) {
					expected = StreamUtils.toString(stream, Charset.defaultCharset()).trim();
				}
				String[] tokens = expected.split("\\s+");
				String actual = FileUtils.checksum(archiveFile, algo);
				if (tokens.length == 2) {
					verify(actual + " " + cassandraPackage.getName(), tokens[0] + " " + tokens[1]);
				}
				else {
					verify(actual, tokens[0]);
				}
				LOGGER.info("Checksums are identical");
				return;
			}
			else {
				failures.add(response);
			}
		}
		StringBuilder builder = new StringBuilder("Could not download checksums").append(System.lineSeparator());
		for (HttpResponse failure : failures) {
			builder.append(failure).append(System.lineSeparator());
		}
		throw new FileNotFoundException(builder.substring(0, builder.length() - System.lineSeparator().length()));
	}

	private void verify(String actual, String expected) {
		if (!actual.equals(expected)) {
			LOGGER.error("Checksum mismatch!");
			throw new IllegalStateException(String.format("Checksum mismatch. "
					+ "Actual: '%s' Expected: '%s'", actual, expected));
		}
	}

	private Path findCassandraHome(Path directory) throws IOException {
		try (Stream<Path> stream = Files.find(directory, 5, this::isCassandraHome)) {
			return stream.findFirst().orElseThrow(() -> new IllegalStateException(
					"Could not find Apache Cassandra directory in directory: '" + directory + "'"));
		}
	}

	private boolean isCassandraHome(Path path, BasicFileAttributes attributes) {
		if (attributes.isDirectory()) {
			return Files.isDirectory(path.resolve("bin")) && Files.isDirectory(path.resolve("lib"))
					&& Files.isDirectory(path.resolve("conf"));
		}
		return false;
	}

	private static CassandraPackage createPackage(String name, String uri) {
		Map<String, URI> checksums = new LinkedHashMap<>();
		for (String algo : ALGORITHMS) {
			checksums.put(algo, URI.create(String.format("%s.%s", uri,
					algo.toLowerCase(Locale.ENGLISH).replace("-", ""))));
		}
		return new CassandraPackage(name, URI.create(uri), checksums);
	}

	private static void deleteSilently(Path path) {
		try {
			FileUtils.delete(path);
		}
		catch (Exception ex) {
			//ignore
		}
	}

	/**
	 * Represents Cassandra package to download.
	 */
	protected static final class CassandraPackage {

		private final String name;

		private final URI uri;

		private final Map<String, URI> checksums;

		/**
		 * Creates {@link CassandraPackage}.
		 *
		 * @param name the name of the package.
		 * <pre>apache-cassandra-4.0-beta3-bin.tar.gz</pre>
		 * @param uri the URI to the package to download.
		 * <pre>https://URL/apache-cassandra-4.0-beta3-bin.tar.gz</pre>
		 * @param checksums URIs to download checksums. If empty checksum verifying will be skipped.
		 * <pre>SHA-512 : https://URL/apache-cassandra-4.0-beta3-bin.tar.gz.sha512</pre>
		 */
		public CassandraPackage(String name, URI uri, Map<String, URI> checksums) {
			Objects.requireNonNull(name, "Name must not be null");
			Objects.requireNonNull(uri, "URI must not be null");
			Objects.requireNonNull(checksums, "Checksums must not be null");
			if (!StringUtils.hasText(name)) {
				throw new IllegalArgumentException("Name must not be empty");
			}
			this.name = name;
			this.uri = uri;
			this.checksums = Collections.unmodifiableMap(checksums);
		}

		/**
		 * Gets the URI to the package to download.
		 *
		 * @return the URI
		 */
		public URI getUri() {
			return this.uri;
		}

		/**
		 * Gets the package name.
		 *
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Gets URIs to download checksums.
		 *
		 * @return the URIS
		 */
		public Map<String, URI> getChecksums() {
			return this.checksums;
		}

	}

}
