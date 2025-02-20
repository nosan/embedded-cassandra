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

/**
 * A strategy interface for initializing the working directory. After the {@link #init(Path, Version)} method is
 * executed, the working directory must contain all the necessary Cassandra files required for operation.
 *
 * <p>This interface provides an abstraction for setting up the working directory
 * with required files and configurations.</p>
 *
 * @author Dmytro Nosan
 * @see DefaultWorkingDirectoryInitializer
 * @since 4.0.0
 */
@FunctionalInterface
public interface WorkingDirectoryInitializer {

	/**
	 * Initializes the working directory by copying or setting up all required files.
	 *
	 * @param workingDirectory The working directory to initialize
	 * @param version The Cassandra version
	 * @throws IOException If an I/O error occurs during the initialization process
	 */
	void init(Path workingDirectory, Version version) throws IOException;

}
