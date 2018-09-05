/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StreamUtils}.
 *
 * @author Dmytro Nosan
 */
public class StreamUtilsTests {

	@Test
	public void toStringFullContent() throws Exception {
		assertThat(StreamUtils.toString(ClassLoader.getSystemResourceAsStream("utf8"),
				StandardCharsets.UTF_8))
				.contains("UTF-8");

		assertThat(StreamUtils.toString(ClassLoader.getSystemResourceAsStream("utf16-be"),
				StandardCharsets.UTF_16BE))
				.contains("UTF-16");

		assertThat(StreamUtils.toString(ClassLoader.getSystemResourceAsStream("utf16-le"),
				StandardCharsets.UTF_16LE))
				.contains("UTF-16");
	}

	@Test
	public void emptyIfNull() throws IOException {
		assertThat(StreamUtils.toString(null, StandardCharsets.UTF_16)).isEmpty();
		assertThat(StreamUtils.toString(null)).isEmpty();
	}
}
