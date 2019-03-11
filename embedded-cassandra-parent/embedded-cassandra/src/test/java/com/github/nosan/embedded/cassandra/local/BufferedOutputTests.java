/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BufferedOutput}.
 *
 * @author Dmytro Nosan
 */
class BufferedOutputTests {

	@Test
	void shouldKeepLines() {
		BufferedOutput output = new BufferedOutput(2);
		output.accept("line1");
		output.accept("line2");
		output.accept("line3");
		output.accept("line4");
		assertThat(output.toString()).contains("line3").contains("line4")
				.doesNotContain("line1").doesNotContain("line2");
	}

}
