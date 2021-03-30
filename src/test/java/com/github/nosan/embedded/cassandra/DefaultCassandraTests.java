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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultCassandra}.
 *
 * @author Dmytro Nosan
 */
class DefaultCassandraTests {

	private final CassandraBuilder builder = new CassandraBuilder();

	@Test
	void unableToInitialize() {
		this.builder.workingDirectoryInitializer((workingDirectory, version) -> {
			throw new IOException("Fail");
		}).build();
		assertThatThrownBy(() -> this.builder.build().start()).hasStackTraceContaining("Unable to initialize");
	}

	@Test
	void getName() {
		Cassandra cassandra = this.builder.name("test").build();
		assertThat(cassandra.getName()).isEqualTo("test");
	}

	@Test
	void getVersion() {
		Cassandra cassandra = this.builder.version("3.11.10").build();
		assertThat(cassandra.getVersion()).isEqualTo(Version.parse("3.11.10"));
	}

	@Test
	void getWorkingDirectory(@TempDir Path workingDirectory) {
		Cassandra cassandra = this.builder.workingDirectory(() -> workingDirectory).build();
		assertThat(cassandra.getWorkingDirectory()).isEqualTo(workingDirectory);
	}

}
