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

package com.github.nosan.embedded.cassandra.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link FileUtils}.
 *
 * @author Dmytro Nosan
 */
class FileUtilsTests {

	@Nullable
	private Path temporaryFolder;

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder) {
		this.temporaryFolder = temporaryFolder;
	}

	@Test
	void getUserHomeDirectory() {
		assertThat(FileUtils.getUserHome()).isEqualTo(Paths.get(System.getProperty("user.home")));
	}

	@Test
	void shouldThrowIllegalStateExceptionWhenUserHomeDirNotPresent() {
		Properties properties = new Properties();
		properties.putAll(System.getProperties());
		try {
			System.clearProperty("user.home");
			assertThatIllegalStateException().isThrownBy(FileUtils::getUserHome);
		}
		finally {
			System.setProperties(properties);
		}
	}

	@Test
	void createIfNotExists() throws IOException {
		Path file = newFile();
		FileUtils.delete(file);
		FileUtils.createIfNotExists(file);
		FileUtils.createIfNotExists(file);
		assertThat(file).exists();
	}

	@Test
	void deleteFile() throws IOException {
		Path path = newFile();
		assertThat(path).exists();
		assertThat(FileUtils.delete(path)).isTrue();
		assertThat(path).doesNotExist();
	}

	@Test
	void shouldNotDelete() throws IOException {
		Path path = newFile();
		FileUtils.delete(path);
		assertThat(FileUtils.delete(path)).isFalse();
	}

	@Test
	void deleteRecursivelyFolder() throws IOException {
		Path rootDir = newFolder();
		Path subDir = newFolder(rootDir);
		Path subDirFile = newFile(subDir);

		assertThat(FileUtils.delete(rootDir)).isTrue();
		assertThat(subDir).doesNotExist();
		assertThat(subDirFile).doesNotExist();
		assertThat(rootDir).doesNotExist();
	}

	@Test
	void copyFile() throws IOException {
		Path src = newFile();
		Path dest = newFile();

		FileUtils.delete(dest);
		assertThat(src).exists();
		assertThat(dest).doesNotExist();
		FileUtils.copy(src, dest, (path, attrs) -> true);
		assertThat(dest).exists();
	}

	@Test
	void copyDir() throws IOException {
		Path src = newFolder();
		Path folder = newFolder(src);
		Path file = newFile(folder);

		Path dest = newFolder();
		Files.deleteIfExists(dest);

		assertThat(src).exists();
		assertThat(dest).doesNotExist();

		FileUtils.copy(src, dest, (path, attrs) -> true);

		assertThat(dest).exists();
		assertThat(dest.resolve(folder.getFileName())).exists();
		assertThat(dest.resolve(folder.getFileName()).resolve(file.getFileName())).exists();
	}

	@Test
	void shouldNotCopyFile() throws IOException {
		Path src = newFile();

		Path dest = newFile();
		FileUtils.delete(dest);

		assertThat(src).exists();
		assertThat(dest).doesNotExist();
		FileUtils.copy(src, dest, (path, attrs) -> false);
		assertThat(dest).doesNotExist();
	}

	@Test
	void shouldNotCopyNestedFiles() throws IOException {
		Path src = newFolder();
		Path folder = newFolder(src);
		Path file = newFile(folder);

		Path dest = newFolder();
		Files.deleteIfExists(dest);

		assertThat(src).exists();
		assertThat(dest).doesNotExist();

		FileUtils.copy(src, dest, (path, attrs) -> false);

		assertThat(dest).doesNotExist();
		assertThat(dest.resolve(folder.getFileName())).doesNotExist();
		assertThat(dest.resolve(folder.getFileName()).resolve(file.getFileName())).doesNotExist();
	}

	private Path newFile() throws IOException {
		return newFile(this.temporaryFolder);
	}

	private Path newFolder() throws IOException {
		return newFolder(this.temporaryFolder);
	}

	private Path newFolder(@Nullable Path dir) throws IOException {
		return (dir != null) ? Files.createTempDirectory(dir, "") : Files.createTempDirectory("");
	}

	private Path newFile(@Nullable Path dir) throws IOException {
		return (dir != null) ? Files.createTempFile(dir, "", "") : Files.createTempFile("", "");
	}

}
