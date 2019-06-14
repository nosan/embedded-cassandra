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

import java.io.BufferedInputStream;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.MDCThreadFactory;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Artifact} that downloads an {@code archive} from the internet.
 *
 * @author Dmytro Nosan
 * @see RemoteArtifactFactory
 * @since 1.0.0
 */
class RemoteArtifact implements Artifact {

	private static final Logger log = LoggerFactory.getLogger(RemoteArtifact.class);

	private static final AtomicLong artifactNumber = new AtomicLong();

	private static final int MAX_REDIRECTS = 20;

	private final ThreadFactory threadFactory = new MDCThreadFactory(String.format("artifact-%d",
			artifactNumber.incrementAndGet()), true);

	private final Version version;

	private final UrlFactory urlFactory;

	private final Duration readTimeout;

	private final Duration connectTimeout;

	@Nullable
	private final Proxy proxy;

	RemoteArtifact(Version version, UrlFactory urlFactory, @Nullable Proxy proxy, Duration readTimeout,
			Duration connectTimeout) {
		this.version = version;
		this.urlFactory = urlFactory;
		this.proxy = proxy;
		this.readTimeout = readTimeout;
		this.connectTimeout = connectTimeout;
	}

	@Override
	public Path getArchive() throws IOException {
		Version version = this.version;
		URL[] urls = this.urlFactory.create(version);
		Objects.requireNonNull(urls, "URLs must not be null");
		IOException exceptions = new IOException(String.format("Can not download a resource from URLs %s."
				+ " See suppressed exceptions for details", Arrays.toString(urls)));
		for (URL url : urls) {
			try {
				return getFile(url);
			}
			catch (ClosedByInterruptException ex) {
				throw ex;
			}
			catch (IOException ex) {
				exceptions.addSuppressed(ex);
			}
		}
		throw exceptions;
	}

	private Path getFile(URL url) throws IOException {
		URLConnection connection = getUrlConnection(url, MAX_REDIRECTS);
		try (InputStream stream = new BufferedInputStream(connection.getInputStream())) {
			long expectedSize = connection.getContentLengthLong();
			Path file = Files.createTempFile(null, String.format("-%s", getFileName(url)));
			file.toFile().deleteOnExit();
			FileProgress fileProgress = new FileProgress(file, expectedSize);
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(this.threadFactory);
			log.info("Downloading Apache Cassandra '{}' from '{}'.", this.version, connection.getURL());
			long start = System.currentTimeMillis();
			scheduler.scheduleAtFixedRate(fileProgress::update, 0, 1, TimeUnit.SECONDS);
			try {
				Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
			}
			finally {
				scheduler.shutdown();
			}
			long elapsed = System.currentTimeMillis() - start;
			log.info("Apache Cassandra '{}' is downloaded ({} ms)", this.version, elapsed);
			return file;
		}
	}

	private String getFileName(URL url) {
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

	private URLConnection getUrlConnection(URL url, int maxRedirects) throws IOException {
		URLConnection urlConnection = (this.proxy != null) ? url.openConnection(this.proxy) : url.openConnection();
		urlConnection.setConnectTimeout(Math.toIntExact(this.connectTimeout.toMillis()));
		urlConnection.setReadTimeout(Math.toIntExact(this.readTimeout.toMillis()));
		if (urlConnection instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
			httpConnection.setInstanceFollowRedirects(false);
			int status = httpConnection.getResponseCode();
			if (status >= 200 && status < 300) {
				return httpConnection;
			}
			else if (status >= 300 && status <= 307 && status != 306 && status != 304) {
				if (maxRedirects > 0) {
					String location = httpConnection.getHeaderField("Location");
					if (StringUtils.hasText(location)) {
						return getUrlConnection(new URL(httpConnection.getURL(), location), maxRedirects - 1);
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

		return urlConnection;
	}

	private static class FileProgress {

		private static final long MIN_STEP_PERCENT = 10;

		private final Path file;

		private long lastPercent;

		private long expectedSize;

		FileProgress(Path file, long expectedSize) {
			this.file = file;
			this.expectedSize = expectedSize;
		}

		void update() {
			long currentSize = getCurrentSize();
			long expectedSize = this.expectedSize;
			if (currentSize > 0 && expectedSize > 0) {
				long currentPercent = currentSize * 100 / expectedSize;
				if ((currentPercent - this.lastPercent) >= MIN_STEP_PERCENT) {
					this.lastPercent = currentPercent;
					log.info("Downloaded {} / {}  {}%", getFormatSize(currentSize), getFormatSize(expectedSize),
							currentPercent);
				}
			}
		}

		private long getCurrentSize() {
			try {
				return Files.size(this.file);
			}
			catch (IOException ex) {
				return -1;
			}
		}

		private String getFormatSize(long bytes) {
			if (bytes > 1024) {
				long kilobytes = bytes / 1024;
				if (kilobytes > 1024) {
					return (kilobytes / 1024) + "MB";
				}
				return kilobytes + "KB";
			}
			return bytes + "B";
		}

	}

}
