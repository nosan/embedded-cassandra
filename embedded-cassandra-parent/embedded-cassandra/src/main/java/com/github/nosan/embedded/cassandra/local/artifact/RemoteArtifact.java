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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.FileUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.ThreadNameSupplier;

/**
 * {@link Artifact} implementation, that downloads {@code archive} from the internet, if {@code archive} doesn't exist
 * locally.
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
	private final ThreadFactory threadFactory = runnable -> {
		Thread thread = new Thread(runnable, this.threadNameSupplier.get());
		thread.setDaemon(true);
		return thread;
	};

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
		Version version = this.version;
		URL[] urls = this.urlFactory.create(version);
		Objects.requireNonNull(urls, "URLs must not be null");
		URLConnection urlConnection = getUrlConnection(this.proxy, this.readTimeout, this.connectTimeout, urls);
		String name = getName(urlConnection.getURL());
		long length = urlConnection.getContentLengthLong();
		Path artifact = this.directory.resolve(name);
		if (isDownloaded(artifact, length)) {
			return artifact;
		}
		Path tempArtifact = FileUtils.getTmpDirectory().resolve(UUID.randomUUID().toString()).resolve(name);
		Files.createDirectories(tempArtifact.getParent());
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(this.threadFactory);
		try (InputStream inputStream = urlConnection.getInputStream()) {
			long start = System.currentTimeMillis();
			log.info("Downloading Apache Cassandra ({}) from ({}).", version, urlConnection.getURL());
			executorService.scheduleAtFixedRate(() -> progress(tempArtifact, length), 0, 1500, TimeUnit.MILLISECONDS);
			Files.copy(inputStream, tempArtifact);
			long elapsed = System.currentTimeMillis() - start;
			log.info("Apache Cassandra ({}) has been downloaded ({} ms)", version, elapsed);
		}
		finally {
			executorService.shutdown();
		}
		if (isDownloaded(artifact, length)) {
			return artifact;
		}
		if (isDownloaded(tempArtifact, length)) {
			Files.createDirectories(artifact.getParent());
			try {
				return Files.move(tempArtifact, artifact, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException ex) {
				if (log.isDebugEnabled()) {
					log.error(String.format("Could not rename (%s) as (%s).", tempArtifact, artifact));
				}
				return tempArtifact;
			}
		}
		throw new IOException(String.format("Apache Cassandra (%s) is not downloaded from URLs (%s)",
				version, Arrays.toString(urls)));
	}

	private static URLConnection getUrlConnection(Proxy proxy, Duration readTimeout, Duration connectTimeout,
			URL... urls) throws IOException {
		IOException exception = new IOException(String.format("Could not create an URLConnection from URLs (%s)",
				Arrays.toString(urls)));
		for (URL url : urls) {
			try {
				return getUrlConnection(proxy, readTimeout, connectTimeout, url);
			}
			catch (IOException ex) {
				exception.addSuppressed(ex);
			}
		}
		throw exception;
	}

	private static URLConnection getUrlConnection(Proxy proxy, Duration readTimeout, Duration connectTimeout, URL url)
			throws IOException {
		URLConnection urlConnection = (proxy != null) ? url.openConnection(proxy) : url.openConnection();
		if (urlConnection instanceof HttpURLConnection) {
			HttpURLConnection connection = (HttpURLConnection) urlConnection;
			if (connectTimeout != null) {
				connection.setConnectTimeout(Math.toIntExact(connectTimeout.toMillis()));
			}
			if (readTimeout != null) {
				connection.setReadTimeout(Math.toIntExact(readTimeout.toMillis()));
			}
			switch (connection.getResponseCode()) {
				case 301:
				case 302:
				case 303:
				case 307:
				case 308:
					String location = connection.getHeaderField("Location");
					if (StringUtils.hasText(location)) {
						return getUrlConnection(proxy, readTimeout, connectTimeout, new URL(url, location));
					}
			}
		}
		return urlConnection;
	}

	private static String getName(URL url) {
		String name = url.getFile();
		if (StringUtils.hasText(name) && name.contains("/")) {
			name = name.substring(name.lastIndexOf("/") + 1);
		}
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException(
					String.format("There is no way to determine a file name from (%s)", url));
		}
		return name;
	}

	private static boolean isDownloaded(Path file, long length) {
		if (!Files.exists(file)) {
			return false;
		}
		if (length < 0) {
			return true;
		}
		try {
			return Files.size(file) == length;
		}
		catch (IOException ex) {
			if (log.isDebugEnabled()) {
				log.error(String.format("Could not compare content-length (%s) with a file (%s)", length, file), ex);
			}
		}
		return true;
	}

	private static void progress(Path file, long length) {
		if (length > 0 && Files.exists(file)) {
			try {
				long current = Files.size(file);
				log.info("Downloaded {} / {}  {}%", current, length, (current * 100) / length);
			}
			catch (IOException ignore) {
			}
		}
	}
}
