/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraFileExecutableCustomizer}.
 *
 * @author Dmytro Nosan
 */
class CassandraFileExecutableCustomizerTests {

	private final CassandraFileExecutableCustomizer customizer =
			new CassandraFileExecutableCustomizer();

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void setExecutableUnixFile(@TempDir Path temporaryFolder) throws IOException {
		Path file = temporaryFolder.resolve("bin/cassandra");
		Files.createDirectories(file.getParent());
		Files.createFile(file);
		this.customizer.customize(temporaryFolder, Version.parse("3.11.3"));
		assertThat(Files.getPosixFilePermissions(file)).
				contains(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OWNER_EXECUTE,
						PosixFilePermission.OTHERS_EXECUTE);
	}

}
