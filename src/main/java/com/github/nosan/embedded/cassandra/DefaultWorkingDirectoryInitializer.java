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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.github.nosan.embedded.cassandra.commons.FileUtils;

/**
 * The default implementation of {@link WorkingDirectoryInitializer} retrieves the Cassandra directory from the
 * {@link CassandraDirectoryProvider} and copies all files from the retrieved directory into the working directory,
 * except for the <b>javadoc</b>, <b>doc</b>, and <b>licenses</b> directories. By default, it replaces any existing
 * files in the working directory.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class DefaultWorkingDirectoryInitializer implements WorkingDirectoryInitializer {

	private static final Set<String> SKIP_DIRECTORIES = Collections.unmodifiableSet(
			new LinkedHashSet<>(Arrays.asList("javadoc", "doc", "licenses")));

	private final CassandraDirectoryProvider cassandraDirectoryProvider;

	private final CopyStrategy copyStrategy;

	/**
	 * Creates a new {@link DefaultWorkingDirectoryInitializer} with the {@link CopyStrategy#REPLACE_EXISTING} copy
	 * strategy.
	 *
	 * @param cassandraDirectoryProvider the Cassandra directory provider. This provider is used to retrieve the path to
	 * the Cassandra directory.
	 */
	public DefaultWorkingDirectoryInitializer(CassandraDirectoryProvider cassandraDirectoryProvider) {
		this(cassandraDirectoryProvider, DefaultWorkingDirectoryInitializer.CopyStrategy.REPLACE_EXISTING);
	}

	/**
	 * Creates a new {@link DefaultWorkingDirectoryInitializer}.
	 *
	 * @param cassandraDirectoryProvider the Cassandra directory provider. This provider is used to retrieve the path to
	 * the Cassandra directory.
	 * @param copyStrategy the strategy for copying Cassandra files.
	 */
	public DefaultWorkingDirectoryInitializer(CassandraDirectoryProvider cassandraDirectoryProvider,
			CopyStrategy copyStrategy) {
		Objects.requireNonNull(cassandraDirectoryProvider, "Cassandra Directory Provider must not be null");
		Objects.requireNonNull(copyStrategy, "Copy Strategy must not be null");
		this.cassandraDirectoryProvider = cassandraDirectoryProvider;
		this.copyStrategy = copyStrategy;
	}

	@Override
	public final void init(Path workingDirectory, Version version) throws IOException {
		Objects.requireNonNull(workingDirectory, "Working Directory must not be null");
		Objects.requireNonNull(version, "Version must not be null");
		Path cassandraDirectory = this.cassandraDirectoryProvider.getDirectory(version);
		Objects.requireNonNull(cassandraDirectory, "Cassandra Directory must not be null");
		this.copyStrategy.copy(cassandraDirectory, workingDirectory);
	}

	/**
	 * Cassandra file copy strategies.
	 */
	public interface CopyStrategy {

		/**
		 * Replaces a destination file if it exists.
		 */
		CopyStrategy REPLACE_EXISTING = (cassandraDirectory, workingDirectory) -> FileUtils.copy(cassandraDirectory,
				workingDirectory, (path, attributes) -> {
					if (attributes.isDirectory()) {
						return !SKIP_DIRECTORIES.contains(path.getFileName().toString());
					}
					return true;
				}, StandardCopyOption.REPLACE_EXISTING);

		/**
		 * Skips copying if a destination file already exists.
		 */
		CopyStrategy SKIP_EXISTING = (cassandraDirectory, workingDirectory) -> FileUtils.copy(cassandraDirectory,
				workingDirectory, (path, attributes) -> {
					if (attributes.isDirectory()) {
						return !SKIP_DIRECTORIES.contains(path.getFileName().toString());
					}
					return !Files.exists(workingDirectory.resolve(cassandraDirectory.relativize(path)));
				});

		/**
		 * Copies Cassandra files into the working directory.
		 *
		 * @param cassandraDirectory the Cassandra directory
		 * @param workingDirectory the Cassandra working directory
		 * @throws IOException if an I/O error occurs
		 */
		void copy(Path cassandraDirectory, Path workingDirectory) throws IOException;

	}

}
