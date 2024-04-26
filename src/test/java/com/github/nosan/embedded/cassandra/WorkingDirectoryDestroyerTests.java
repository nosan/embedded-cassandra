/*
 * Copyright 2020-2024 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link WorkingDirectoryDestroyer}.
 *
 * @author Dmytro Nosan
 */
class WorkingDirectoryDestroyerTests {

	@Test
	void deleteAll(@TempDir Path directory) throws IOException {
		Files.createDirectory(directory.resolve("bin"));
		Files.createDirectory(directory.resolve("conf"));
		Files.createDirectory(directory.resolve("tools"));
		Files.createDirectory(directory.resolve("data"));
		Files.createFile(directory.resolve("conf/cassandra.yaml"));
		Files.createFile(directory.resolve("conf/cassandra-env.sh"));
		WorkingDirectoryDestroyer.deleteAll().destroy(directory, CassandraBuilder.DEFAULT_VERSION);
		assertThat(directory).doesNotExist();
	}

	@Test
	void deleteOnly(@TempDir Path directory) throws IOException {
		Files.createDirectory(directory.resolve("bin"));
		Files.createFile(directory.resolve("bin/cassandra.sh"));
		Files.createFile(directory.resolve("bin/cassandra.ps1"));
		Files.createDirectory(directory.resolve("conf"));
		Files.createDirectory(directory.resolve("tools"));
		Files.createDirectory(directory.resolve("data"));
		Files.createFile(directory.resolve("conf/cassandra.yaml"));
		Files.createFile(directory.resolve("conf/cassandra-env.sh"));
		WorkingDirectoryDestroyer.deleteOnly("bin", "tools", "conf/cassandra.yaml")
				.destroy(directory, CassandraBuilder.DEFAULT_VERSION);
		assertThat(directory).exists();
		assertThat(directory.resolve("tools")).doesNotExist();
		assertThat(directory.resolve("bin")).doesNotExist();
		assertThat(directory.resolve("data")).exists();
		assertThat(directory.resolve("conf/cassandra-env.sh")).exists();
		assertThat(directory.resolve("conf/cassandra.yaml")).doesNotExist();
	}

	@Test
	void shouldNotDeleteOutOfDirectory(@TempDir Path directory) {
		assertThatThrownBy(() -> WorkingDirectoryDestroyer.deleteOnly("/ttt")
				.destroy(directory, CassandraBuilder.DEFAULT_VERSION)).hasMessageContaining(" is out of a directory");

	}

	@Test
	void doNothing(@TempDir Path workingDirectory) throws IOException {
		WorkingDirectoryDestroyer.doNothing().destroy(workingDirectory, CassandraBuilder.DEFAULT_VERSION);
		assertThat(workingDirectory).exists();
	}

}
