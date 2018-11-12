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
 * Basic implementation of the {@link Directory}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class WorkingDirectory implements Directory {


	private static final Logger log = LoggerFactory.getLogger(Directory.class);

	private static final Set<String> SKIP_CANDIDATES;

	static {
		Set<String> candidates = new LinkedHashSet<>();
		candidates.add("doc");
		candidates.add("tools");
		candidates.add("pylib");
		candidates.add("javadoc");
		SKIP_CANDIDATES = Collections.unmodifiableSet(candidates);
	}

	@Nonnull
	private final Path rootDirectory;


	/**
	 * Creates a {@link WorkingDirectory}.
	 *
	 * @param directory the working directory
	 */
	WorkingDirectory(@Nonnull Path directory) {
		this.rootDirectory = directory;
	}

	@Override
	public Path initialize(@Nonnull Artifact artifact) throws Exception {
		Path archive = artifact.get();
		Path rootDirectory = this.rootDirectory;
		log.debug("Initialize working directory ({})", rootDirectory);
		try {
			log.info("Extract ({}) into ({}). It takes a while...", archive, rootDirectory);
			ArchiveUtils.extract(archive, rootDirectory, path -> {
				Path sub = path.subpath(rootDirectory.getNameCount(), path.getNameCount());
				for (int i = 0; i < sub.getNameCount(); i++) {
					String name = String.valueOf(sub.getName(i));
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
		return getDirectory(this.rootDirectory);
	}


	@Override
	public void destroy() throws Exception {
		if (FileUtils.isTemporary(this.rootDirectory)) {
			log.debug("Delete recursively working directory ({})", this.rootDirectory);
			FileUtils.delete(this.rootDirectory);
		}
	}


	@Override
	@Nonnull
	public String toString() {
		return String.valueOf(this.rootDirectory);
	}

	private static Path getDirectory(Path rootDirectory) throws IOException {
		List<Path> candidates = Files.find(rootDirectory, 5, WorkingDirectory::isMatch)
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
