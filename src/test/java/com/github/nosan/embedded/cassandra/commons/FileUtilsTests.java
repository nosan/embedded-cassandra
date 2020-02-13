/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileUtils}.
 *
 * @author Dmytro Nosan
 */
class FileUtilsTests {

	private Path temporaryFolder;

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder) {
		this.temporaryFolder = temporaryFolder;
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
		assertThat(FileUtils.delete(null)).isFalse();
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
		Path dest = newFolder();

		FileUtils.delete(dest);
		assertThat(src).exists();
		assertThat(dest).doesNotExist();
		FileUtils.copy(src, dest);
		assertThat(dest).exists();
	}

	@Test
	void copyFileReplace() throws IOException {
		Path src = newFile();
		Files.copy(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}), src, StandardCopyOption.REPLACE_EXISTING);
		Path dest = newFolder();

		assertThat(src).exists();
		assertThat(dest).exists();
		FileUtils.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
		assertThat(dest).hasSameBinaryContentAs(src);
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

		FileUtils.copy(src, dest, (path, attrs) -> !path.equals(folder));

		assertThat(dest).exists();
		assertThat(dest.resolve(folder.getFileName())).doesNotExist();
		assertThat(dest.resolve(folder.getFileName()).resolve(file.getFileName())).doesNotExist();
	}

	@Test
	void checksum() throws IOException, NoSuchAlgorithmException {
		Path path = Paths.get(new ClassPathResource("schema.cql").toURI());
		assertThat(path).hasDigest("SHA-512", FileUtils.checksum(path, "SHA-512"));
		assertThat(path).hasDigest("SHA-256", FileUtils.checksum(path, "SHA-256"));
		assertThat(path).hasDigest("SHA-1", FileUtils.checksum(path, "SHA-1"));
		assertThat(path).hasDigest("MD5", FileUtils.checksum(path, "MD5"));
	}

	private Path newFile() throws IOException {
		return newFile(this.temporaryFolder);
	}

	private Path newFolder() throws IOException {
		return newFolder(this.temporaryFolder);
	}

	private Path newFolder(Path dir) throws IOException {
		return Files.createTempDirectory(dir, "");
	}

	private Path newFile(Path dir) throws IOException {
		return Files.createTempFile(dir, "", "");
	}

}
