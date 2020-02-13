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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ClassPathResource}.
 *
 * @author Dmytro Nosan
 */
class ClassPathResourceTests {

	private final URL url = getClass().getResource("/test.txt");

	private final ClassPathResource resource = new ClassPathResource("test.txt");

	@Test
	void emptyName() {
		assertThatThrownBy(() -> new ClassPathResource("")).hasStackTraceContaining("empty");
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
	void getOutputStream() {
		assertThat(this.resource.isWritable()).isFalse();
		assertThatThrownBy(this.resource::getOutputStream).isInstanceOf(IOException.class);
	}

	@Test
	void toURI() throws IOException, URISyntaxException {
		assertThat(this.resource.toURI()).isEqualTo(this.url.toURI());
	}

	@Test
	void toURL() throws FileNotFoundException {
		assertThat(this.resource.toURL()).isEqualTo(this.url);
	}

	@Test
	void getFileName() {
		assertThat(this.resource.getFileName()).hasValue("test.txt");
	}

	@Test
	void doesNotExist() {
		assertThat(new ClassPathResource(UUID.randomUUID().toString()).exists()).isFalse();
	}

	@Test
	void exists() {
		assertThat(this.resource.exists()).isTrue();
	}

	@Test
	void testEquals() throws MalformedURLException {
		assertThat(this.resource.equals(this.resource)).isTrue();
		assertThat(this.resource.equals(new ClassPathResource(this.resource.getPath()))).isTrue();
		assertThat(this.resource.equals(new ClassPathResource(this.resource.getPath(),
				new ClassLoader() {

				}))).isFalse();
		assertThat(this.resource.equals(new UrlResource(new URL("http://localhost:8080")))).isFalse();
		assertThat(this.resource.equals(null)).isFalse();
	}

	@Test
	void nameContainsDirectoryFileName() {
		assertThat(new ClassPathResource("/api/text.txt").getFileName())
				.hasValue("text.txt");
	}

	@Test
	void customClassLoader() {
		assertThatThrownBy(() -> new ClassPathResource(this.resource.getPath(), new ClassLoader() {

			@Override
			public URL getResource(String name) {
				return null;
			}
		}).getInputStream()).hasStackTraceContaining("Classpath resource with a name");
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
