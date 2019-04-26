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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;

/**
 * An initializer that initialize a {@code working directory} with an {@link Artifact}.
 *
 * @author Dmytro Nosan
 * @since 1.3.0
 */
class ArtifactWorkingDirectoryInitializer {

	private static final Logger log = LoggerFactory.getLogger(ArtifactWorkingDirectoryInitializer.class);

	private final ArtifactFactory artifactFactory;

	private final Path artifactDirectory;

	ArtifactWorkingDirectoryInitializer(ArtifactFactory artifactFactory, Path artifactDirectory) {
		this.artifactFactory = artifactFactory;
		this.artifactDirectory = artifactDirectory;
	}

	/**
	 * Initializes the working directory with an {@link Artifact}.
	 *
	 * @param version a version
	 * @param workingDirectory the working directory that should be initialized
	 * @throws IOException in the case of I/O errors
	 */
	void initialize(Path workingDirectory, Version version) throws IOException {
		Path artifactDirectory = this.artifactDirectory;
		String artifactName = getArtifactName(version);
		if (hasNotExtracted(artifactDirectory, artifactName)) {
			Files.createDirectories(artifactDirectory);
			Path lockFile = artifactDirectory.resolve(String.format("%s.lock", artifactName));
			try (FileLock fileLock = new FileLock(lockFile)) {
				fileLock.lock();
				if (hasNotExtracted(artifactDirectory, artifactName)) {
					extract(this.artifactFactory.create(version), artifactDirectory, artifactName);
				}
			}
		}
		copy(requireSingleDirectory(artifactDirectory), workingDirectory, artifactName);
	}

	private static void extract(Artifact artifact, Path artifactDirectory, String artifactName) throws IOException {
		Objects.requireNonNull(artifact, "Artifact must not be null");
		Path archiveFile = artifact.getArchive();
		log.info("Extract '{}' into the '{}'.", archiveFile, artifactDirectory);
		ArchiveUtils.extract(archiveFile, artifactDirectory);
		requireSingleDirectory(artifactDirectory);
		createFile(artifactDirectory.resolve(artifactName));
		log.info("'{}' archive is extracted into the '{}'", archiveFile, artifactDirectory);
	}

	private static void copy(Path artifactDirectory, Path workingDirectory, String artifactName) throws IOException {
		log.info("Copy '{}' folder into the '{}'.", artifactDirectory, workingDirectory);
		Files.createDirectories(workingDirectory);
		FileUtils.copy(artifactDirectory, workingDirectory, path -> shouldCopy(artifactDirectory, path, artifactName));
		log.info("'{}' folder is copied into the '{}'", artifactDirectory, workingDirectory);
	}

	private static boolean shouldCopy(Path src, Path srcPath, String artifactName) {
		if (Files.isDirectory(srcPath)) {
			String name = src.relativize(srcPath).getName(0).toString().toLowerCase(Locale.ENGLISH);
			return !name.equals("javadoc") && !name.equals("doc");
		}
		return !artifactName.equals(srcPath.getFileName().toString());
	}

	private static boolean hasNotExtracted(Path directory, String artifactName) {
		try {
			return !Files.exists(directory.resolve(artifactName));
		}
		catch (Exception ex) {
			return true;
		}
	}

	private static Path requireSingleDirectory(Path directory) throws IOException {
		try (Stream<Path> stream = Files.find(directory, 1, (path, attributes) -> {
			Path bin = path.resolve("bin");
			Path lib = path.resolve("lib");
			Path conf = path.resolve("conf");
			Path configuration = conf.resolve("cassandra.yaml");
			return getCount(bin) > 0 && getCount(lib) > 0 && getCount(conf) > 0 && Files.exists(configuration);
		})) {

			Set<Path> directories = stream.collect(Collectors.toSet());

			if (directories.isEmpty()) {
				throw new IllegalStateException(
						String.format("'%s' does not have the Apache Cassandra files.", directory));
			}

			if (directories.size() > 1) {
				throw new IllegalStateException(String.format(
						"Impossible to determine the Apache Cassandra directory. There are '%s' candidates : '%s'",
						directories.size(), directories));
			}

			return directories.iterator().next();
		}
	}

	private static long getCount(Path path) {
		try (Stream<Path> list = Files.list(path)) {
			return list.count();
		}
		catch (IOException ex) {
			return 0;
		}
	}

	private static String getArtifactName(Version version) {
		return String.format(".artifact.%s", version);
	}

	private static void createFile(Path file) throws IOException {
		try {
			Files.createFile(file);
		}
		catch (FileAlreadyExistsException ignored) {
		}
	}

}
