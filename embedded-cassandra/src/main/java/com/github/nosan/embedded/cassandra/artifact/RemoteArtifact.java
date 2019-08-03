/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.artifact;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.MDCThreadFactory;
import com.github.nosan.embedded.cassandra.commons.PathSupplier;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * An artifact that downloads Apache Cassandra from the specified URLs and extracts the archive to the specified
 * directory. The latter used for determines {@code Cassandra's} home directory.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class RemoteArtifact implements Artifact {

	private static final Logger log = LoggerFactory.getLogger(RemoteArtifact.class);

	private final List<URL> urls = new ArrayList<>();

	private final Version version;

	private Duration readTimeout = Duration.ofSeconds(10);

	private Duration connectTimeout = Duration.ofSeconds(3);

	private Proxy proxy = Proxy.NO_PROXY;

	@Nullable
	private Path extractDirectory;

	/**
	 * Constructs a new {@link RemoteArtifact} with the specified version.
	 *
	 * @param version the version
	 */
	public RemoteArtifact(Version version) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
	}

	/**
	 * Constructs a new {@link RemoteArtifact} with the specified version and extract directory.
	 *
	 * @param version the version
	 * @param extractDirectory the directory to extract an archive file
	 */
	public RemoteArtifact(Version version, @Nullable Path extractDirectory) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
		this.extractDirectory = extractDirectory;
	}

	/**
	 * Returns Cassandra's version.
	 *
	 * @return the version
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * URLs to download an archive file. Defaults to
	 * <pre>{@code https://apache.org/dyn/closer.cgi/...
	 * https://dist.apache.org/repos/dist/release/cassandra/...
	 * https://archive.apache.org/dist/cassandra/...}</pre>
	 *
	 * @return the URLs
	 */
	public List<URL> getUrls() {
		return this.urls;
	}

	/**
	 * Proxy through which artifact will download an archive file. Defaults to {@code NO_PROXY}.
	 *
	 * @return the proxy
	 */
	public Proxy getProxy() {
		return this.proxy;
	}

	/**
	 * Sets proxy through which artifact will download an archive file.
	 *
	 * @param proxy the proxy
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = Objects.requireNonNull(proxy, "'proxy' must not be null");
	}

	/**
	 * Directory used to extract an archive file. Defaults to {@code {user.home}/.embeddedCassandra/artifact}
	 *
	 * @return the directory
	 */
	@Nullable
	public Path getExtractDirectory() {
		return this.extractDirectory;
	}

	/**
	 * Sets directory used to extract an archive file.
	 *
	 * @param extractDirectory the directory to extract an archive file
	 */
	public void setExtractDirectory(@Nullable Path extractDirectory) {
		this.extractDirectory = extractDirectory;
	}

	/**
	 * Timeout when reading from Input stream when a connection is established to the URL. Defaults to {@code 10
	 * seconds}.
	 *
	 * @return the read timeout
	 */
	public Duration getReadTimeout() {
		return this.readTimeout;
	}

	/**
	 * Sets read timeout when reading from Input stream when a connection is established to the URL.
	 *
	 * @param readTimeout the read timeout
	 */
	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = Objects.requireNonNull(readTimeout, "'readTimeout' must not be null");
	}

	/**
	 * Connection timeout to be used when opening a communications link to the URL. Defaults to {@code 3 seconds}.
	 *
	 * @return the connection timeout
	 */
	public Duration getConnectTimeout() {
		return this.connectTimeout;
	}

	/**
	 * Sets connection timeout to be used when opening a communications link to the URL.
	 *
	 * @param connectTimeout the connection timeout
	 */
	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = Objects.requireNonNull(connectTimeout, "'connectTimeout' must not be null");
	}

	@Override
	public Resource getResource() throws Exception {
		Version version = getVersion();
		List<URL> urls = new ArrayList<>(getUrls());
		if (urls.isEmpty()) {
			urls.add(new URL(String.format("https://apache.org/dyn/closer.cgi?action=download"
					+ "&filename=cassandra/%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
			urls.add(new URL(String.format("https://dist.apache.org/repos/dist/release/cassandra/"
					+ "%1$s/apache-cassandra-%1$s-bin.tar.gz", version)));
			urls.add(new URL(String
					.format("https://archive.apache.org/dist/cassandra/%1$s/apache-cassandra-%1$s-bin.tar.gz",
							version)));
		}
		UrlArchiveFileSupplier archiveFileSupplier = new UrlArchiveFileSupplier(version, getReadTimeout(),
				getConnectTimeout(), getProxy(), urls);
		return new ArchiveArtifact(version, getExtractDirectory(), archiveFileSupplier).getResource();
	}

	private static final class UrlArchiveFileSupplier implements PathSupplier {

		private final Version version;

		private final Duration readTimeout;

		private final Duration connectTimeout;

		private final Proxy proxy;

		private final List<URL> urls;

		UrlArchiveFileSupplier(Version version, Duration readTimeout, Duration connectTimeout, Proxy proxy,
				List<URL> urls) {
			this.version = version;
			this.readTimeout = readTimeout;
			this.connectTimeout = connectTimeout;
			this.proxy = proxy;
			this.urls = Collections.unmodifiableList(new ArrayList<>(urls));
		}

		@Override
		public Path get() throws IOException {
			List<IOException> exceptions = new ArrayList<>();
			for (URL url : this.urls) {
				try {
					return downloadFile(url);
				}
				catch (ClosedByInterruptException ex) {
					throw ex;
				}
				catch (IOException ex) {
					exceptions.add(ex);
				}
			}
			IOException ex = new IOException("Apache Cassandra cannot be downloaded from " + this.urls);
			exceptions.forEach(ex::addSuppressed);
			throw ex;
		}

		private Path downloadFile(URL url) throws IOException {
			URLConnection connection = openConnection(url, 20);
			try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
				long expectedSize = connection.getContentLengthLong();
				Path tempFile = createTempFile(url, connection.getURL());
				FileProgress fileProgress = new FileProgress(tempFile, expectedSize);
				ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new MDCThreadFactory());
				log.info("Downloading Apache Cassandra '{}' from '{}'", this.version, connection.getURL());
				long start = System.currentTimeMillis();
				scheduler.scheduleAtFixedRate(fileProgress::update, 0, 1, TimeUnit.SECONDS);
				try {
					Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
				}
				finally {
					scheduler.shutdown();
				}
				long elapsed = System.currentTimeMillis() - start;
				log.info("Apache Cassandra '{}' is downloaded ({} ms)", this.version, elapsed);
				return tempFile;
			}
		}

		private URLConnection openConnection(URL url, int maxRedirects) throws IOException {
			URLConnection connection = url.openConnection(this.proxy);
			connection.setConnectTimeout(Math.toIntExact(this.connectTimeout.toMillis()));
			connection.setReadTimeout(Math.toIntExact(this.readTimeout.toMillis()));
			if (connection instanceof HttpURLConnection) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setInstanceFollowRedirects(false);
				int status = httpConnection.getResponseCode();
				if (status >= 200 && status < 300) {
					return httpConnection;
				}
				else if (status >= 300 && status <= 307 && status != 306 && status != 304) {
					if (maxRedirects > 0) {
						String location = httpConnection.getHeaderField("Location");
						if (StringUtils.hasText(location)) {
							return openConnection(new URL(httpConnection.getURL(), location), maxRedirects - 1);
						}
					}
					else {
						throw new IOException("Too many redirects for URL '" + url + "'");
					}
				}
				else if (status >= 400 || status < 200) {
					throw new IOException("HTTP Status '" + status + "' is invalid for URL '" + url + "'");
				}
				return httpConnection;
			}
			return connection;
		}

		private static Path createTempFile(URL original, URL connection) throws IOException {
			String fileName = getFileName(connection);
			if (!StringUtils.hasText(fileName)) {
				fileName = getFileName(original);
			}
			if (!StringUtils.hasText(fileName)) {
				throw new IllegalArgumentException(
						String.format("There is no way to determine a file name from '%s' and '%s'", original,
								connection));
			}
			Path tempFile = Files.createTempFile("", "-" + fileName);
			tempFile.toFile().deleteOnExit();
			return tempFile;
		}

		@Nullable
		private static String getFileName(URL url) {
			String fileName = url.getFile();
			if (StringUtils.hasText(fileName) && fileName.contains("/")) {
				fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
			}
			return fileName;
		}

		private static class FileProgress {

			private static final int MB = 1024 * 1024;

			private final Path file;

			private final long expectedSize;

			private long lastPercent;

			FileProgress(Path file, long expectedSize) {
				this.file = file;
				this.expectedSize = expectedSize;
			}

			void update() {
				if (this.expectedSize > 0) {
					long currentSize = getSize(this.file);
					if (currentSize > 0) {
						long percent = currentSize * 100 / this.expectedSize;
						if ((percent - this.lastPercent) >= 5) {
							this.lastPercent = percent;
							log.info("Downloaded {} / {}  {}%", formatSize(currentSize), formatSize(this.expectedSize),
									percent);
						}
					}
				}
			}

			private static long getSize(Path file) {
				try {
					return Files.size(file);
				}
				catch (IOException ex) {
					return -1;
				}
			}

			private static String formatSize(long bytes) {
				long mb = bytes / MB;
				return (mb > 0) ? mb + "MB" : bytes + "B";
			}

		}

	}

}
