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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BufferedOutput}.
 *
 * @author Dmytro Nosan
 */
public class BufferedOutputTests {

	private final BufferedOutput output = new BufferedOutput(2);

	@Test
	public void shouldKeepLines() {
		this.output.accept("line");
		this.output.accept("line1");
		this.output.accept("line2");
		this.output.accept("line3");
		assertThat(this.output.lines()).hasSize(2)
				.containsExactly("line2", "line3");
	}

	@Test
	public void contains() {
		this.output.accept("line");
		this.output.accept("linE3");
		assertThat(this.output.lines()).hasSize(2)
				.containsExactly("line", "linE3");
	}
}
