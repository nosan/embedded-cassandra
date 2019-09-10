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

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.api.Version;

/**
 * Simple {@link Artifact.Distribution} implementation with a specified directory and version.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class DefaultDistribution implements Artifact.Distribution {

	private final Version version;

	private final Path directory;

	/**
	 * Constructs a new {@link DefaultDistribution} with the specified version and directory.
	 *
	 * @param version Cassandra's version
	 * @param directory Cassandra's directory
	 */
	public DefaultDistribution(Version version, Path directory) {
		this.version = Objects.requireNonNull(version, "'version' must not be null");
		this.directory = Objects.requireNonNull(directory, "'directory' must not be null");
	}

	@Override
	public Path getDirectory() {
		return this.directory;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", DefaultDistribution.class.getSimpleName() + "[", "]")
				.add("version=" + this.version)
				.add("directory=" + this.directory)
				.toString();
	}

}
