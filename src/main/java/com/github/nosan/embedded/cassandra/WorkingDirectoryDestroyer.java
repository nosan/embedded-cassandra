/*
 * Copyright 2020-2025 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.FileUtils;

/**
 * A strategy interface to destroy the working directory.
 *
 * <p>This interface allows implementing different strategies for cleaning up
 * the working directory, such as deleting all files, specific files, or doing nothing.</p>
 *
 * @author Dmytro Nosan
 * @see #deleteAll()
 * @see #deleteOnly(String...)
 * @see #doNothing()
 * @since 4.0.0
 */
@FunctionalInterface
public interface WorkingDirectoryDestroyer {

	/**
	 * Completely deletes the working directory.
	 *
	 * @return A new working directory destroyer
	 */
	static WorkingDirectoryDestroyer deleteAll() {
		return (workingDirectory, version) -> FileUtils.delete(workingDirectory);
	}

	/**
	 * Deletes the specified paths in the working directory.
	 *
	 * @param paths Paths within the working directory to delete (e.g., {@code lib}, {@code bin}, {@code tools},
	 * {@code conf/cassandra.yaml})
	 * @return A new working directory destroyer
	 * @throws NullPointerException If the paths or any of their elements are {@code null}
	 * @throws IllegalArgumentException If any path points outside the working directory
	 */
	static WorkingDirectoryDestroyer deleteOnly(String... paths) {
		Objects.requireNonNull(paths, "Paths must not be null");
		for (String path : paths) {
			Objects.requireNonNull(path, "Path must not be null");
		}
		return (workingDirectory, version) -> {
			Objects.requireNonNull(workingDirectory, "Working directory must not be null");
			Objects.requireNonNull(version, "Version must not be null");
			for (String path : paths) {
				Path normalizedPath = workingDirectory.resolve(path).normalize();
				if (!normalizedPath.startsWith(workingDirectory)) {
					throw new IllegalArgumentException(
							"Path: '" + path + "' is outside the directory: '" + workingDirectory + "'");
				}
				FileUtils.delete(normalizedPath);
			}
		};
	}

	/**
	 * Does nothing to the working directory. In other words, leaves it as it is.
	 *
	 * @return A new working directory destroyer
	 */
	static WorkingDirectoryDestroyer doNothing() {
		return (workingDirectory, version) -> {
		};
	}

	/**
	 * Destroys the working directory based on the implemented strategy.
	 *
	 * @param workingDirectory The working directory
	 * @param version The Cassandra version
	 * @throws IOException If an I/O error occurs
	 */
	void destroy(Path workingDirectory, Version version) throws IOException;

}
