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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.util.ArchiveUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BaseDirectory}.
 *
 * @author Dmytro Nosan
 */
public class BaseDirectoryTests {


	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public final ExpectedException throwable = ExpectedException.none();


	@Test
	public void shouldInitializeDirectoryArchiveRootFolder() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = this.temporaryFolder.newFolder().toPath();
		BaseDirectory baseDirectory = new BaseDirectory(workingDirectory);

		baseDirectory.initialize(() -> archive);
		Path directory = baseDirectory.get();

		assertThat(directory).exists();
		assertThat(directory.resolve("doc")).doesNotExist();
		assertThat(directory.resolve("javadoc")).doesNotExist();
		assertThat(directory.resolve("conf")).exists();
		assertThat(directory.resolve("bin")).exists();
	}

	@Test
	public void shouldInitializeDirectoryNoRootFolder() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		Path workingDirectory = this.temporaryFolder.newFolder().toPath();


		BaseDirectory baseDirectory = new BaseDirectory(workingDirectory);

		baseDirectory.initialize(() -> archive);
		Path directory = baseDirectory.get();

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
		Path workingDirectory = this.temporaryFolder.newFolder().toPath();
		BaseDirectory baseDirectory = new BaseDirectory(workingDirectory);
		baseDirectory.get();
	}

	@Test
	public void severalDirectoriesCandidate() throws Exception {
		this.throwable.expectMessage("Impossible to determine a base directory");
		this.throwable.expect(IllegalStateException.class);
		Path workingDirectory = this.temporaryFolder.newFolder().toPath();

		Path root = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path plain = Paths.get(getClass().getResource("/apache-cassandra-plain-3.11.3.zip").toURI());
		ArchiveUtils.extract(root, workingDirectory, ignore -> true);
		BaseDirectory baseDirectory = new BaseDirectory(workingDirectory);
		baseDirectory.initialize(() -> plain);
		baseDirectory.get();
	}


	@Test
	public void shouldDestroyTemporaryDirectory() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = this.temporaryFolder.newFolder().toPath();
		BaseDirectory baseDirectory = new BaseDirectory(workingDirectory);

		baseDirectory.initialize(() -> archive);
		Path directory = baseDirectory.get();

		assertThat(directory).exists();
		assertThat(directory).hasParent(workingDirectory);

		baseDirectory.destroy();
		assertThat(workingDirectory).doesNotExist();
		assertThat(directory).doesNotExist();


	}


	@Test
	public void shouldNotDestroyDirectory() throws Exception {
		Path archive = Paths.get(getClass().getResource("/apache-cassandra-3.11.3.zip").toURI());
		Path workingDirectory = FileUtils.getUserDirectory().resolve(String.format("target/%s", UUID.randomUUID()));
		BaseDirectory baseDirectory = new BaseDirectory(workingDirectory);
		try {

			baseDirectory.initialize(() -> archive);
			Path directory = baseDirectory.get();

			assertThat(directory).exists();
			assertThat(directory).hasParent(workingDirectory);

			baseDirectory.destroy();
			assertThat(directory).exists();
			assertThat(workingDirectory).exists();

		}
		finally {
			FileUtils.delete(workingDirectory);
		}


	}

}
