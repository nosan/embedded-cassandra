/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassPathResource}.
 *
 * @author Dmytro Nosan
 */
class ClassPathResourceTests {

	private final URL url = getClass().getResource("/text.txt");

	private final ClassPathResource resource = new ClassPathResource("text.txt");

	@Test
	void getInputStream() throws IOException {
		try (InputStream inputStream = this.resource.getInputStream()) {
			assertThat(StreamUtils.copyToByteArray(inputStream)).isEqualTo(
					"Text File\n".getBytes(StandardCharsets.UTF_8));
		}
	}

	@Test
	void toURI() throws IOException, URISyntaxException {
		assertThat(this.resource.toURI()).isEqualTo(this.url.toURI());
	}

	@Test
	void toPath() throws IOException, URISyntaxException {
		assertThat(this.resource.toPath()).isEqualTo(Paths.get(this.url.toURI()));
	}

	@Test
	void toFile() throws IOException, URISyntaxException {
		assertThat(this.resource.toFile()).isEqualTo(new File(this.url.toURI()));
	}

	@Test
	void getBytes() throws IOException {
		assertThat(this.resource.getBytes()).isEqualTo("Text File\n".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void toURL() throws FileNotFoundException {
		assertThat(this.resource.toURL()).isEqualTo(this.url);
	}

	@Test
	void getFileName() {
		assertThat(this.resource.getFileName()).isEqualTo("text.txt");
	}

	@Test
	void exists() {
		assertThat(this.resource.exists()).isTrue();
	}

	@Test
	void testEquals() throws MalformedURLException {
		assertThat(this.resource).isEqualTo(this.resource);
		assertThat(this.resource).isEqualTo(
				new ClassPathResource(this.resource.getPath(), this.resource.getClassLoader()));
		assertThat(this.resource).isNotEqualTo(new UrlResource(new URL("http://localhost:8080")));
	}

	@Test
	void testHashCode() {
		assertThat(this.resource).hasSameHashCodeAs(this.resource);
	}

	@Test
	void testToString() {
		assertThat(this.resource.toString()).contains("text.txt");
	}

}
