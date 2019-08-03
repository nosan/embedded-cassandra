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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StreamUtils}.
 *
 * @author Dmytro Nosan
 */
class StreamUtilsTests {

	@Test
	void readlines() {
		String source = "line1\n" + "line2\n" + "line3\n" + "\n" + "line4";
		StringBuilder result = new StringBuilder();
		StreamUtils.lines(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8,
				result::append);
		assertThat(result.toString()).isEqualTo("line1line2line3line4");
	}

}
