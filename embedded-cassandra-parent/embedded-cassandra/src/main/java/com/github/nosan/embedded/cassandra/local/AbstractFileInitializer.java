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

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Version;

/**
 * Abstract file {@link Initializer} to initialize a file within a working directory.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
abstract class AbstractFileInitializer implements Initializer {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Nonnull
	private final BiFunction<Path, Version, Path> fileMapper;

	/**
	 * Creates a {@link AbstractFileInitializer}.
	 *
	 * @param fileMapper the function to resolve a file within a directory
	 */
	AbstractFileInitializer(@Nonnull BiFunction<Path, Version, Path> fileMapper) {
		this.fileMapper = fileMapper;
	}

	@Override
	public final void initialize(@Nonnull Path workingDirectory, @Nonnull Version version) throws IOException {
		Path file = this.fileMapper.apply(workingDirectory, version);
		if (file != null) {
			initialize(file, workingDirectory, version);
		}
	}

	/**
	 * Initialize a file within a working directory.
	 *
	 * @param file the file to initialize (file may not exist)
	 * @param workingDirectory the working directory (CASSANDRA_HOME)
	 * @param version a version
	 * @throws IOException in the case of any IO errors
	 */
	protected abstract void initialize(@Nonnull Path file, @Nonnull Path workingDirectory, @Nonnull Version version)
			throws IOException;
}
