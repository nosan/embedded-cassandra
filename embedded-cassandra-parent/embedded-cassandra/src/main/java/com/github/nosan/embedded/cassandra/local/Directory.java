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
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.nosan.embedded.cassandra.local.artifact.Artifact;

/**
 * Encapsulates information about a directory.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
interface Directory extends Supplier<Path> {

	/**
	 * Creates a directory based on the given artifact.
	 *
	 * @param artifact the artifact to initialize a directory
	 * @throws Exception in the case of any errors
	 */
	void initialize(@Nonnull Artifact artifact) throws Exception;

	/**
	 * Disposes the directory.
	 *
	 * @throws Exception in the case of any errors
	 */
	void destroy() throws Exception;

	/**
	 * Returns the directory path.
	 *
	 * @return the path
	 * @throws java.io.UncheckedIOException if directory has not been created.
	 */
	@Override
	@Nonnull
	Path get();

}
