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
 * Tests for {@link FilteredOutput}.
 *
 * @author Dmytro Nosan
 */
public class FilteredOutputTests {

	@Test
	public void shouldFilter() {
		BufferedOutput delegate = new BufferedOutput(500);
		FilteredOutput filteredOutput = new FilteredOutput(delegate, line -> line.equals("text"));
		filteredOutput.accept("Hello world");
		filteredOutput.accept("text");
		assertThat(delegate.lines()).containsExactly("text");
	}
}
