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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StreamUtils}.
 *
 * @author Dmytro Nosan
 */
class StreamUtilsTests {

	@Test
	void copyToByteArray() throws IOException {
		byte[] bytes = ("text").getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		assertThat(StreamUtils.toByteArray(is)).isEqualTo(bytes);
	}

	@Test
	void copyToString() throws IOException {
		String text = "text";
		byte[] bytes = text.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		assertThat(StreamUtils.toString(is, Charset.defaultCharset())).isEqualTo(text);
	}

	@Test
	void copyToStream() throws IOException {
		byte[] bytes = ("text").getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		StreamUtils.copy(is, os);
		assertThat(os.toByteArray()).isEqualTo(bytes);

	}

}
