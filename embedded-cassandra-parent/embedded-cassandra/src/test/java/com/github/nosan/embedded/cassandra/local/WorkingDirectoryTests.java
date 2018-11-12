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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.util.ArchiveUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WorkingDirectory}.
 *
 * @author Dmytro Nosan
 */
public class WorkingDirectoryTests {


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
	public void shouldInitializeDirectoryArchiveRootFolder() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());

		WorkingDirectory workDir = new WorkingDirectory(this.rootDirectory);

		Path directory = workDir.initialize(() -> archive);

		assertThat(directory).exists();
		assertThat(directory.resolve("doc")).doesNotExist();
		assertThat(directory.resolve("javadoc")).doesNotExist();
		assertThat(directory.resolve("conf")).exists();
		assertThat(directory.resolve("bin")).exists();
	}

	@Test
	public void shouldInitializeDirectoryNoRootFolder() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());


		WorkingDirectory workDir = new WorkingDirectory(this.rootDirectory);

		Path directory = workDir.initialize(() -> archive);

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

		WorkingDirectory workDir = new WorkingDirectory(this.rootDirectory);
		Path path = Paths.get(getClass().getResource("/empty.zip").toURI());
		workDir.initialize(() -> path);
	}

	@Test
	public void severalDirectoriesCandidate() throws Exception {
		this.throwable.expectMessage("Impossible to determine a base directory");
		this.throwable.expect(IllegalStateException.class);


		Path root = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path plain = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		ArchiveUtils.extract(root, this.rootDirectory, ignore -> true);
		WorkingDirectory baseDirectory = new WorkingDirectory(this.rootDirectory);
		baseDirectory.initialize(() -> plain);
	}


	@Test
	public void shouldDestroyTemporaryDirectory() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());

		WorkingDirectory workDir = new WorkingDirectory(this.rootDirectory);

		Path directory = workDir.initialize(() -> archive);

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
		WorkingDirectory workDir = new WorkingDirectory(this.rootDirectory);
		try {

			Path directory = workDir.initialize(() -> archive);

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

}
