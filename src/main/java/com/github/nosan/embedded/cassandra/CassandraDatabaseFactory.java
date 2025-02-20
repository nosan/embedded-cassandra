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

import java.nio.file.Path;

/**
 * A factory interface for creating instances of {@link CassandraDatabase}.
 *
 * @author Dmytro Nosan
 */
interface CassandraDatabaseFactory {

	/**
	 * Creates an instance of {@link CassandraDatabase} using the specified working directory.
	 *
	 * @param workingDirectory the directory where the Cassandra database will operate
	 * @return a new {@link CassandraDatabase} instance
	 * @throws Exception if an error occurs during the creation of the database instance
	 */
	CassandraDatabase create(Path workingDirectory) throws Exception;

}
