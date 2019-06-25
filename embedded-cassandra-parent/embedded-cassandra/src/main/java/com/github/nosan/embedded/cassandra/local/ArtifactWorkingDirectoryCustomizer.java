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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
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
 * {@link WorkingDirectoryCustomizer} that initialize a {@code working directory} using {@link Artifact}.
 *
 * @author Dmytro Nosan
 * @since 1.3.0
 */
class ArtifactWorkingDirectoryCustomizer implements WorkingDirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(ArtifactWorkingDirectoryCustomizer.class);

	private final ArtifactFactory artifactFactory;

	private final Path artifactDirectory;

	ArtifactWorkingDirectoryCustomizer(ArtifactFactory artifactFactory, Path artifactDirectory) {
		this.artifactFactory = artifactFactory;
		this.artifactDirectory = artifactDirectory;
	}

	@Override
	public void customize(Path workingDirectory, Version version) throws IOException {
		Path artifactDirectory = this.artifactDirectory;
		Path artifactFile = artifactDirectory.resolve(String.format("artifact.%s", version));
		if (!Files.exists(artifactFile)) {
			Files.createDirectories(artifactDirectory);
			Path lockFile = artifactDirectory.resolve(String.format("%s.lock", artifactFile.getFileName()));
			try (FileLock fileLock = new FileLock(lockFile)) {
				fileLock.lock();
				if (!Files.exists(artifactFile)) {
					extract(getArchive(version), artifactDirectory);
					findCassandraHome(artifactDirectory);
					createArtifactFile(artifactFile);
				}
			}
		}
		Path cassandraHome = findCassandraHome(artifactDirectory);
		copy(cassandraHome, workingDirectory);
	}

	private void createArtifactFile(Path artifactFile) throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(artifactFile, StandardOpenOption.CREATE)) {
			bw.write(ZonedDateTime.now().toString());
		}
	}

	private Path getArchive(Version version) throws IOException {
		Artifact artifact = this.artifactFactory.create(version);
		Objects.requireNonNull(artifact, "Artifact must not be null");
		Path archiveFile = artifact.getArchive();
		Objects.requireNonNull(archiveFile, "Archive File must not be null");
		return archiveFile;
	}

	private void extract(Path archiveFile, Path artifactDirectory) throws IOException {
		ArchiveUtils.extract(archiveFile, artifactDirectory);
		log.info("Archive '{}' was extracted into the '{}'", archiveFile, artifactDirectory);
	}

	private void copy(Path cassandraHome, Path workingDirectory) throws IOException {
		Files.createDirectories(workingDirectory);
		FileUtils.copy(cassandraHome, workingDirectory, this::skipDocs);
		if (log.isDebugEnabled()) {
			log.debug("Folder '{}' was recursively copied into the '{}'", cassandraHome, workingDirectory);
		}
	}

	private boolean skipDocs(Path path, BasicFileAttributes attributes) {
		if (attributes.isDirectory()) {
			String name = path.getFileName().toString().toLowerCase(Locale.ENGLISH);
			return !name.equals("javadoc") && !name.equals("doc");
		}
		return true;
	}

	private Path findCassandraHome(Path artifactDirectory) throws IOException {
		try (Stream<Path> stream = Files.find(artifactDirectory, 1, this::isCassandraHome)) {
			Set<Path> directories = stream.collect(Collectors.toSet());
			if (directories.isEmpty()) {
				throw new IllegalStateException(
						String.format("'%s' does not have the Apache Cassandra files.", artifactDirectory));
			}
			if (directories.size() > 1) {
				throw new IllegalStateException(String.format(
						"Impossible to determine the Apache Cassandra directory. There are '%s' candidates : '%s'",
						directories.size(), directories));
			}
			return directories.iterator().next();
		}
	}

	private boolean isCassandraHome(Path path, BasicFileAttributes attributes) {
		if (attributes.isDirectory()) {
			Path bin = path.resolve("bin");
			Path lib = path.resolve("lib");
			Path conf = path.resolve("conf");
			Path configuration = conf.resolve("cassandra.yaml");
			return Files.isDirectory(bin) && Files.isDirectory(lib) && Files.isDirectory(conf)
					&& Files.isRegularFile(configuration);
		}
		return false;
	}

}
