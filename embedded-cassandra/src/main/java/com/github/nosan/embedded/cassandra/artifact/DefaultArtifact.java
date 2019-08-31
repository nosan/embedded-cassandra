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

package com.github.nosan.embedded.cassandra.artifact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.DefaultPathSupplier;
import com.github.nosan.embedded.cassandra.commons.PathSupplier;

/**
 * An artifact that provides a {@link Artifact.Resource} based on the specified directory and version.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class DefaultArtifact implements Artifact {

	private final Version version;

	private final PathSupplier directorySupplier;

	/**
	 * Constructs a new {@link DefaultArtifact} with the specified version and directory.
	 *
	 * @param version the version
	 * @param directory the directory ({@code Cassandra's home directory})
	 */
	public DefaultArtifact(Version version, Path directory) {
		this(version, new DefaultPathSupplier(directory));
	}

	/**
	 * Constructs a new {@link DefaultArtifact} with the specified version and directory.
	 *
	 * @param version the version
	 * @param directorySupplier the directory ({@code Cassandra's home directory}) supplier
	 */
	public DefaultArtifact(Version version, PathSupplier directorySupplier) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
		this.directorySupplier = Objects.requireNonNull(directorySupplier, "'directorySupplier' must not be null");
	}

	/**
	 * Returns Cassandra's version.
	 *
	 * @return the version
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Returns the directory supplier.
	 *
	 * @return the directory ({@code Cassandra's home directory}) supplier
	 */
	public PathSupplier getDirectorySupplier() {
		return this.directorySupplier;
	}

	@Override
	public Resource getResource() throws Exception {
		Path directory = this.directorySupplier.get();
		if (directory == null) {
			throw new IllegalStateException("Directory must not be null");
		}
		Version version = this.version;
		Path cassandraHome = findCassandraHome(directory);

		return new Resource() {

			@Override
			public Path getDirectory() {
				return cassandraHome;
			}

			@Override
			public Version getVersion() {
				return version;
			}
		};
	}

	private static Path findCassandraHome(Path directory) throws IOException {
		try (Stream<Path> stream = Files.find(directory, 1, DefaultArtifact::isCassandraHome)) {
			Set<Path> directories = stream.collect(Collectors.toSet());
			if (directories.isEmpty()) {
				throw new IllegalStateException(
						String.format("'%s' does not have the Apache Cassandra files", directory));
			}
			if (directories.size() > 1) {
				throw new IllegalStateException(String.format(
						"Impossible to determine the Apache Cassandra directory. There are '%s' candidates  '%s'",
						directories.size(), directories));
			}
			return directories.iterator().next();
		}
	}

	private static boolean isCassandraHome(Path path, BasicFileAttributes attributes) {
		if (attributes.isDirectory()) {
			return Files.isDirectory(path.resolve("bin")) && Files.isDirectory(path.resolve("lib")) && Files
					.isDirectory(path.resolve("conf")) && Files.isRegularFile(
					path.resolve("conf").resolve("cassandra.yaml"));
		}
		return false;
	}

}
