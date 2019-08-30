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

package com.github.nosan.embedded.cassandra.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * Simple utility methods for dealing with archives.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class ArchiveUtils {

	private static final Map<String, FileType> archives;

	static {
		Map<String, FileType> candidates = new LinkedHashMap<>();
		candidates.put(".tar.gz", new FileType(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
		candidates.put(".tar.bz2", new FileType(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
		candidates.put(".tbz2", new FileType(ArchiveStreamFactory.TAR, CompressorStreamFactory.BZIP2));
		candidates.put(".tgz", new FileType(ArchiveStreamFactory.TAR, CompressorStreamFactory.GZIP));
		candidates.put(".jar", new FileType(ArchiveStreamFactory.JAR));
		candidates.put(".tar", new FileType(ArchiveStreamFactory.TAR));
		candidates.put(".zip", new FileType(ArchiveStreamFactory.ZIP));
		candidates.put(".zipx", new FileType(ArchiveStreamFactory.ZIP));
		archives = Collections.unmodifiableMap(candidates);
	}

	private ArchiveUtils() {
	}

	/**
	 * Extracts the source archive file into the given destination directory.
	 *
	 * @param archiveFile the archive file to extract
	 * @param destDir the directory to extract the files
	 * @throws IOException in the case of I/O errors
	 */
	public static void extract(Path archiveFile, Path destDir) throws IOException {
		Objects.requireNonNull(archiveFile, "'archiveFile' must not be null");
		Objects.requireNonNull(destDir, "'destDir' must not be null");
		try (ArchiveInputStream archiveStream = createArchiveStream(archiveFile)) {
			Files.createDirectories(destDir);
			ArchiveEntry entry;
			while ((entry = archiveStream.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					Path directory = destDir.resolve(entry.getName());
					Files.createDirectories(directory);
					FileModeUtils.set(entry, directory);
				}
				else {
					Path file = destDir.resolve(entry.getName());
					Path directory = file.getParent();
					if (directory != null && !Files.exists(directory)) {
						Files.createDirectories(directory);
					}
					Files.copy(archiveStream, file, StandardCopyOption.REPLACE_EXISTING);
					FileModeUtils.set(entry, file);
				}
			}
		}
		catch (ArchiveException | CompressorException ex) {
			throw new IOException(String.format("Can not create a stream for an archive '%s'", archiveFile), ex);
		}
	}

	private static ArchiveInputStream createArchiveStream(Path archiveFile)
			throws ArchiveException, CompressorException, IOException {
		for (Map.Entry<String, FileType> entry : archives.entrySet()) {
			if (archiveFile.getFileName().toString().endsWith(entry.getKey())) {
				return entry.getValue().create(archiveFile);
			}
		}
		throw new IllegalArgumentException(String.format("Archive File '%s' is not supported", archiveFile));
	}

	private abstract static class FileModeUtils {

		private static final int MASK = 511;

		private static final Map<Integer, PosixFilePermission> permissions;

		static {
			Map<Integer, PosixFilePermission> candidates = new LinkedHashMap<>();
			candidates.put(256, PosixFilePermission.OWNER_READ);
			candidates.put(128, PosixFilePermission.OWNER_WRITE);
			candidates.put(64, PosixFilePermission.OWNER_EXECUTE);
			candidates.put(32, PosixFilePermission.GROUP_READ);
			candidates.put(16, PosixFilePermission.GROUP_WRITE);
			candidates.put(8, PosixFilePermission.GROUP_EXECUTE);
			candidates.put(4, PosixFilePermission.OTHERS_READ);
			candidates.put(2, PosixFilePermission.OTHERS_WRITE);
			candidates.put(1, PosixFilePermission.OTHERS_EXECUTE);
			permissions = Collections.unmodifiableMap(candidates);
		}

		static void set(ArchiveEntry entry, Path path) {
			if (!SystemUtils.isWindows()) {
				long mode = getMode(entry) & MASK;
				if (mode > 0) {
					Set<PosixFilePermission> permissions = getPermissions(mode);
					try {
						Files.setPosixFilePermissions(path, permissions);
					}
					catch (Exception ex) {
						//ignore
					}
				}
			}
		}

		private static Set<PosixFilePermission> getPermissions(long mode) {
			return permissions.entrySet().stream().filter(entry -> (mode & entry.getKey()) > 0).map(Map.Entry::getValue)
					.collect(Collectors.toSet());
		}

		private static long getMode(ArchiveEntry entry) {
			if (entry instanceof TarArchiveEntry) {
				return ((TarArchiveEntry) entry).getMode();
			}
			else if (entry instanceof ZipArchiveEntry) {
				return ((ZipArchiveEntry) entry).getUnixMode();
			}
			return 0;
		}

	}

	private static final class FileType {

		private final String archiveType;

		@Nullable
		private final String compressorType;

		FileType(String archiveType, String compressorType) {
			this.archiveType = archiveType;
			this.compressorType = compressorType;
		}

		FileType(String archiveType) {
			this.archiveType = archiveType;
			this.compressorType = null;
		}

		ArchiveInputStream create(Path file) throws IOException, CompressorException, ArchiveException {
			ArchiveStreamFactory af = new ArchiveStreamFactory();
			CompressorStreamFactory csf = new CompressorStreamFactory();
			if (StringUtils.hasText(this.compressorType)) {
				return af.createArchiveInputStream(this.archiveType,
						csf.createCompressorInputStream(this.compressorType, Files.newInputStream(file)));
			}
			return af.createArchiveInputStream(this.archiveType, Files.newInputStream(file));
		}

	}

}
