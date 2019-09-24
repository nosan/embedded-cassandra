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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.FileLock;
import com.github.nosan.embedded.cassandra.commons.io.ArchiveResource;
import com.github.nosan.embedded.cassandra.commons.io.FileSystemResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;
import com.github.nosan.embedded.cassandra.commons.io.UrlResource;
import com.github.nosan.embedded.cassandra.commons.util.FileUtils;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * An {@link Artifact} that downloads an archive from the {@code Internet} and then extracts it to the {@code
 * destination} directory. Archive will be extracted and downloaded only once.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class RemoteArtifact implements Artifact {

	private static final Logger log = LoggerFactory.getLogger(RemoteArtifact.class);

	private final Version version;

	private Duration readTimeout = Duration.ofSeconds(10);

	private Duration connectTimeout = Duration.ofSeconds(3);

	private Proxy proxy = Proxy.NO_PROXY;

	private UrlFactory urlFactory = new DefaultUrlFactory();

	@Nullable
	private Path destination;

	/**
	 * Constructs a new {@link RemoteArtifact} with the specified version.
	 *
	 * @param version the version
	 */
	public RemoteArtifact(Version version) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
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
	 * Returns {@link UrlFactory}. Defaults to {@link DefaultUrlFactory}.
	 *
	 * @return the UrlFactory
	 */
	public UrlFactory getUrlFactory() {
		return this.urlFactory;
	}

	/**
	 * Sets {@link UrlFactory}.
	 *
	 * @param urlFactory the UrlFactory
	 */
	public void setUrlFactory(UrlFactory urlFactory) {
		this.urlFactory = Objects.requireNonNull(urlFactory, "'urlFactory' must not be null");
	}

	/**
	 * Directory used to extract an archive file. Defaults to {@code user.home}
	 *
	 * @return the destination directory
	 */
	@Nullable
	public Path getDestination() {
		return this.destination;
	}

	/**
	 * Sets directory to extract an archive file.
	 *
	 * @param destination the path to the directory
	 */
	public void setDestination(@Nullable Path destination) {
		this.destination = destination;
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
	public Distribution getDistribution() throws IOException {
		Path destination = getRealDestination();
		Artifact artifact = new DefaultArtifact(this.version, destination);
		if (!Files.exists(destination.resolve(".extracted"))) {
			Files.createDirectories(destination);
			Path lockFile = destination.resolve(".lock");
			try (FileLock fileLock = FileLock.of(lockFile)) {
				log.info("Acquires a lock to the file '{}' ...", lockFile);
				if (!fileLock.tryLock(2, TimeUnit.MINUTES)) {
					throw new IllegalStateException("File lock cannot be acquired for a file '" + lockFile + "'");
				}
				log.info("The lock to the file '{}' was acquired", lockFile);
				if (!Files.exists(destination.resolve(".extracted"))) {
					Resource resource = download();
					log.info("Extracts '{}' into '{}' directory", resource, destination);
					ArchiveResource archiveResource = new ArchiveResource(resource);
					archiveResource.extract(destination);
					Distribution distribution = artifact.getDistribution();
					FileUtils.createIfNotExists(destination.resolve(".extracted"));
					return distribution;
				}
			}
		}
		return artifact.getDistribution();
	}

	private Path getRealDestination() {
		Path destination = this.destination;
		if (destination == null) {
			destination = Optional.ofNullable(System.getProperty("user.home")).map(Paths::get).orElse(null);
		}
		if (destination == null) {
			throw new IllegalStateException("'destination' must not be null");
		}
		return destination.resolve(".embedded-cassandra/artifact/remote/" + this.version);
	}

	private Resource download() throws IOException {
		List<Exception> exceptions = new ArrayList<>();
		List<URL> urls = this.urlFactory.create(this.version);
		FileDownloader downloader = new FileDownloader(this.readTimeout, this.connectTimeout, this.proxy);
		for (URL url : urls) {
			try {
				return downloader.download(url, new DefaultProgressListener(url, this.version));
			}
			catch (ClosedByInterruptException ex) {
				throw ex;
			}
			catch (Exception ex) {
				exceptions.add(ex);
			}
		}
		IOException ex = new IOException("Apache Cassandra cannot be downloaded from " + urls);
		exceptions.forEach(ex::addSuppressed);
		throw ex;
	}

	private interface ProgressListener {

		void start();

		void update(long readBytes, long totalBytes);

		void finish();

	}

	private static final class DefaultProgressListener implements ProgressListener {

		private static final long MB = 1024 * 1024;

		private final URL url;

		private final Version version;

		private long lastPercent;

		DefaultProgressListener(URL url, Version version) {
			this.url = url;
			this.version = version;
		}

		@Override
		public void start() {
			log.info("Downloading Apache Cassandra '{}' from '{}'", this.version, this.url);
		}

		@Override
		public void update(long readBytes, long totalBytes) {
			long percent = readBytes * 100 / totalBytes;
			if (percent - this.lastPercent >= 10) {
				this.lastPercent = percent;
				log.info("Downloaded {}MB / {}MB  {}%", (readBytes / MB), (totalBytes / MB), percent);
			}
		}

		@Override
		public void finish() {
			log.info("Apache Cassandra '{}' is downloaded from '{}'", this.version, this.url);
		}

	}

	private static final class FileDownloader {

		private final Duration readTimeout;

		private final Duration connectTimeout;

		private final Proxy proxy;

		FileDownloader(Duration readTimeout, Duration connectTimeout, Proxy proxy) {
			this.readTimeout = readTimeout;
			this.connectTimeout = connectTimeout;
			this.proxy = proxy;
		}

		Resource download(URL url, ProgressListener progressListener) throws IOException {
			URLConnection connection = connect(url);
			try (InputStream is = connection.getInputStream()) {
				long totalSize = connection.getContentLengthLong();
				Path tempFile = createTempFile(url);
				progressListener.start();
				try (OutputStream os = Files.newOutputStream(tempFile)) {
					byte[] buffer = new byte[8192];
					long readBytes = 0;
					int read;
					while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
						readBytes += read;
						if (totalSize > 0 && readBytes > 0) {
							progressListener.update(readBytes, totalSize);
						}
					}
				}
				if (Thread.interrupted()) {
					throw new ClosedByInterruptException();
				}
				progressListener.finish();
				return new FileSystemResource(tempFile);
			}
		}

		private URLConnection connect(URL url) throws IOException {
			int maxRedirects = 10;
			URL target = url;
			for (; ; ) {
				URLConnection connection = connect(target, this.readTimeout, this.connectTimeout, this.proxy);
				if (connection instanceof HttpURLConnection) {
					HttpURLConnection httpConnection = (HttpURLConnection) connection;
					httpConnection.setInstanceFollowRedirects(false);
					int status = httpConnection.getResponseCode();
					if (status >= 300 && status <= 307 && status != 306 && status != 304) {
						if (maxRedirects < 0) {
							throw new IOException("Too many redirects for URL '" + url + "'");
						}
						String location = httpConnection.getHeaderField("Location");
						if (location != null) {
							httpConnection.disconnect();
							maxRedirects--;
							target = new URL(url, location);
							continue;
						}
					}
					if (status == HttpURLConnection.HTTP_OK) {
						return connection;
					}
					throw new IOException("HTTP Status '" + status + "' is invalid for URL '" + url + "'");
				}
				return connection;
			}

		}

		private static URLConnection connect(URL url, Duration readTimeout, Duration connectTimeout, Proxy proxy)
				throws IOException {
			URLConnection connection = url.openConnection(proxy);
			connection.setConnectTimeout(Math.toIntExact(connectTimeout.toMillis()));
			connection.setReadTimeout(Math.toIntExact(readTimeout.toMillis()));
			return connection;
		}

		private static Path createTempFile(URL url) throws IOException {
			String fileName = new UrlResource(url).getFileName();
			if (!StringUtils.hasText(fileName)) {
				throw new IllegalArgumentException(
						String.format("There is no way to determine a file name from a '%s'", url));
			}
			Path tempFile = Files.createTempFile("", "-" + fileName);
			tempFile.toFile().deleteOnExit();
			return tempFile;
		}

	}

}
