/*
 * Copyright 2020-2021 the original author or authors.
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
 * A strategy interface to initialize the working directory. The working directory must contain all necessary Cassandra
 * files after the {@link #init(Path, Version)} method has been called.
 *
 * @author Dmytro Nosan
 * @see DefaultWorkingDirectoryInitializer
 * @since 4.0.0
 */
@FunctionalInterface
public interface WorkingDirectoryInitializer {

	/**
	 * Initializes working directory.
	 *
	 * @param workingDirectory working directory
	 * @param version Cassandra version
	 * @throws IOException an I/O error occurs
	 */
	void init(Path workingDirectory, Version version) throws IOException;

}
