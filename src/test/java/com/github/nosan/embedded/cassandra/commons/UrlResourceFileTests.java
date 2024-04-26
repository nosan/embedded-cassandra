/*
 * Copyright 2020-2024 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UrlResource}.
 *
 * @author Dmytro Nosan
 */
class UrlResourceFileTests {

	private UrlResource resource;

	private URL url;

	@BeforeEach
	void setUp(@TempDir Path directory) throws IOException {
		Path file = directory.resolve("test.txt");
		try (InputStream inputStream = new ClassPathResource("test.txt").getInputStream()) {
			Files.copy(inputStream, file);
		}
		this.resource = new UrlResource(file.toUri().toURL());
		this.url = file.toUri().toURL();
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
	void getOutputStream() throws Exception {
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
	void toURI() throws URISyntaxException, IOException {
		assertThat(this.resource.toURI()).isEqualTo(this.url.toURI());
	}

	@Test
	void toURL() {
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
	void testEquals() throws MalformedURLException {
		assertThat(this.resource.equals(this.resource)).isTrue();
		assertThat(this.resource.equals(new UrlResource(this.url))).isTrue();
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
