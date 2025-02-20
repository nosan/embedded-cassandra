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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link WorkingDirectoryCustomizer}.
 *
 * @author Dmytro Nosan
 */
class WorkingDirectoryCustomizerTests {

	@Test
	void copy(@TempDir Path directory) throws IOException {
		ClassPathResource resource = new ClassPathResource("schema.cql");
		WorkingDirectoryCustomizer.addResource(resource, "conf/schema.cql")
				.customize(directory, CassandraBuilder.DEFAULT_VERSION);

		WorkingDirectoryCustomizer.addResource(resource, "schema.cql")
				.customize(directory, CassandraBuilder.DEFAULT_VERSION);

		WorkingDirectoryCustomizer.addResource(resource, "bin/tools/schema.cql")
				.customize(directory, CassandraBuilder.DEFAULT_VERSION);

		try (InputStream inputStream = resource.getInputStream()) {
			byte[] expected = StreamUtils.toByteArray(inputStream);
			assertThat(directory.resolve("conf/schema.cql")).hasBinaryContent(expected);
			assertThat(directory.resolve("schema.cql")).hasBinaryContent(expected);
			assertThat(directory.resolve("bin/tools/schema.cql")).hasBinaryContent(expected);
		}

	}

	@Test
	void shouldNotCopyOutOfDirectory(@TempDir Path directory) {
		ClassPathResource resource = new ClassPathResource("schema.cql");
		assertThatThrownBy(() -> WorkingDirectoryCustomizer.addResource(resource, "/conf/schema.cql")
				.customize(directory, CassandraBuilder.DEFAULT_VERSION)).hasMessageContaining(
				"is out of the directory");
	}

	@Test
	void shouldNotCopyTargetIsDirectory(@TempDir Path directory) throws IOException {
		Files.createDirectories(directory.resolve("conf/schema.cql"));
		ClassPathResource resource = new ClassPathResource("schema.cql");
		assertThatThrownBy(() -> WorkingDirectoryCustomizer.addResource(resource, "conf/schema.cql")
				.customize(directory, CassandraBuilder.DEFAULT_VERSION)).hasMessageContaining("is a directory");
	}

}
