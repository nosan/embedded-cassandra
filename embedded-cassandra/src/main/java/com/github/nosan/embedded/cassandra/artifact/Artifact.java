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
import java.nio.file.Path;

import com.github.nosan.embedded.cassandra.api.Version;

/**
 * This interface provides a {@link Distribution}.
 *
 * @author Dmytro Nosan
 * @see DefaultArtifact
 * @see ArchiveArtifact
 * @see RemoteArtifact
 * @since 3.0.0
 */
@FunctionalInterface
public interface Artifact {

	/**
	 * Constructs a new {@link Artifact} with the specified version.
	 *
	 * @param version the version
	 * @return a new {@link Artifact}
	 */
	static Artifact ofVersion(String version) {
		return ofVersion(Version.of(version));
	}

	/**
	 * Constructs a new {@link Artifact} with the specified version.
	 *
	 * @param version the version
	 * @return a new {@link Artifact}
	 */
	static Artifact ofVersion(Version version) {
		return new RemoteArtifact(version);
	}

	/**
	 * Returns the {@link Distribution}.
	 *
	 * @return the distribution
	 * @throws IOException if an I/O error occurs
	 */
	Distribution getDistribution() throws IOException;

	/**
	 * This interface provides a path to {@code Cassandra's} directory and {@code Cassandra's} version.
	 */
	interface Distribution {

		/**
		 * Returns the path to Cassandra's directory.
		 *
		 * @return path to Cassandra's directory
		 */
		Path getDirectory();

		/**
		 * Returns Cassandra's version.
		 *
		 * @return the version
		 */
		Version getVersion();

	}

}
