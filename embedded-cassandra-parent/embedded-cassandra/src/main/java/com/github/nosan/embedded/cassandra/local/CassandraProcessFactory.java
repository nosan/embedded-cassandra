/*
 * Copyright 2018-2019 the original author or authors.
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

import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Factory that creates a {@link CassandraProcess}.
 *
 * @author Dmytro Nosan
 * @see DefaultCassandraProcess
 * @see DefaultCassandraProcessFactory
 * @since 1.0.9
 */
interface CassandraProcessFactory {

	/**
	 * Creates a new  configured {@link CassandraProcess}.
	 *
	 * @param directory a configured base directory
	 * @return {@code process} to use
	 */
	@Nonnull
	CassandraProcess create(@Nonnull Path directory);
}
