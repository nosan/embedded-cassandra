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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.commons.Resource;

/**
 * A callback interface to customize a working directory.
 *
 * @author Dmytro Nosan
 * @see #addResource(Resource, String)
 * @since 4.0.0
 */
@FunctionalInterface
public interface WorkingDirectoryCustomizer {

	/**
	 * Copies a resource to a target path within the working directory.
	 * <pre>
	 * - If the target file does not exist, it will be created.
	 * - If the target file exists, it will be replaced.
	 * </pre>
	 * For example:
	 * <pre>
	 * {@code WorkingDirectoryCustomizer.addResource(new ClassPathResource("cassandra.yaml"), "conf/cassandra.yaml")}
	 * </pre>
	 *
	 * @param path The path (file only) within the working directory (e.g., conf/cassandra.yaml)
	 * @param resource The resource to be copied
	 * @return A new working directory customizer
	 * @throws NullPointerException if the path or resource is {@code null}
	 * @throws IllegalArgumentException if the target path points outside the working directory or is a directory
	 */
	static WorkingDirectoryCustomizer addResource(Resource resource, String path) {
		Objects.requireNonNull(path, "File path must not be null");
		Objects.requireNonNull(resource, "Resource must not be null");
		return (workingDirectory, version) -> {
			Objects.requireNonNull(workingDirectory, "Working directory must not be null");
			Objects.requireNonNull(version, "Version must not be null");
			Path normalizedPath = workingDirectory.resolve(path).normalize().toAbsolutePath();
			if (!normalizedPath.startsWith(workingDirectory)) {
				throw new IllegalArgumentException("Path: '" + normalizedPath
						+ "' is out of the directory: '" + workingDirectory + "'");
			}
			if (Files.isDirectory(normalizedPath)) {
				throw new IllegalArgumentException("Path: '" + normalizedPath + "' is a directory");
			}
			Path parent = normalizedPath.getParent();
			if (!Files.exists(parent)) {
				Files.createDirectories(parent);
			}
			try (InputStream is = resource.getInputStream()) {
				Files.copy(is, normalizedPath, StandardCopyOption.REPLACE_EXISTING);
			}
		};
	}

	/**
	 * Customizes the working directory.
	 *
	 * @param workingDirectory The working directory
	 * @param version The version
	 * @throws IOException If an I/O error occurs
	 */
	void customize(Path workingDirectory, Version version) throws IOException;

}
