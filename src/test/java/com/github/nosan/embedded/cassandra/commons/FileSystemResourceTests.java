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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileSystemResource}.
 *
 * @author Dmytro Nosan
 */
class FileSystemResourceTests {

	private FileSystemResource resource;

	private URL url;

	private Path file;

	@BeforeEach
	void setUp(@TempDir Path directory) throws IOException {
		this.file = directory.resolve("test.txt");
		try (InputStream inputStream = new ClassPathResource("test.txt").getInputStream()) {
			Files.copy(inputStream, this.file);
		}
		this.resource = new FileSystemResource(this.file);
		this.url = this.file.toUri().toURL();
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void setWritable() throws IOException {
		assertThat(this.resource.isWritable()).isTrue();
		Set<PosixFilePermission> permissions = new LinkedHashSet<>();
		Files.setPosixFilePermissions(this.file, permissions);
		assertThat(this.resource.isWritable()).isFalse();
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void setReadable() throws IOException {
		assertThat(this.resource.isReadable()).isTrue();
		Set<PosixFilePermission> permissions = new LinkedHashSet<>();
		Files.setPosixFilePermissions(this.file, permissions);
		assertThat(this.resource.isReadable()).isFalse();
	}

	@Test
	void getInputStream() throws IOException {
		assertThat(this.resource.isReadable()).isTrue();
		String s = "Hello World\n";
		try (InputStream inputStream = this.resource.getInputStream()) {
			assertThat(StreamUtils.toByteArray(inputStream)).isEqualTo(s.getBytes(Charset.defaultCharset()));
		}
	}

	@Test
	void getOutputStream() throws IOException {
		String s = "World Hello";
		assertThat(this.resource.isWritable()).isTrue();
		try (OutputStream outputStream = this.resource.getOutputStream()) {
			StreamUtils.copy(new ByteArrayInputStream(s.getBytes()), outputStream);
		}
		try (InputStream inputStream = this.resource.getInputStream()) {
			assertThat(StreamUtils.toString(inputStream, Charset.defaultCharset()))
					.isEqualTo(s);
		}
	}

	@Test
	void toURI() throws URISyntaxException {
		assertThat(this.resource.toURI()).isEqualTo(this.url.toURI());
	}

	@Test
	void toURL() throws IOException {
		assertThat(this.resource.toURL()).isEqualTo(this.url);
	}

	@Test
	void getFileName() {
		assertThat(this.resource.getFileName()).hasValue("test.txt");
	}

	@Test
	void exists() {
		assertThat(this.resource.exists()).isTrue();
	}

	@Test
	void isWritable(@TempDir Path dir) {
		assertThat(this.resource.isWritable()).isTrue();
		assertThat(new FileSystemResource(dir).isWritable()).isFalse();
	}

	@Test
	void isReadable(@TempDir Path dir) {
		assertThat(this.resource.isReadable()).isTrue();
		assertThat(new FileSystemResource(dir).isReadable()).isFalse();
	}

	@Test
	void testEquals() throws MalformedURLException {
		assertThat(this.resource.equals(this.resource)).isTrue();
		assertThat(this.resource.equals(new FileSystemResource(this.resource.getFile()))).isTrue();
		assertThat(this.resource.equals(new FileSystemResource(this.resource.getFile().toFile()))).isTrue();
		assertThat(this.resource.equals(null)).isFalse();
		assertThat(this.resource.equals(new UrlResource(new URL("http://localhost:8080")))).isFalse();
	}

	@Test
	void testHashCode() {
		assertThat(this.resource).hasSameHashCodeAs(this.resource);
	}

	@Test
	void testToString() {
		assertThat(this.resource.toString()).contains("test.txt");
	}

}
