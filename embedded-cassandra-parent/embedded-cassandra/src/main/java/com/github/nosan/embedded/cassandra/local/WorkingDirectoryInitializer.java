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

import java.io.File;
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
import com.github.nosan.embedded.cassandra.util.ArchiveUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

/**
 * {@link Initializer} to initialize a {@code directory} with an {@link Artifact}.
 *
 * @author Dmytro Nosan
 * @since 1.3.0
 */
class WorkingDirectoryInitializer implements Initializer {

	private static final String ARTIFACT_FILE = String.format(".%s", Artifact.class.getName());

	private static final Logger log = LoggerFactory.getLogger(WorkingDirectoryInitializer.class);

	private final ArtifactFactory artifactFactory;

	private final Path artifactDirectory;

	/**
	 * Creates an {@link WorkingDirectoryInitializer}.
	 *
	 * @param artifactFactory a factory to create {@link Artifact}
	 * @param artifactDirectory a directory to extract an {@link Artifact} (must be writable)
	 */
	WorkingDirectoryInitializer(ArtifactFactory artifactFactory, Path artifactDirectory) {
		this.artifactFactory = artifactFactory;
		this.artifactDirectory = artifactDirectory;
	}

	@Override
	public void initialize(Path workingDirectory, Version version) throws IOException {
		Path artifactDirectory = this.artifactDirectory;
		if (!hasExtracted(artifactDirectory)) {
			extract(this.artifactFactory.create(version), artifactDirectory);
		}
		copy(getSingleCandidate(artifactDirectory), workingDirectory);
	}

	private static void extract(Artifact artifact, Path dest) throws IOException {
		Objects.requireNonNull(artifact, "Artifact must not be null");
		Path src = artifact.get();
		log.info("Extract ({}) into the ({}).", src, dest);
		try {
			ArchiveUtils.extract(src, dest);
		}
		catch (IOException ex) {
			throw new IOException(String.format("Artifact (%s) could not be extracted into the (%s)", src, dest), ex);
		}
		createHiddenFile(getSingleCandidate(dest).resolve(ARTIFACT_FILE));
		log.info("({}) archive has been extracted into the ({})", src, dest);
	}

	private static void copy(Path src, Path dest) throws IOException {
		log.info("Copy ({}) folder into the ({}).", src, dest);
		try {
			FileUtils.copy(src, dest, path -> shouldCopy(src, path));
		}
		catch (IOException ex) {
			throw new IOException(String.format("Could not copy folder (%s) into the (%s)", src, dest), ex);
		}
		log.info("({}) folder has been copied into the ({})", src, dest);
	}

	private static void createHiddenFile(Path file) throws IOException {
		try {
			Files.createFile(file);
			if (isWindows()) {
				Files.setAttribute(file, "dos:hidden", true);
			}
		}
		catch (FileAlreadyExistsException ignored) {
		}
	}

	private static boolean shouldCopy(Path src, Path srcPath) {
		if (Files.isDirectory(srcPath)) {
			String name = src.relativize(srcPath).getName(0).toString().toLowerCase(Locale.ENGLISH);
			return !name.equals("javadoc") && !name.equals("doc");
		}
		return true;
	}

	private static boolean hasExtracted(Path directory) {
		try {
			return Files.exists(getSingleCandidate(directory).resolve(ARTIFACT_FILE));
		}
		catch (Exception ex) {
			return false;
		}
	}

	private static Path getSingleCandidate(Path directory) throws IOException {
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
						String.format("(%s) does not have the Apache Cassandra files.", directory));
			}

			if (directories.size() > 1) {
				throw new IllegalStateException(
						String.format("Impossible to determine the Apache Cassandra directory." +
								" There are (%s) candidates : (%s)", directories.size(), directories));
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

	private static boolean isWindows() {
		return File.separatorChar == '\\';
	}

}
