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

package com.github.nosan.embedded.cassandra.commons.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * {@link Resource} implementation for archives.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class ArchiveResource implements Resource {

	private final Resource resource;

	private final ArchiveStream archiveStream;

	/**
	 * Constructs a new {@link ArchiveResource} with the specified {@link Resource}.
	 *
	 * @param resource the {@link Resource}
	 */
	public ArchiveResource(Resource resource) {
		this.resource = Objects.requireNonNull(resource, "'resource' must not be null");
		this.archiveStream = ArchiveStreams.create(resource);
	}

	/**
	 * Performs the given {@code callback} for each {@link ArchiveEntry}.
	 *
	 * @param callback The action to be performed for each {@link ArchiveEntry}.
	 * @throws IOException if an I/O error occurs or the resource does not exist
	 */
	public void forEach(ArchiveEntryCallback callback) throws IOException {
		Objects.requireNonNull(callback, "'callback' must not be null");
		try (ArchiveInputStream is = getInputStream()) {
			ArchiveEntry entry;
			while ((entry = is.getNextEntry()) != null) {
				callback.accept(entry, is);
			}
		}
	}

	/**
	 * Extracts this {@code Resource} into the given destination directory.
	 *
	 * @param destination the directory to which to extract the files
	 * @throws IOException if an I/O error occurs or the resource does not exist
	 */
	public void extract(Path destination) throws IOException {
		Objects.requireNonNull(destination, "'destination' must not be null");
		forEach((entry, stream) -> {
			if (entry.isDirectory()) {
				Path directory = destination.resolve(entry.getName());
				Files.createDirectories(directory);
			}
			else {
				Path file = destination.resolve(entry.getName());
				Path directory = file.getParent();
				if (directory != null && !Files.exists(directory)) {
					Files.createDirectories(directory);
				}
				Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
			}
		});

	}

	/**
	 * Returns the underlying resource.
	 *
	 * @return the underlying resource.
	 */
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public String getFileName() {
		return this.resource.getFileName();
	}

	@Override
	public boolean exists() {
		return this.resource.exists();
	}

	@Override
	public URL toURL() throws IOException {
		return this.resource.toURL();
	}

	@Override
	public ArchiveInputStream getInputStream() throws IOException {
		InputStream is = this.resource.getInputStream();
		try {
			return this.archiveStream.open(is);
		}
		catch (CompressorException | ArchiveException ex) {
			try {
				is.close();
			}
			catch (Exception swallow) {
				ex.addSuppressed(swallow);
			}
			throw new IOException(ex);
		}
	}

	@Override
	public URI toURI() throws IOException {
		return this.resource.toURI();
	}

	@Override
	public Path toPath() throws IOException, FileSystemNotFoundException {
		return this.resource.toPath();
	}

	@Override
	public File toFile() throws IOException {
		return this.resource.toFile();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		ArchiveResource that = (ArchiveResource) other;
		return this.resource.equals(that.resource);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ArchiveResource.class.getSimpleName() + "[", "]")
				.add("resource=" + this.resource).toString();
	}

	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}

	/**
	 * Callback that accepts {@link ArchiveEntry} and {@link ArchiveInputStream}.
	 */
	public interface ArchiveEntryCallback {

		/**
		 * Performs this operation on the given {@link ArchiveEntry} and {@link ArchiveInputStream}.
		 *
		 * @param entry archive entry
		 * @param stream the stream
		 * @throws IOException if an I/O error occurs
		 */
		void accept(ArchiveEntry entry, ArchiveInputStream stream) throws IOException;

	}

	@FunctionalInterface
	private interface ArchiveStream {

		ArchiveInputStream open(InputStream is) throws ArchiveException, CompressorException;

	}

	private static final class ArchiveStreams {

		private static final Map<String, ArchiveStream> STREAMS;

		static {
			Map<String, ArchiveStream> streams = new LinkedHashMap<>();
			streams.put(".tar.gz", create(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
			streams.put(".tar.bz2", create(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
			streams.put(".tgz", create(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
			streams.put(".tbz2", create(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
			streams.put(".7z", create(ArchiveStreamFactory.SEVEN_Z));
			streams.put(".a", create(ArchiveStreamFactory.AR));
			streams.put(".ar", create(ArchiveStreamFactory.AR));
			streams.put(".arj", create(ArchiveStreamFactory.ARJ));
			streams.put(".cpio", create(ArchiveStreamFactory.CPIO));
			streams.put(".dump", create(ArchiveStreamFactory.DUMP));
			streams.put(".jar", create(ArchiveStreamFactory.JAR));
			streams.put(".tar", create(ArchiveStreamFactory.TAR));
			streams.put(".zip", create(ArchiveStreamFactory.ZIP));
			streams.put(".zipx", create(ArchiveStreamFactory.ZIP));
			STREAMS = Collections.unmodifiableMap(streams);
		}

		static ArchiveStream create(Resource resource) {
			String name = Objects.toString(resource.getFileName(), "");
			for (Map.Entry<String, ArchiveStream> entry : STREAMS.entrySet()) {
				if (name.endsWith(entry.getKey())) {
					return entry.getValue();
				}
			}
			throw new IllegalArgumentException("Archive Type for '" + resource + "' cannot be detected");
		}

		private static ArchiveStream create(String archiveType, String compressorType) {
			return is -> {
				ArchiveStreamFactory af = new ArchiveStreamFactory();
				CompressorStreamFactory csf = new CompressorStreamFactory();
				return af.createArchiveInputStream(archiveType, csf.createCompressorInputStream(compressorType, is));
			};
		}

		private static ArchiveStream create(String archiveType) {
			return is -> {
				ArchiveStreamFactory af = new ArchiveStreamFactory();
				return af.createArchiveInputStream(archiveType, is);
			};
		}

	}

}
