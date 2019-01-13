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

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with archives.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.INTERNAL)
public abstract class ArchiveUtils {

	private static final Map<String, ArchiveFactory> ARCHIVES;

	static {
		Map<String, ArchiveFactory> candidates = new LinkedHashMap<>();
		candidates.put(".tar.gz", ArchiveFactory.create(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
		candidates.put(".tar.bz2", ArchiveFactory.create(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
		candidates.put(".tar.xz", ArchiveFactory.create(ArchiveStreamFactory.TAR, CompressorStreamFactory.XZ));
		candidates.put(".tbz2", ArchiveFactory.create(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
		candidates.put(".tgz", ArchiveFactory.create(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
		candidates.put(".txz", ArchiveFactory.create(ArchiveStreamFactory.TAR, CompressorStreamFactory.XZ));
		candidates.put(".a", ArchiveFactory.create(ArchiveStreamFactory.AR));
		candidates.put(".ar", ArchiveFactory.create(ArchiveStreamFactory.AR));
		candidates.put(".cpio", ArchiveFactory.create(ArchiveStreamFactory.CPIO));
		candidates.put(".jar", ArchiveFactory.create(ArchiveStreamFactory.JAR));
		candidates.put(".tar", ArchiveFactory.create(ArchiveStreamFactory.TAR));
		candidates.put(".zip", ArchiveFactory.create(ArchiveStreamFactory.ZIP));
		candidates.put(".zipx", ArchiveFactory.create(ArchiveStreamFactory.ZIP));
		ARCHIVES = Collections.unmodifiableMap(candidates);
	}

	/**
	 * Extracts the source archive file into the given destination directory.
	 * The destination is expected to be a writable directory.
	 *
	 * @param archive the archive file to extract
	 * @param destination the directory to which to extract the files
	 * @param include the filter to check whether {@code Path} should be included or not
	 * @throws IOException in the case of I/O errors
	 */
	public static void extract(@Nonnull Path archive, @Nonnull Path destination,
			@Nullable Predicate<? super Path> include)
			throws IOException {
		Objects.requireNonNull(archive, "Archive must not be null");
		Objects.requireNonNull(destination, "Destination must not be null");

		if (Files.isDirectory(archive)) {
			throw new IllegalArgumentException(String.format("Can not extract (%s) Source is a directory.",
					archive));
		}
		if (!Files.exists(archive)) {
			throw new IllegalArgumentException(String.format("Archive (%s) is not found", archive));
		}
		if (!Files.isReadable(archive)) {
			throw new IllegalArgumentException(
					String.format("Can not extract (%s). Can not read from  source.", archive));
		}
		if (!Files.exists(destination)) {
			Files.createDirectories(destination);
		}
		if (!Files.isDirectory(destination)) {
			throw new IllegalArgumentException(
					String.format("(%s) exists and is a file, directory or path expected.", destination));
		}
		if (!Files.isWritable(destination)) {
			throw new IllegalArgumentException(String.format("(%s) is not writable", destination));
		}

		ArchiveFactory archiveFactory = createArchiveFactory(archive);
		try (ArchiveInputStream stream = archiveFactory.create(archive)) {
			ArchiveEntry entry;
			while ((entry = stream.getNextEntry()) != null) {
				Path path = destination.resolve(entry.getName());
				if (include != null && !include.test(path)) {
					continue;
				}
				if (entry.isDirectory()) {
					Files.createDirectories(path);
				}
				else {
					if (path.getParent() != null) {
						Files.createDirectories(path.getParent());
					}
					Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
				}
				if (OS.get() != OS.WINDOWS) {
					FileModeUtils.set(entry, path);
				}
			}
		}
	}

	private static ArchiveFactory createArchiveFactory(Path source) {
		for (Map.Entry<String, ArchiveFactory> candidate : ARCHIVES.entrySet()) {
			if (String.valueOf(source.getFileName()).endsWith(candidate.getKey())) {
				return candidate.getValue();
			}
		}
		throw new IllegalArgumentException(String.format("Archive (%s) is not supported", source));
	}

	/**
	 * Factory for creating {@link ArchiveInputStream} instances by a given archive.
	 */
	private interface ArchiveFactory {

		/**
		 * Reads the given archive file as an {@link ArchiveInputStream} which is used to access individual
		 * {@link ArchiveEntry} objects within the archive without extracting the archive onto the file system.
		 *
		 * @param archive the archive file to stream
		 * @return a new archive stream for the given archive
		 * @throws IOException IOException in the case of I/O errors
		 */
		@Nonnull
		ArchiveInputStream create(@Nonnull Path archive) throws IOException;

		/**
		 * Creates a factory for the given archive format.
		 *
		 * @param archiveFormat the archiveFormat format e.g. "tar" or "zip"
		 * @return a new Factory instance that also handles compression
		 */
		@Nonnull
		static ArchiveFactory create(@Nonnull String archiveFormat) {
			return create(archiveFormat, null);
		}

		/**
		 * Creates a factory for the given archive format that uses compression.
		 *
		 * @param archiveFormat the archiveFormat format e.g. "tar" or "zip"
		 * @param compressionFormat the compression algorithm name e.g. "gz"
		 * @return a new Factory instance that also handles compression
		 */
		@Nonnull
		static ArchiveFactory create(@Nonnull String archiveFormat, @Nullable String compressionFormat) {
			return archive -> {
				CompressorStreamFactory cf = new CompressorStreamFactory();
				ArchiveStreamFactory af = new ArchiveStreamFactory();

				InputStream stream = null;
				CompressorInputStream cstream = null;

				try {
					stream = Files.newInputStream(archive);
					if (StringUtils.hasText(compressionFormat)) {
						cstream = cf.createCompressorInputStream(compressionFormat, stream);
					}
					return (cstream != null) ? af.createArchiveInputStream(archiveFormat, cstream)
							: af.createArchiveInputStream(archiveFormat, stream);
				}
				catch (IOException | ArchiveException | CompressorException ex) {
					IOUtils.closeQuietly(cstream);
					IOUtils.closeQuietly(stream);
					throw new IOException("Could not create a stream", ex);
				}
			};
		}
	}

	/**
	 * Utility class to set a file mode.
	 */
	private abstract static class FileModeUtils {

		private static final Logger log = LoggerFactory.getLogger(FileModeUtils.class);

		private static final int MASK = 511;

		/**
		 * Sets the file mode onto the given file.
		 *
		 * @param entry the archive entry that holds the mode
		 * @param file the file to apply the mode onto
		 */
		static void set(@Nonnull ArchiveEntry entry, @Nonnull Path file) {
			long mode = getMode(entry) & MASK;
			if (mode > 0) {
				try {
					List<String> cmd = Arrays.asList("chmod",
							Long.toOctalString(mode), String.valueOf(file.toAbsolutePath()));
					new ProcessBuilder(cmd).start();
				}
				catch (IOException ex) {
					log.error(String.format("Could not set a permission (%s) to (%s)", mode, file), ex);
				}
			}
		}

		private static long getMode(ArchiveEntry entry) {
			if (entry instanceof TarArchiveEntry) {
				return ((TarArchiveEntry) entry).getMode();
			}
			else if (entry instanceof ZipArchiveEntry) {
				return ((ZipArchiveEntry) entry).getUnixMode();
			}
			else if (entry instanceof CpioArchiveEntry) {
				return ((CpioArchiveEntry) entry).getMode();
			}
			else if (entry instanceof ArArchiveEntry) {
				return ((ArArchiveEntry) entry).getMode();
			}
			return 0;
		}
	}
}

