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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
class BaseDirectory implements Directory {


	private static final Logger log = LoggerFactory.getLogger(Directory.class);

	@Nonnull
	private final Path rootDirectory;

	@Nullable
	private Path directory;


	/**
	 * Creates a {@link BaseDirectory}.
	 *
	 * @param directory the working directory
	 */
	BaseDirectory(@Nonnull Path directory) {
		this.rootDirectory = directory;
	}

	@Override
	public void initialize(@Nonnull Artifact artifact) throws Exception {
		Path archive = artifact.get();
		Path rootDirectory = this.rootDirectory;
		log.debug("Initialize ({})", rootDirectory);
		try {
			log.info("Extract ({}) into ({}). It takes a while...", archive, rootDirectory);
			ArchiveUtils.extract(archive, rootDirectory, path -> {
				for (int i = path.getNameCount() - 1; i >= rootDirectory.getNameCount(); i--) {
					String name = String.valueOf(path.getName(i));
					if ((name.equalsIgnoreCase("doc") || name.equalsIgnoreCase("javadoc"))) {
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
	}


	@Override
	public void destroy() throws Exception {
		if (FileUtils.isTemporary(this.rootDirectory)) {
			log.debug("Delete ({})", this.rootDirectory);
			this.directory = null;
			FileUtils.delete(this.rootDirectory);
		}
	}

	@Nonnull
	@Override
	public Path get() throws UncheckedIOException {
		try {
			if (this.directory == null) {
				this.directory = getDirectory(this.rootDirectory);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return Objects.requireNonNull(this.directory, "Directory is not initialized");

	}

	@Override
	@Nonnull
	public String toString() {
		return String.valueOf(this.rootDirectory);
	}

	private static Path getDirectory(Path rootDirectory) throws IOException {
		List<Path> candidates = Files.find(rootDirectory, Integer.MAX_VALUE, BaseDirectory::isMatch)
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
