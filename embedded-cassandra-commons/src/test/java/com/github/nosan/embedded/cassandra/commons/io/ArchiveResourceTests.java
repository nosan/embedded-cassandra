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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArchiveResource}.
 *
 * @author Dmytro Nosan
 */
class ArchiveResourceTests {

	private final URL url = getClass().getResource("/test.tar.gz");

	private final ArchiveResource resource = new ArchiveResource(new UrlResource(this.url));

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
		try (InputStream inputStream = this.resource.getInputStream()) {
			assertThat(this.resource.getBytes()).isEqualTo(StreamUtils.copyToByteArray(inputStream));
		}
	}

	@Test
	void toURL() throws IOException {
		assertThat(this.resource.toURL()).isEqualTo(this.url);
	}

	@Test
	void getFileName() {
		assertThat(this.resource.getFileName()).isEqualTo("test.tar.gz");
	}

	@Test
	void exists() {
		assertThat(this.resource.exists()).isTrue();
	}

	@Test
	void testEquals() {
		assertThat(this.resource).isEqualTo(this.resource);
		assertThat(this.resource).isEqualTo(new ArchiveResource(new UrlResource(this.url)));
		assertThat(this.resource).isNotEqualTo(new ArchiveResource(new ClassPathResource("test.tar.gz")));
	}

	@Test
	void testHashCode() {
		assertThat(this.resource).hasSameHashCodeAs(this.resource);
	}

	@Test
	void testToString() {
		assertThat(this.resource.toString()).contains("test.tar.gz");
	}

	@Test
	void extract(@TempDir Path destination) throws IOException {
		this.resource.extract(destination);
		assertThat(destination.resolve("test")).isDirectory();
		assertThat(destination.resolve("test/file.txt")).isRegularFile();
	}

}
