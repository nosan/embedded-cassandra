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
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * {@link Resource} implementation for archives.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class ArchiveResource implements Resource {

	private final Resource resource;

	private final Handler handler;

	/**
	 * Constructs a new {@link ArchiveResource} with the specified {@link Resource}.
	 *
	 * @param resource the {@link Resource}
	 */
	public ArchiveResource(Resource resource) {
		this.resource = Objects.requireNonNull(resource, "'resource' must not be null");
		this.handler = Handler.detect(resource);
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
		try {
			return (ArchiveInputStream) this.handler.open(this.resource.getInputStream());
		}
		catch (CompressorException | ArchiveException ex) {
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
	 * Performs the given {@code callback} for each {@link ArchiveEntry}.
	 *
	 * @param callback The action to be performed for each {@link ArchiveEntry}.
	 * @throws IOException if an I/O error occurs or the resource does not exist
	 */
	public void forEach(ArchiveEntryConsumer callback) throws IOException {
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
	 * Consumer that accepts {@link ArchiveEntry} and {@link ArchiveInputStream}.
	 */
	public interface ArchiveEntryConsumer {

		/**
		 * Performs this operation on the given {@link ArchiveEntry} and {@link ArchiveInputStream}.
		 *
		 * @param entry archive entry
		 * @param stream the stream
		 * @throws IOException if an I/O error occurs
		 */
		void accept(ArchiveEntry entry, ArchiveInputStream stream) throws IOException;

	}

	private static final class Handler {

		private static final Map<String, FileType> FILE_TYPES;

		private static final Handler EMPTY = new Handler(new FileType(null, null), null);

		static {
			Map<String, FileType> types = new LinkedHashMap<>();
			types.put(".tgz", new FileType(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
			types.put(".tbz2", new FileType(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
			types.put(".7z", new FileType(ArchiveStreamFactory.SEVEN_Z, null));
			types.put(".a", new FileType(ArchiveStreamFactory.AR, null));
			types.put(".ar", new FileType(ArchiveStreamFactory.AR, null));
			types.put(".arj", new FileType(ArchiveStreamFactory.ARJ, null));
			types.put(".cpio", new FileType(ArchiveStreamFactory.CPIO, null));
			types.put(".dump", new FileType(ArchiveStreamFactory.DUMP, null));
			types.put(".jar", new FileType(ArchiveStreamFactory.JAR, null));
			types.put(".tar", new FileType(ArchiveStreamFactory.TAR, null));
			types.put(".zip", new FileType(ArchiveStreamFactory.ZIP, null));
			types.put(".zipx", new FileType(ArchiveStreamFactory.ZIP, null));
			types.put(".bz2", new FileType(null, CompressorStreamFactory.BZIP2));
			types.put(".gzip", new FileType(null, CompressorStreamFactory.GZIP));
			types.put(".gz", new FileType(null, CompressorStreamFactory.GZIP));
			types.put(".pack", new FileType(null, CompressorStreamFactory.PACK200));
			types.put(".xz", new FileType(null, CompressorStreamFactory.XZ));
			types.put(".z", new FileType(null, CompressorStreamFactory.Z));
			FILE_TYPES = Collections.unmodifiableMap(types);
		}

		private final FileType fileType;

		@Nullable
		private final Handler handler;

		private Handler(FileType fileType, @Nullable Handler handler) {
			this.fileType = fileType;
			this.handler = handler;
		}

		static Handler detect(Resource resource) {
			String name = resource.getFileName();
			if (!StringUtils.hasText(name)) {
				return EMPTY;
			}
			Handler handler = EMPTY;
			label:
			for (; ; ) {
				for (Map.Entry<String, FileType> entry : FILE_TYPES.entrySet()) {
					if (name.endsWith(entry.getKey())) {
						handler = new Handler(entry.getValue(), handler);
						name = name.substring(0, name.length() - entry.getKey().length());
						continue label;
					}
				}
				break;
			}
			if (handler.fileType.archive == null) {
				throw new IllegalArgumentException("Archive Type for '" + resource + "' cannot be detected");
			}
			return handler;
		}

		InputStream open(InputStream is) throws CompressorException, ArchiveException {
			return this.fileType.open((this.handler != null) ? this.handler.open(is) : is);
		}

		private static final class FileType {

			@Nullable
			private final String archive;

			@Nullable
			private final String compressor;

			private FileType(@Nullable String archive, @Nullable String compressor) {
				this.archive = archive;
				this.compressor = compressor;
			}

			InputStream open(InputStream is) throws CompressorException, ArchiveException {
				if (StringUtils.hasText(this.archive) && StringUtils.hasText(this.compressor)) {
					CompressorStreamFactory csf = new CompressorStreamFactory();
					ArchiveStreamFactory af = new ArchiveStreamFactory();
					return af.createArchiveInputStream(this.archive,
							csf.createCompressorInputStream(this.compressor, is));
				}
				if (StringUtils.hasText(this.archive)) {
					ArchiveStreamFactory af = new ArchiveStreamFactory();
					return af.createArchiveInputStream(this.archive, is);
				}
				if (StringUtils.hasText(this.compressor)) {
					CompressorStreamFactory csf = new CompressorStreamFactory();
					return csf.createCompressorInputStream(this.compressor, is);
				}
				return is;
			}

		}

	}

}
