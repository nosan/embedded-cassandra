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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.DefaultThreadFactory;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Artifact} implementation, that downloads an {@code archive} from the internet, only if an {@code archive}
 * doesn't exist locally.
 *
 * @author Dmytro Nosan
 * @see RemoteArtifactFactory
 * @since 1.0.0
 */
class RemoteArtifact implements Artifact {

	private static final Logger log = LoggerFactory.getLogger(RemoteArtifact.class);

	private final Version version;

	private final Path directory;

	private final UrlFactory urlFactory;

	private final Duration readTimeout;

	private final Duration connectTimeout;

	@Nullable
	private final Proxy proxy;

	RemoteArtifact(Version version, Path directory, UrlFactory urlFactory, @Nullable Proxy proxy, Duration readTimeout,
			Duration connectTimeout) {
		this.version = version;
		this.directory = directory;
		this.urlFactory = urlFactory;
		this.proxy = proxy;
		this.readTimeout = readTimeout;
		this.connectTimeout = connectTimeout;
	}

	@Override
	public Path getArchive() throws IOException {
		Version version = this.version;
		Path directory = this.directory;
		URL[] urls = this.urlFactory.create(version);
		Objects.requireNonNull(urls, "URLs must not be null");

		Files.createDirectories(directory);

		IOException exceptions = new IOException(
				String.format("Can not download a resource from URLs %s. See suppressed exceptions for details",
						Arrays.toString(urls)));

		for (URL url : urls) {
			try {
				Resource remoteResource = new RemoteResource(directory, version, url, this.proxy, this.readTimeout,
						this.connectTimeout);
				Resource localResource = new LocalResource(directory, remoteResource);
				return localResource.getFile();
			}
			catch (ClosedByInterruptException ex) {
				throw ex;
			}
			catch (IOException ex) {
				log.warn(ex.getMessage());
				exceptions.addSuppressed(ex);
			}
		}
		throw exceptions;
	}

	/**
	 * Resource that abstracts from the actual type of underlying source.
	 */
	private interface Resource {

		/**
		 * Return a File handle for this resource.
		 *
		 * @return the file
		 * @throws IOException in case of any I/O errors
		 */
		Path getFile() throws IOException;

		/**
		 * Return a name for this resource.
		 *
		 * @return a resource name
		 */
		String getName();

	}

	private static final class LocalResource implements Resource {

		private final Path directory;

		private final Resource resource;

		LocalResource(Path directory, Resource resource) {
			this.directory = directory;
			this.resource = resource;
		}

		@Override
		public Path getFile() throws IOException {
			Path file = this.directory.resolve(getName());
			if (!Files.exists(file)) {
				Path tempFile = this.resource.getFile();
				try {
					return Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
				}
				catch (IOException ex) {
					log.error(String.format("Can not rename '%s' as '%s'.", tempFile, file), ex);
					return tempFile;
				}
			}
			return file;
		}

		@Override
		public String getName() {
			return this.resource.getName();
		}

	}

	private static final class RemoteResource implements Resource {

		private static final AtomicLong counter = new AtomicLong();

		private final ThreadFactory threadFactory = new DefaultThreadFactory(String.format("progress-%d",
				counter.incrementAndGet()));

		private final Path directory;

		private final Version version;

		private final URL url;

		@Nullable
		private final Proxy proxy;

		private final Duration readTimeout;

		private final Duration connectTimeout;

		RemoteResource(Path directory, Version version, URL url, @Nullable Proxy proxy, Duration readTimeout,
				Duration connectTimeout) {
			this.directory = directory;
			this.version = version;
			this.url = url;
			this.proxy = proxy;
			this.readTimeout = readTimeout;
			this.connectTimeout = connectTimeout;
		}

		@Override
		public Path getFile() throws IOException {
			int maxRedirects = 20;
			URLConnection urlConnection = getUrlConnection(this.url, maxRedirects, 1);
			long size = urlConnection.getContentLengthLong();

			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(this.threadFactory);

			Path file = this.directory
					.resolve(String.format("download-%s-%s", UUID.randomUUID(), getFileName(this.url)));
			file.toFile().deleteOnExit();

			try (InputStream urlInputStream = urlConnection.getInputStream()) {
				log.info("Downloading Apache Cassandra '{}' from '{}'.", this.version, urlConnection.getURL());
				long start = System.currentTimeMillis();
				showProgress(file, size, executorService);
				Files.copy(urlInputStream, file, StandardCopyOption.REPLACE_EXISTING);
				long fileSize = Files.size(file);
				if (fileSize < size) {
					throw new IOException(
							String.format("The size '%d' of the file '%s' is not valid. Expected size is '%d'",
									fileSize, file, size));
				}
				long elapsed = System.currentTimeMillis() - start;
				log.info("Apache Cassandra '{}' is downloaded ({} ms)", this.version, elapsed);
			}
			finally {
				executorService.shutdown();
			}

			return file;
		}

		@Override
		public String getName() {
			return getFileName(this.url);
		}

		private static String getFileName(URL url) {
			String fileName = url.getFile();
			if (StringUtils.hasText(fileName) && fileName.contains("/")) {
				fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
			}
			if (!StringUtils.hasText(fileName)) {
				throw new IllegalArgumentException(
						String.format("There is no way to determine a file name from '%s'", url));
			}
			return fileName;
		}

		private static void showProgress(Path file, long size, ScheduledExecutorService executorService) {
			if (size > 0) {
				AtomicLong prevPercent = new AtomicLong(0);
				int minPercentStep = 10;
				AtomicBoolean logOnlyOnce = new AtomicBoolean(false);
				executorService.scheduleAtFixedRate(() -> {
					try {
						if (Files.exists(file)) {
							long current = Files.size(file);
							long percent = Math.max(current * 100, 1) / size;
							if (percent - prevPercent.get() >= minPercentStep) {
								prevPercent.set(percent);
								log.info("Downloaded {} / {}  {}%", formatSize(current), formatSize(size), percent);
							}
						}
					}
					catch (Exception ex) {
						if (logOnlyOnce.compareAndSet(false, true)) {
							log.error(String.format("Can not show progress for a file '%s'", file), ex);
						}
					}
				}, 0, 1, TimeUnit.SECONDS);
			}
		}

		private static String formatSize(long bytes) {
			if (bytes > 1024) {
				long kilobytes = bytes / 1024;
				if (kilobytes > 1024) {
					return (kilobytes / 1024) + "MB";
				}
				return kilobytes + "KB";
			}
			return bytes + "B";
		}

		private URLConnection getUrlConnection(URL url, int maxRedirects, int redirectCount) throws IOException {
			URLConnection connection = (this.proxy != null) ? url.openConnection(this.proxy) : url.openConnection();
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
					if (redirectCount <= maxRedirects) {
						String location = httpConnection.getHeaderField("Location");
						if (StringUtils.hasText(location)) {
							return getUrlConnection(new URL(httpConnection.getURL(), location), maxRedirects,
									redirectCount + 1);
						}
					}
					else {
						throw new IOException(String.format("Too many redirects for URL '%s'", url));
					}
				}
				else if (status >= 400 || status < 200) {
					throw new IOException(String.format("HTTP (%d %s) status for URL '%s'", status,
							httpConnection.getResponseMessage(), url));
				}
				return httpConnection;
			}

			return connection;
		}

	}

}
