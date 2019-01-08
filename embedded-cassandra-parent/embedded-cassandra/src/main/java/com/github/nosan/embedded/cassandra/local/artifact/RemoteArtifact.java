/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.FileUtils;
import com.github.nosan.embedded.cassandra.util.MDCUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.ThreadNameSupplier;

/**
 * {@link Artifact} which implements a remote {@code archive}. It checks if {@code archive} doesn't exist locally, it
 * tries to download it using {@link UrlFactory#create(Version) URL} and store locally. Artifact name generates based
 * on {@code URL}.
 *
 * @author Dmytro Nosan
 * @see RemoteArtifactFactory
 * @since 1.0.0
 */
class RemoteArtifact implements Artifact {

	private static final Logger log = LoggerFactory.getLogger(Artifact.class);

	private static final AtomicLong instanceCounter = new AtomicLong();

	@Nonnull
	private final ThreadNameSupplier threadNameSupplier = new ThreadNameSupplier(String.format("artifact-%d",
			instanceCounter.incrementAndGet()));

	@Nonnull
	private final ThreadFactory threadFactory = runnable -> new Thread(runnable, this.threadNameSupplier.get());

	@Nonnull
	private final Version version;

	@Nonnull
	private final Path directory;

	@Nonnull
	private final UrlFactory urlFactory;

	@Nullable
	private final Proxy proxy;

	@Nullable
	private final Duration readTimeout;

	@Nullable
	private final Duration connectTimeout;

	/**
	 * Creates a {@link RemoteArtifact}.
	 *
	 * @param version a version
	 * @param directory a directory to store/search an artifact (directory must be writable)
	 * @param urlFactory factory to create {@code URL}
	 * @param proxy proxy for {@code connection}
	 * @param connectTimeout connect timeout for {@code connection}
	 * @param readTimeout read timeout for {@code connection}
	 */
	RemoteArtifact(@Nonnull Version version, @Nonnull Path directory,
			@Nonnull UrlFactory urlFactory, @Nullable Proxy proxy,
			@Nullable Duration readTimeout, @Nullable Duration connectTimeout) {
		this.version = Objects.requireNonNull(version, "Version must not be null");
		this.directory = Objects.requireNonNull(directory, "Directory must not be null");
		this.urlFactory = Objects.requireNonNull(urlFactory, "URL Factory must not be null");
		this.proxy = proxy;
		this.readTimeout = readTimeout;
		this.connectTimeout = connectTimeout;
	}

	@Override
	@Nonnull
	public Path get() throws IOException {
		URL[] urls = this.urlFactory.create(this.version);
		Objects.requireNonNull(urls, "URLs must not be null");
		if (urls.length == 0) {
			throw new IOException("URLs must not be empty");
		}
		if (!Files.exists(this.directory)) {
			Files.createDirectories(this.directory);
		}
		if (!Files.isDirectory(this.directory)) {
			throw new IllegalArgumentException(
					String.format("(%s) exists and is a file, directory or path expected.", this.directory));
		}
		if (!Files.isWritable(this.directory)) {
			throw new IllegalArgumentException(String.format("(%s) is not writable", this.directory));
		}

		IOException exception = null;

		for (URL url : urls) {
			try {
				String name = getName(url);
				Path target = this.directory.resolve(name);
				if (!Files.exists(target)) {
					if (log.isDebugEnabled()) {
						log.debug("({}) doesn't exist, it will be downloaded from ({})", target, url);
					}
					URLConnection urlConnection = getUrlConnection(url);
					long contentLength = urlConnection.getContentLengthLong();
					Path source = download(name, urlConnection, contentLength);
					try {
						if (!Files.exists(target) && isDownloaded(source, contentLength)) {
							if (target.getParent() != null) {
								Files.createDirectories(target.getParent());
							}
							return Files.move(source, target);
						}
						return source;
					}
					catch (IOException ex) {
						log.error(String.format("Could not rename (%s) as (%s)", source, target), ex);
						return source;
					}
				}

				return target;
			}
			catch (IOException ex) {
				if (exception == null) {
					exception = ex;
				}
				else {
					exception.addSuppressed(ex);
				}
			}
		}
		throw exception;
	}

	private String getName(URL url) {
		String file = url.getFile();
		if (StringUtils.hasText(file) && file.contains("/")) {
			file = file.substring(file.lastIndexOf("/") + 1);
		}
		if (!StringUtils.hasText(file)) {
			throw new IllegalArgumentException(
					String.format("There is no way to determine a file name from (%s)", url));
		}
		return file;
	}

	private boolean isDownloaded(Path file, long contentLength) {
		if (contentLength > 0) {
			try {
				return contentLength == Files.size(file);
			}
			catch (Exception ex) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Could not compare content length (%s) with file (%s)",
							contentLength, file), ex);
				}
			}
		}
		return !Thread.currentThread().isInterrupted();
	}

	private URLConnection getUrlConnection(URL url) throws IOException {
		URLConnection urlConnection = (this.proxy != null) ? url.openConnection(this.proxy) : url.openConnection();
		if (urlConnection instanceof HttpURLConnection) {
			HttpURLConnection connection = (HttpURLConnection) urlConnection;
			if (this.connectTimeout != null) {
				connection.setConnectTimeout(Math.toIntExact(this.connectTimeout.toMillis()));
			}
			if (this.readTimeout != null) {
				connection.setReadTimeout(Math.toIntExact(this.readTimeout.toMillis()));
			}
			connection.setInstanceFollowRedirects(false);
			switch (connection.getResponseCode()) {
				case 301:
				case 302:
				case 303:
				case 307:
				case 308:
					String location = connection.getHeaderField("Location");
					if (StringUtils.hasText(location)) {
						return getUrlConnection(new URL(url, location));
					}
			}
		}
		return urlConnection;
	}

	private Path download(String name, URLConnection urlConnection, long contentLength) throws IOException {
		URL url = urlConnection.getURL();
		Path tempFile = FileUtils.getTmpDirectory()
				.resolve(String.format("%s-%s", UUID.randomUUID(), name));
		Files.createFile(tempFile);
		try {
			tempFile.toFile().deleteOnExit();
		}
		catch (Throwable ex) {
			log.error(String.format("Shutdown hook is not registered for (%s)", tempFile), ex);
		}
		try (FileChannel fileChannel = new FileOutputStream(tempFile.toFile()).getChannel();
				ReadableByteChannel urlChannel = Channels.newChannel(urlConnection.getInputStream())) {
			log.info("Downloading Cassandra from ({}). It takes a while...", url);
			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(this.threadFactory);
			try {
				showProgress(contentLength, tempFile, scheduledExecutor);
				fileChannel.transferFrom(urlChannel, 0, Long.MAX_VALUE);
			}
			finally {
				scheduledExecutor.shutdown();
			}
		}
		catch (IOException ex) {
			throw new IOException(String.format("Could not download Cassandra from (%s)", url), ex);
		}
		log.info("Cassandra ({}) has been downloaded", this.version);
		return tempFile;
	}

	private void showProgress(long contentLength, Path file, ScheduledExecutorService scheduledExecutor) {
		if (contentLength > 0) {
			Map<String, String> context = MDCUtils.getContext();
			scheduledExecutor.scheduleAtFixedRate(() -> {
				MDCUtils.setContext(context);
				try {
					long current = Files.size(file);
					log.info("Downloaded {} / {}  {}%", current, contentLength,
							(current * 100) / contentLength);
				}
				catch (Exception ignore) {
				}
			}, 0, 3, TimeUnit.SECONDS);
		}
	}
}
