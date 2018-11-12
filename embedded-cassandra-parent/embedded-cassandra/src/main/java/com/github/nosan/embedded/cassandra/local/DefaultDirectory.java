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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.util.ArchiveUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

/**
 * Default implementation of the {@link Directory}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class DefaultDirectory implements Directory {

	private static final Logger log = LoggerFactory.getLogger(DefaultDirectory.class);

	private static final Set<String> SKIP_CANDIDATES =
			Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("doc", "tools", "pylib", "javadoc")));

	@Nonnull
	private final Path directory;

	@Nonnull
	private final Artifact artifact;

	@Nonnull
	private final List<? extends DirectoryCustomizer> customizers;

	/**
	 * Creates a {@link DefaultDirectory}.
	 *
	 * @param artifact artifact to initialize a directory
	 * @param directory the working directory
	 * @param customizers the directory customizers
	 */
	DefaultDirectory(@Nonnull Path directory, @Nonnull Artifact artifact,
			@Nonnull List<? extends DirectoryCustomizer> customizers) {
		this.directory = directory;
		this.artifact = artifact;
		this.customizers = Collections.unmodifiableList(new ArrayList<>(customizers));
	}


	@Nonnull
	public Path initialize() throws IOException {
		Path archive = this.artifact.get();
		Path rootDirectory = this.directory;
		log.debug("Initialize working directory ({})", rootDirectory);
		try {
			log.info("Extract ({}) into ({}). It takes a while...", archive, rootDirectory);
			ArchiveUtils.extract(archive, rootDirectory, path -> {
				Path subPath = path.subpath(rootDirectory.getNameCount(), path.getNameCount());
				for (int i = 0; i < subPath.getNameCount(); i++) {
					String name = String.valueOf(subPath.getName(i));
					if (SKIP_CANDIDATES.contains(name)) {
						return false;
					}
				}
				return true;
			});
		}
		catch (Exception ex) {
			throw new IOException(
					String.format("Archive : (%s) could not be extracted into (%s)", archive, rootDirectory), ex);
		}
		Path directory = getDirectory(this.directory);
		for (DirectoryCustomizer customizer : this.customizers) {
			customizer.customize(directory);
		}

		return directory;
	}


	@Override
	public void destroy() throws IOException {
		if (FileUtils.isTemporary(this.directory)) {
			log.debug("Delete recursively working directory ({})", this.directory);
			FileUtils.delete(this.directory);
		}
	}


	@Override
	@Nonnull
	public String toString() {
		return String.valueOf(this.directory);
	}

	private static Path getDirectory(Path rootDirectory) throws IOException {
		List<Path> candidates = Files.find(rootDirectory, 5, DefaultDirectory::isMatch)
				.map(Path::getParent)
				.filter(Objects::nonNull)
				.map(Path::getParent)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (candidates.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("(%s) doesn't have a 'bin/cassandra' file.", rootDirectory));
		}
		if (candidates.size() > 1) {
			throw new IllegalStateException(String.format(
					"Impossible to determine a base directory. There are (%s) candidates : (%s)",
					candidates.size(), candidates));

		}
		return candidates.get(0);
	}

	private static boolean isMatch(Path candidate, BasicFileAttributes attributes) {
		Path parent = candidate.getParent();
		if (parent == null) {
			return false;
		}
		return String.valueOf(parent.getFileName()).equals("bin") &&
				String.valueOf(candidate.getFileName()).equals("cassandra");
	}

}
