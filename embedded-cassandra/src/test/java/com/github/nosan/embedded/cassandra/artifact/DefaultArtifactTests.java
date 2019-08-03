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

package com.github.nosan.embedded.cassandra.artifact;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.api.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link DefaultArtifact}.
 *
 * @author Dmytro Nosan
 */
class DefaultArtifactTests {

	@Test
	void testArtifact(@TempDir Path temporaryFolder) throws Exception {
		Path home = temporaryFolder.resolve("apache-cassandra-3.11.4");
		Files.createDirectories(home);
		Files.createDirectories(home.resolve("bin"));
		Files.createDirectories(home.resolve("lib"));
		Files.createDirectories(home.resolve("conf"));
		Files.createFile(home.resolve("conf/cassandra.yaml"));
		Version version = Version.of("3.11.4");
		Artifact artifact = new DefaultArtifact(version, temporaryFolder);
		Artifact.Resource distribution = artifact.getResource();
		assertThat(distribution.getVersion()).isEqualTo(version);
		Path directory = distribution.getDirectory();
		assertThat(directory.resolve("bin")).exists();
		assertThat(directory.resolve("conf")).exists();
		assertThat(directory.resolve("lib")).exists();
		assertThat(directory.resolve("conf/cassandra.yaml")).exists();
	}

	@Test
	void testArtifactFailNoCassandraHome(@TempDir Path temporaryFolder) {
		assertThatIllegalStateException().isThrownBy(() -> {
			Version version = Version.of("3.11.4");
			Artifact artifact = new DefaultArtifact(version, temporaryFolder);
			artifact.getResource();
		});
	}

	@Test
	void testArtifactFailTwoDirectories(@TempDir Path temporaryFolder) throws Exception {
		Path home = temporaryFolder.resolve("apache-cassandra-3.11.4-home");
		Files.createDirectories(home);
		Files.createDirectories(home.resolve("bin"));
		Files.createDirectories(home.resolve("lib"));
		Files.createDirectories(home.resolve("conf"));
		Files.createFile(home.resolve("conf/cassandra.yaml"));

		Path home1 = temporaryFolder.resolve("apache-cassandra-3.11.4-home1");
		Files.createDirectories(home1);
		Files.createDirectories(home1.resolve("bin"));
		Files.createDirectories(home1.resolve("lib"));
		Files.createDirectories(home1.resolve("conf"));
		Files.createFile(home1.resolve("conf/cassandra.yaml"));

		assertThatIllegalStateException().isThrownBy(() -> {
			Version version = Version.of("3.11.4");
			Artifact artifact = new DefaultArtifact(version, temporaryFolder);
			artifact.getResource();
		});
	}

}
