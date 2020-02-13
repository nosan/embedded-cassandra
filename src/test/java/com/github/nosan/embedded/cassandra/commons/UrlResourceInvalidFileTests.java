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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link UrlResource}.
 *
 * @author Dmytro Nosan
 */
class UrlResourceInvalidFileTests {

	private UrlResource resource;

	private URL url;

	@BeforeEach
	void setUp(@TempDir Path directory) throws IOException {
		Path file = directory.resolve("test.txt");
		try (InputStream inputStream = new ClassPathResource("test.txt").getInputStream()) {
			Files.copy(inputStream, file);
		}
		this.url = new URL(file.toUri().toString().substring(0, 5) + " " + file.toUri().toString().substring(6));
		this.resource = new UrlResource(this.url);
	}

	@Test
	void getInputStream() throws IOException {
		assertThat(this.resource.isReadable()).isFalse();
	}

	@Test
	void getOutputStream() throws Exception {
		assertThat(this.resource.isWritable()).isFalse();
	}

	@Test
	void toURI() throws URISyntaxException, IOException {
		assertThatThrownBy(() -> this.resource.toURI())
				.hasStackTraceContaining("is not formatted strictly according to RFC2396");
	}

	@Test
	void toURL() {
		assertThat(this.resource.toURL()).isEqualTo(this.url);
	}

	@Test
	void getFileName() {
		assertThat(this.resource.getFileName()).isEmpty();
	}

	@Test
	void exists() {
		assertThat(this.resource.exists()).isFalse();
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
