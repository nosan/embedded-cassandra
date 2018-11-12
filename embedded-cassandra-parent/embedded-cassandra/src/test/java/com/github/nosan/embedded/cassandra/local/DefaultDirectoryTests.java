/*
 * Copyright 2018-2018 the original author or authors.
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.util.ArchiveUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultDirectory}.
 *
 * @author Dmytro Nosan
 */
public class DefaultDirectoryTests {


	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public final ExpectedException throwable = ExpectedException.none();

	private Path rootDirectory;

	@Before
	public void setUp() throws Exception {
		this.rootDirectory = this.temporaryFolder.newFolder().toPath();
	}

	@Test
	public void shouldInitializeDirectoryFolderArchive() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());

		DefaultDirectory workDir = new DefaultDirectory(this.rootDirectory, () -> archive, Collections.emptyList());

		Path directory = workDir.initialize();

		assertThat(directory).exists();
		assertThat(directory.resolve("doc")).doesNotExist();
		assertThat(directory.resolve("javadoc")).doesNotExist();
		assertThat(directory.resolve("conf")).exists();
		assertThat(directory.resolve("bin")).exists();
	}

	@Test
	public void shouldInitializeDirectoryFlatArchive() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());


		DefaultDirectory workDir = new DefaultDirectory(this.rootDirectory, () -> archive, Collections.emptyList());

		Path directory = workDir.initialize();

		assertThat(directory).exists();
		assertThat(directory.resolve("doc")).doesNotExist();
		assertThat(directory.resolve("javadoc")).doesNotExist();
		assertThat(directory.resolve("conf")).exists();
		assertThat(directory.resolve("bin")).exists();

	}


	@Test
	public void directoryNotValidNoCassandraExecutable() throws Exception {
		this.throwable.expectMessage("doesn't have a 'bin/cassandra' file");
		this.throwable.expect(IllegalArgumentException.class);
		Path archive = Paths.get(getClass().getResource("/empty.zip").toURI());

		DefaultDirectory workDir = new DefaultDirectory(this.rootDirectory, () -> archive, Collections.emptyList());
		workDir.initialize();
	}

	@Test
	public void severalDirectoriesCandidate() throws Exception {
		this.throwable.expectMessage("Impossible to determine a base directory");
		this.throwable.expect(IllegalStateException.class);


		Path archiveFolder = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path archiveFlat = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		ArchiveUtils.extract(archiveFolder, this.rootDirectory, ignore -> true);
		DefaultDirectory workDir = new DefaultDirectory(this.rootDirectory, () -> archiveFlat, Collections.emptyList());
		workDir.initialize();
	}


	@Test
	public void shouldDestroyTemporaryDirectory() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());

		DefaultDirectory workDir = new DefaultDirectory(this.rootDirectory, () -> archive, Collections.emptyList());

		Path directory = workDir.initialize();

		assertThat(directory).exists();
		assertThat(directory).hasParent(this.rootDirectory);

		workDir.destroy();
		assertThat(this.rootDirectory).doesNotExist();
		assertThat(directory).doesNotExist();


	}


	@Test
	public void shouldNotDestroyDirectory() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		this.rootDirectory = FileUtils.getUserDirectory().resolve(String.format("target/%s", UUID.randomUUID()));
		DefaultDirectory workDir = new DefaultDirectory(this.rootDirectory, () -> archive, Collections.emptyList());
		try {

			Path directory = workDir.initialize();

			assertThat(directory).exists();
			assertThat(directory).hasParent(this.rootDirectory);

			workDir.destroy();
			assertThat(directory).exists();
			assertThat(this.rootDirectory).exists();

		}
		finally {
			FileUtils.delete(this.rootDirectory);
		}

	}

	@Test
	public void shouldInvokeCustomizers() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		final class Customizer implements DirectoryCustomizer {
			private Path directory;

			@Override
			public void customize(@Nonnull Path directory) {
				this.directory = directory;
			}
		}
		Customizer customizer = new Customizer();

		DefaultDirectory workDir =
				new DefaultDirectory(this.rootDirectory, () -> archive, Collections.singletonList(customizer));

		Path directory = workDir.initialize();
		assertThat(directory).exists();
		assertThat(directory.resolve("doc")).doesNotExist();
		assertThat(directory.resolve("javadoc")).doesNotExist();
		assertThat(directory.resolve("conf")).exists();
		assertThat(directory.resolve("bin")).exists();
		assertThat(directory).isEqualTo(customizer.directory);

	}

	@Test
	public void invalidArchive() throws Exception {
		this.throwable.expectMessage("could not be extracted into ");
		this.throwable.expect(IOException.class);

		DefaultDirectory workDir =
				new DefaultDirectory(this.rootDirectory, () -> this.temporaryFolder.newFile().toPath(),
						Collections.emptyList());
		workDir.initialize();
	}


}
