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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ArtifactWorkingDirectoryCustomizer}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("ConstantConditions")
class ArtifactWorkingDirectoryCustomizerTests {

	private final Version version = Version.parse("3.11.3");

	@Nullable
	private Path workingDirectory;

	@Nullable
	private Path artifactDirectory;

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder) {
		this.workingDirectory = temporaryFolder.resolve(UUID.randomUUID().toString());
		this.artifactDirectory = temporaryFolder.resolve(UUID.randomUUID().toString());
	}

	@Test
	void impossibleToDetermineDirectory() throws Exception {
		Path plain = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path root = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;
		Path artifactDirectory = this.artifactDirectory;
		Version version = this.version;

		ArchiveUtils.extract(plain, artifactDirectory);

		ArtifactWorkingDirectoryCustomizer customizer = new ArtifactWorkingDirectoryCustomizer(
				new StaticArtifactFactory(version, root), artifactDirectory);

		assertThatThrownBy(() -> customizer.customize(workingDirectory, version))
				.hasStackTraceContaining("Impossible to determine the Apache Cassandra directory")
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void shouldInitializeDirectoryFolder() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;
		Path artifactDirectory = this.artifactDirectory;
		Version version = this.version;

		ArtifactWorkingDirectoryCustomizer customizer = new ArtifactWorkingDirectoryCustomizer(
				new StaticArtifactFactory(version, archive), artifactDirectory);
		customizer.customize(workingDirectory, version);

		assertThat(workingDirectory).exists();
		assertThat(workingDirectory.resolve("doc")).doesNotExist();
		assertThat(workingDirectory.resolve("javadoc")).doesNotExist();
		assertThat(workingDirectory.resolve("conf")).exists();
		assertThat(workingDirectory.resolve("bin")).exists();
	}

	@Test
	void shouldInitializeDirectoryFlat() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;
		Path artifactDirectory = this.artifactDirectory;
		Version version = this.version;

		ArtifactWorkingDirectoryCustomizer customizer = new ArtifactWorkingDirectoryCustomizer(
				new StaticArtifactFactory(version, archive), artifactDirectory);

		customizer.customize(workingDirectory, version);

		assertThat(workingDirectory).exists();
		assertThat(workingDirectory.resolve("doc")).doesNotExist();
		assertThat(workingDirectory.resolve("javadoc")).doesNotExist();
		assertThat(workingDirectory.resolve("conf")).exists();
		assertThat(workingDirectory.resolve("bin")).exists();
	}

	@Test
	void shouldNotInitializeInvalidDirectory() throws Exception {
		Path archive = Paths.get(getClass().getResource("/empty.zip").toURI());
		Path workingDirectory = this.workingDirectory;
		Path artifactDirectory = this.artifactDirectory;
		Version version = this.version;

		ArtifactWorkingDirectoryCustomizer customizer = new ArtifactWorkingDirectoryCustomizer(
				new StaticArtifactFactory(version, archive), artifactDirectory);

		assertThatThrownBy(() -> customizer.customize(workingDirectory, version))
				.hasStackTraceContaining("does not have the Apache Cassandra files")
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void shouldNotInitializeInvalidArchive(@TempDir Path tempDir) {
		Path archive = tempDir.resolve(UUID.randomUUID().toString());
		Path workingDirectory = this.workingDirectory;
		Path artifactDirectory = this.artifactDirectory;
		Version version = this.version;

		ArtifactWorkingDirectoryCustomizer customizer = new ArtifactWorkingDirectoryCustomizer(
				new StaticArtifactFactory(version, archive), artifactDirectory);

		assertThatThrownBy(() -> customizer.customize(workingDirectory, version))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private static final class StaticArtifactFactory implements ArtifactFactory {

		private final Version version;

		private final Artifact artifact;

		StaticArtifactFactory(Version version, Artifact artifact) {
			this.version = version;
			this.artifact = artifact;
		}

		StaticArtifactFactory(Version version, Path archive) {
			this(version, () -> archive);
		}

		@Override
		public Artifact create(Version version) {
			if (version.equals(this.version)) {
				return this.artifact;
			}
			throw new UnsupportedOperationException();
		}

	}

}
