/*
 * Copyright 2020 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultWorkingDirectoryInitializer}.
 *
 * @author Dmytro Nosan
 */
class DefaultWorkingDirectoryInitializerTests {

	private final CassandraDirectoryProvider directoryProvider = Mockito.mock(CassandraDirectoryProvider.class);

	private final DefaultWorkingDirectoryInitializer initializer = new DefaultWorkingDirectoryInitializer(
			this.directoryProvider);

	@Test
	void initializeWorkingDir(@TempDir Path tempDir) throws IOException {
		Path cassandraDirectory = Files.createDirectory(tempDir.resolve("cassandraDirectory"));
		Path workingDirectory = Files.createDirectory(tempDir.resolve("workingDirectory"));
		Mockito.when(this.directoryProvider.getDirectory(CassandraBuilder.DEFAULT_VERSION))
				.thenReturn(cassandraDirectory);
		Files.createDirectories(cassandraDirectory.resolve("bin"));
		Files.createDirectories(cassandraDirectory.resolve("tools"));
		Files.createDirectories(cassandraDirectory.resolve("lib"));
		Files.createDirectories(cassandraDirectory.resolve("conf"));
		Files.createDirectories(cassandraDirectory.resolve("javadoc"));
		Files.createDirectories(cassandraDirectory.resolve("licenses"));
		Files.createDirectories(cassandraDirectory.resolve("doc"));
		Files.createFile(cassandraDirectory.resolve("conf/cassandra.yaml"));
		Files.createFile(cassandraDirectory.resolve("bin/cassandra"));
		Files.createFile(cassandraDirectory.resolve("lib/apache-cassandra-4.0-beta3.jar"));

		this.initializer.init(workingDirectory, CassandraBuilder.DEFAULT_VERSION);

		assertThat(workingDirectory.resolve("bin")).isDirectory().exists();
		assertThat(workingDirectory.resolve("tools")).isDirectory().exists();
		assertThat(workingDirectory.resolve("lib")).isDirectory().exists();
		assertThat(workingDirectory.resolve("conf")).isDirectory().exists();
		assertThat(workingDirectory.resolve("conf/cassandra.yaml")).isRegularFile().exists();
		assertThat(workingDirectory.resolve("lib/apache-cassandra-4.0-beta3.jar")).isRegularFile().exists();
		assertThat(workingDirectory.resolve("bin/cassandra")).isRegularFile().exists();
		assertThat(workingDirectory.resolve("javadoc")).doesNotExist();
		assertThat(workingDirectory.resolve("licenses")).doesNotExist();
		assertThat(workingDirectory.resolve("doc")).doesNotExist();
	}

}
