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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.util.ArchiveUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArtifactCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class ArtifactCustomizerTests {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public final ExpectedException throwable = ExpectedException.none();

	private final Version version = new Version(3, 11, 3);

	private Path workingDirectory;

	private Path artifactDirectory;

	@Before
	public void setUp() throws Exception {
		this.workingDirectory = this.temporaryFolder.newFolder().toPath();
		this.artifactDirectory = this.temporaryFolder.newFolder().toPath();
	}

	@Test
	public void impossibleToDetermineBaseDirectory() throws Exception {
		this.throwable.expectMessage("Impossible to determine a base directory");
		this.throwable.expect(IllegalStateException.class);

		Path plain = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path root = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());

		ArchiveUtils.extract(plain, this.artifactDirectory, ignore -> true);

		ArtifactCustomizer customizer = new ArtifactCustomizer(new StaticArtifactFactory(this.version, root),
				this.artifactDirectory);

		customizer.customize(this.workingDirectory, this.version);
	}

	@Test
	public void shouldInitializeDirectoryFolderArchive() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;

		ArtifactCustomizer customizer = new ArtifactCustomizer(new StaticArtifactFactory(this.version, archive),
				this.artifactDirectory);
		customizer.customize(workingDirectory, this.version);

		assertThat(workingDirectory).exists();
		assertThat(workingDirectory.resolve("doc")).exists();
		assertThat(workingDirectory.resolve("javadoc")).exists();
		assertThat(workingDirectory.resolve("conf")).exists();
		assertThat(workingDirectory.resolve("bin")).exists();
		assertThat(Files.list(workingDirectory.resolve("doc")).count()).isZero();
		assertThat(Files.list(workingDirectory.resolve("javadoc")).count()).isZero();
	}

	@Test
	public void shouldInitializeDirectoryFlatArchive() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path workingDirectory = this.workingDirectory;

		ArtifactCustomizer customizer = new ArtifactCustomizer(new StaticArtifactFactory(this.version, archive),
				this.artifactDirectory);

		customizer.customize(workingDirectory, this.version);

		assertThat(workingDirectory).exists();
		assertThat(workingDirectory.resolve("doc")).exists();
		assertThat(workingDirectory.resolve("javadoc")).exists();
		assertThat(workingDirectory.resolve("conf")).exists();
		assertThat(workingDirectory.resolve("bin")).exists();
		assertThat(Files.list(workingDirectory.resolve("doc")).count()).isZero();
		assertThat(Files.list(workingDirectory.resolve("javadoc")).count()).isZero();
	}

	@Test
	public void directoryNotValidNoCassandraExecutable() throws Exception {
		this.throwable.expectMessage("doesn't have one of the ");
		this.throwable.expect(IllegalStateException.class);

		Path archive = Paths.get(getClass().getResource("/empty.zip").toURI());
		ArtifactCustomizer customizer = new ArtifactCustomizer(new StaticArtifactFactory(this.version, archive),
				this.artifactDirectory);

		customizer.customize(this.workingDirectory, this.version);
	}

	@Test
	public void invalidArchive() throws Exception {
		this.throwable.expect(IllegalArgumentException.class);

		Path archive = this.temporaryFolder.newFile().toPath();
		ArtifactCustomizer customizer = new ArtifactCustomizer(new StaticArtifactFactory(this.version, archive),
				this.artifactDirectory);

		customizer.customize(this.workingDirectory, this.version);
	}

	private static final class StaticArtifactFactory implements ArtifactFactory {

		@Nonnull
		private final Version version;

		@Nonnull
		private final Artifact artifact;

		StaticArtifactFactory(@Nonnull Version version, @Nonnull Artifact artifact) {
			this.version = version;
			this.artifact = artifact;
		}

		StaticArtifactFactory(@Nonnull Version version, @Nonnull Path archive) {
			this(version, () -> archive);
		}

		@Nonnull
		@Override
		public Artifact create(@Nonnull Version version) {
			if (version.equals(this.version)) {
				return this.artifact;
			}
			throw new UnsupportedOperationException();
		}
	}
}
