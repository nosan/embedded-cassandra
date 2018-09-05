/*
 * Copyright 2018-2018 the original author or authors.
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

import com.github.nosan.embedded.cassandra.Version;


/**
 * Initializer interface used to initialize {@link Directory}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@FunctionalInterface
interface DirectoryInitializer {

	/**
	 * Initialize a directory.
	 *
	 * @param directory the base directory
	 * @param version the version
	 * @throws Exception in the case of any errors
	 */
	void initialize(@Nonnull Path directory, @Nonnull Version version) throws Exception;

}
