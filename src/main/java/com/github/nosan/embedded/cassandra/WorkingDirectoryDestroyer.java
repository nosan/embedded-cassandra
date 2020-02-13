/*
 * Copyright 2020 the original author or authors.
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
 * A strategy interface to destroy working directory.
 *
 * @author Dmytro Nosan
 * @see #deleteAll()
 * @see #deleteOnly(String...)
 * @since 4.0.0
 */
@FunctionalInterface
public interface WorkingDirectoryDestroyer {

	/**
	 * Completely deletes the working directory.
	 *
	 * @return a new working directory destroyer
	 */
	static WorkingDirectoryDestroyer deleteAll() {
		return (workingDirectory, version) -> FileUtils.delete(workingDirectory);
	}

	/**
	 * Delete the given paths in the working directory.
	 *
	 * @param paths paths within the working directory. (e.g. lib, bin, tools, conf/cassandra.yaml)
	 * @return a new working directory destroyer
	 */
	static WorkingDirectoryDestroyer deleteOnly(String... paths) {
		Objects.requireNonNull(paths, "Paths must not be null");
		return (workingDirectory, version) -> {
			for (String path : paths) {
				Objects.requireNonNull(path, "Path must not be null");
				Path normalizedPath = workingDirectory.resolve(path).normalize();
				if (!normalizedPath.startsWith(workingDirectory)) {
					throw new IllegalArgumentException(
							"Path: '" + path + "' is out of directory: '" + workingDirectory + "'");
				}
				FileUtils.delete(normalizedPath);
			}
		};
	}

	/**
	 * Destroys the working directory.
	 *
	 * @param workingDirectory working directory
	 * @param version Cassandra version
	 * @throws IOException an I/O error occurs
	 */
	void destroy(Path workingDirectory, Version version) throws IOException;

}
