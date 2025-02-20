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
 * Provides the path to the Cassandra directory based on a specified version.
 *
 * @author Dmytro Nosan
 * @see WebCassandraDirectoryProvider
 * @since 4.0.0
 */
public interface CassandraDirectoryProvider {

	/**
	 * Returns the path to the Cassandra directory for the specified version.
	 *
	 * @param version the Cassandra version
	 * @return the path to the Cassandra directory
	 * @throws IOException if an I/O error occurs
	 */
	Path getDirectory(Version version) throws IOException;

}
