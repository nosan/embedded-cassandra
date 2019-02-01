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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.util.ArchiveUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

/**
 * {@link DirectoryCustomizer} to initialize a {@code directory} from an {@link Artifact}.
 *
 * @author Dmytro Nosan
 * @since 1.3.0
 */
class ArtifactCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(ArtifactCustomizer.class);

	@Nonnull
	private final ArtifactFactory artifactFactory;

	@Nonnull
	private final Path artifactDirectory;

	/**
	 * Creates an {@link ArtifactCustomizer}.
	 *
	 * @param artifactFactory a factory to create {@link Artifact}
	 * @param artifactDirectory a directory to extract an {@link Artifact} (must be writable)
	 */
	ArtifactCustomizer(@Nonnull ArtifactFactory artifactFactory, @Nonnull Path artifactDirectory) {
		this.artifactFactory = artifactFactory;
		this.artifactDirectory = artifactDirectory;
	}

	@Override
	public void customize(@Nonnull Path directory, @Nonnull Version version) throws IOException {
		Path artifactDirectory = this.artifactDirectory;
		Artifact artifact = this.artifactFactory.create(version);
		Objects.requireNonNull(artifact, "Artifact must not be null");
		extractArtifact(artifact, artifactDirectory);
		copyArtifact(directory, artifactDirectory);
	}

	private static void extractArtifact(Artifact artifact, Path artifactDirectory) throws IOException {
		Path archive = artifact.get();
		log.info("Extract ({}) into ({}).", archive, artifactDirectory);
		try {
			ArchiveUtils.extract(archive, artifactDirectory, entry -> shouldExtract(artifactDirectory, entry));
		}
		catch (IOException ex) {
			throw new IOException(String.format("Artifact (%s) could not be extracted into (%s)",
					archive, artifactDirectory), ex);
		}
		log.info("({}) Archive has been extracted into ({})", archive, artifactDirectory);
	}

	private static void copyArtifact(Path workingDirectory, Path artifactDirectory) throws IOException {
		Path baseDir = determineBaseDir(artifactDirectory);
		log.info("Copy ({}) folder into ({}).", baseDir, workingDirectory);
		try {
			FileUtils.copy(baseDir, workingDirectory, file -> shouldCopy(baseDir, workingDirectory, file));
		}
		catch (IOException ex) {
			throw new IOException(String.format("Could not copy folder (%s) into (%s)", baseDir, workingDirectory), ex);
		}
		log.info("({}) Folder has been copied into ({})", baseDir, workingDirectory);
	}

	private static boolean shouldCopy(Path srcDir, Path destDir, Path srcFile) {
		Path destFile = destDir.resolve(srcDir.relativize(srcFile));
		if (!Files.exists(destFile)) {
			return true;
		}
		try {
			return Files.size(destFile) != Files.size(srcFile);
		}
		catch (IOException ex) {
			return true;
		}
	}

	private static boolean shouldExtract(Path destination, String entry) {
		if (Files.exists(destination.resolve(entry))) {
			return false;
		}
		int endIndex = entry.lastIndexOf('/');
		if (endIndex != -1) {
			for (String directory : entry.substring(0, endIndex).split("/")) {
				if (directory.equalsIgnoreCase("javadoc") || directory.equalsIgnoreCase("doc")) {
					return false;
				}
			}
		}
		return true;
	}

	private static Path determineBaseDir(Path directory) throws IOException {
		Set<Path> directories = Files.find(directory, 3, (path, attributes) -> {
			Path bin = path.resolve("bin");
			Path lib = path.resolve("lib");
			Path conf = path.resolve("conf");
			return Files.exists(bin.resolve("cassandra")) &&
					Files.exists(bin.resolve("cassandra.ps1")) &&
					Files.exists(conf.resolve("cassandra.yaml")) &&
					Files.exists(lib);
		}).collect(Collectors.toSet());

		if (directories.isEmpty()) {
			throw new IllegalStateException(
					String.format("(%s) doesn't have one of the 'bin', lib', 'conf' folders", directory));
		}
		if (directories.size() > 1) {
			throw new IllegalStateException(String.format(
					"Impossible to determine a base directory. There are (%s) candidates : (%s)",
					directories.size(), directories));

		}
		return directories.iterator().next();
	}
}
