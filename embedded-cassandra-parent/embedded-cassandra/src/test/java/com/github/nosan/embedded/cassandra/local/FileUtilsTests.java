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
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileUtils}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("ConstantConditions")
class FileUtilsTests {

	@Nullable
	private Path temporaryFolder;

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder) {
		this.temporaryFolder = temporaryFolder;
	}

	@Test
	void deleteFile() throws IOException {
		Path file = newFile(UUID.randomUUID().toString());

		assertThat(file).exists();
		assertThat(FileUtils.delete(file)).isTrue();
		assertThat(file).doesNotExist();
	}

	@Test
	void shouldNotDelete() throws IOException {
		assertThat(FileUtils.delete(Paths.get(UUID.randomUUID().toString()))).isFalse();
		assertThat(FileUtils.delete(null)).isFalse();
	}

	@Test
	void deleteRecursivelyFolder() throws IOException {
		Path root = newFolder(UUID.randomUUID().toString());
		Path dir = newFolder(root, UUID.randomUUID().toString());
		Path file = newFile(dir, UUID.randomUUID().toString());

		assertThat(FileUtils.delete(root)).isTrue();
		assertThat(dir).doesNotExist();
		assertThat(file).doesNotExist();
	}

	@Test
	void copyFile() throws IOException {
		Path src = newFile(UUID.randomUUID().toString());
		Path dest = this.temporaryFolder.resolve(UUID.randomUUID().toString());

		assertThat(src).exists();
		assertThat(dest).doesNotExist();
		FileUtils.copy(src, dest, (path, attrs) -> true);
		assertThat(dest).exists();
	}

	@Test
	void copyDir() throws IOException {
		Path src = newFolder(UUID.randomUUID().toString());
		Path folder = newFolder(src, UUID.randomUUID().toString());
		Path file = newFile(folder, UUID.randomUUID().toString());

		Path dest = this.temporaryFolder.resolve(UUID.randomUUID().toString());

		assertThat(src).exists();
		assertThat(dest).doesNotExist();

		FileUtils.copy(src, dest, (path, attrs) -> true);

		assertThat(dest).exists();
		assertThat(dest.resolve(folder.getFileName())).exists();
		assertThat(dest.resolve(folder.getFileName()).resolve(file.getFileName())).exists();
	}

	@Test
	void shouldNotCopyFile() throws IOException {
		Path src = newFile(UUID.randomUUID().toString());

		Path dest = this.temporaryFolder.resolve(UUID.randomUUID().toString());

		assertThat(src).exists();
		assertThat(dest).doesNotExist();
		FileUtils.copy(src, dest, (path, attrs) -> false);
		assertThat(dest).doesNotExist();
	}

	@Test
	void shouldNotCopyNestedFiles() throws IOException {
		Path src = newFolder(UUID.randomUUID().toString());
		Path folder = newFolder(src, UUID.randomUUID().toString());
		Path file = newFile(folder, UUID.randomUUID().toString());

		Path dest = this.temporaryFolder.resolve(UUID.randomUUID().toString());

		assertThat(src).exists();
		assertThat(dest).doesNotExist();

		FileUtils.copy(src, dest, (path, attrs) -> false);

		assertThat(dest).doesNotExist();
		assertThat(dest.resolve(folder.getFileName())).doesNotExist();
		assertThat(dest.resolve(folder.getFileName()).resolve(file.getFileName())).doesNotExist();
	}

	private Path newFolder(String name) throws IOException {
		return newFolder(this.temporaryFolder, name);
	}

	private Path newFile(String name) throws IOException {
		return newFile(this.temporaryFolder, name);
	}

	private Path newFolder(Path folder, String name) throws IOException {
		return Files.createDirectories(folder.resolve(name));
	}

	private Path newFile(Path folder, String name) throws IOException {
		return Files.createFile(folder.resolve(name));
	}

}
