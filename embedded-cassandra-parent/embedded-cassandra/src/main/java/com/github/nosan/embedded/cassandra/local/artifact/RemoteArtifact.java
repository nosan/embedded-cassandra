/*
 * Copyright 2018-2018 the original author or authors.
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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.FileUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

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
				Path target = this.directory.resolve(getName(url));
				if (!Files.exists(target)) {
					log.debug("({}) doesn't exist, it will be downloaded from ({})", target, url);
					Path source = download(url);
					try {
						if (target.getParent() != null) {
							Files.createDirectories(target.getParent());
						}
						if (!Files.exists(target)) {
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
		throw Objects.requireNonNull(exception, "Exception must not be null.");

	}


	private Path download(URL url) throws IOException {
		URLConnection urlConnection = getUrlConnection(url);
		Path tempFile = FileUtils.getTmpDirectory()
				.resolve(String.format("%s-%s", UUID.randomUUID(), getName(url)));
		Files.createFile(tempFile);
		try {
			tempFile.toFile().deleteOnExit();
		}
		catch (Throwable ex) {
			log.error(String.format("Shutdown hook is not registered for (%s)", tempFile), ex);
		}
		long size = urlConnection.getContentLengthLong();
		try (FileChannel fileChannel = new FileOutputStream(tempFile.toFile()).getChannel();
				ReadableByteChannel urlChannel = Channels.newChannel(urlConnection.getInputStream())) {
			log.info("Downloading Cassandra from ({}). It takes a while...", urlConnection.getURL());
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			try {
				if (size > 0) {
					executorService.scheduleAtFixedRate(() -> {
						try {
							long current = Files.size(tempFile);
							log.info("Downloaded {} / {}  {}%", current, size, (current * 100) / size);
						}
						catch (IOException ignore) {
						}
					}, 0, 3, TimeUnit.SECONDS);
				}
				fileChannel.transferFrom(urlChannel, 0, Long.MAX_VALUE);
			}
			finally {
				executorService.shutdown();
			}
		}
		catch (IOException ex) {
			throw new IOException(String.format("Could not download Cassandra from (%s)", urlConnection.getURL()), ex);
		}

		log.info("Cassandra has been downloaded");
		return tempFile;
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
			connection.setInstanceFollowRedirects(true);
			switch (connection.getResponseCode()) {
				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
					String location = connection.getHeaderField("Location");
					if (StringUtils.hasText(location)) {
						return getUrlConnection(new URL(url, location));
					}
			}
		}
		return urlConnection;

	}


	private String getName(URL url) {
		String file = url.getFile();
		if (StringUtils.hasText(file) && file.lastIndexOf("/") != -1) {
			file = file.substring(file.lastIndexOf("/") + 1);
		}
		if (!StringUtils.hasText(file)) {
			throw new IllegalArgumentException(
					String.format("There is no way to determine a file name from (%s)", url));
		}
		return file;
	}
}
