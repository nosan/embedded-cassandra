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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.Nullable;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.util.ArchiveUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link WorkingDirectoryInitializer}.
 *
 * @author Dmytro Nosan
 */
public class WorkingDirectoryInitializerTests {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final Version version = new Version(3, 11, 3);

	@Nullable
	private Path workingDirectory;

	@Nullable
	private Path artifactDirectory;

	@Before
	public void setUp() throws Exception {
		this.workingDirectory = this.temporaryFolder.newFolder().toPath();
		this.artifactDirectory = this.temporaryFolder.newFolder().toPath();
	}

	@Test
	public void impossibleToDetermineBaseDirectory() throws Exception {
		Path plain = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path root = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());

		ArchiveUtils.extract(plain, Objects.requireNonNull(this.artifactDirectory));

		WorkingDirectoryInitializer
				customizer = new WorkingDirectoryInitializer(new StaticArtifactFactory(this.version, root),
				this.artifactDirectory);

		assertThatThrownBy(() -> customizer.initialize(Objects.requireNonNull(this.workingDirectory), this.version))
				.hasStackTraceContaining("Impossible to determine the Apache Cassandra directory")
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void shouldInitializeDirectoryFolderArchive() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;

		WorkingDirectoryInitializer
				customizer = new WorkingDirectoryInitializer(new StaticArtifactFactory(this.version, archive),
				Objects.requireNonNull(this.artifactDirectory));
		customizer.initialize(Objects.requireNonNull(workingDirectory), this.version);

		assertThat(workingDirectory).exists();
		assertThat(workingDirectory.resolve("doc")).doesNotExist();
		assertThat(workingDirectory.resolve("javadoc")).doesNotExist();
		assertThat(workingDirectory.resolve("conf")).exists();
		assertThat(workingDirectory.resolve("bin")).exists();
		assertThat(count(workingDirectory.resolve("bin"))).isGreaterThan(0);
		assertThat(count(workingDirectory.resolve("conf"))).isGreaterThan(0);
	}

	@Test
	public void shouldInitializeDirectoryFlatArchive() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;

		WorkingDirectoryInitializer
				customizer = new WorkingDirectoryInitializer(new StaticArtifactFactory(this.version, archive),
				Objects.requireNonNull(this.artifactDirectory));

		customizer.initialize(Objects.requireNonNull(workingDirectory), this.version);

		assertThat(workingDirectory).exists();
		assertThat(workingDirectory.resolve("doc")).doesNotExist();
		assertThat(workingDirectory.resolve("javadoc")).doesNotExist();
		assertThat(workingDirectory.resolve("conf")).exists();
		assertThat(workingDirectory.resolve("bin")).exists();
		assertThat(count(workingDirectory.resolve("bin"))).isGreaterThan(0);
		assertThat(count(workingDirectory.resolve("conf"))).isGreaterThan(0);
	}

	@Test
	public void directoryNotValidNoCassandraExecutable() throws Exception {

		Path archive = Paths.get(getClass().getResource("/empty.zip").toURI());
		WorkingDirectoryInitializer
				customizer = new WorkingDirectoryInitializer(new StaticArtifactFactory(this.version, archive),
				Objects.requireNonNull(this.artifactDirectory));

		assertThatThrownBy(() -> customizer.initialize(Objects.requireNonNull(this.workingDirectory), this.version))
				.hasStackTraceContaining("does not have the Apache Cassandra files")
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void invalidArchive() throws Exception {
		Path archive = this.temporaryFolder.newFile().toPath();
		WorkingDirectoryInitializer
				customizer = new WorkingDirectoryInitializer(new StaticArtifactFactory(this.version, archive),
				Objects.requireNonNull(this.artifactDirectory));

		assertThatThrownBy(() -> customizer.initialize(Objects.requireNonNull(this.workingDirectory), this.version))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private static long count(Path directory) throws IOException {
		return Files.list(directory).count();
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
