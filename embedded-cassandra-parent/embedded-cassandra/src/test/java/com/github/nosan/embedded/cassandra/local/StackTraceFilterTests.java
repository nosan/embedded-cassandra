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
 * Tests for {@link StackTraceFilter}.
 *
 * @author Dmytro Nosan
 */
public class StackTraceFilterTests {

	@Test
	public void shouldFilter() {
		StackTraceFilter filter = new StackTraceFilter();
		assertThat(filter.test("\tat 55")).isFalse();
		assertThat(filter.test("at 55")).isTrue();
		assertThat(filter.test("\t... twxt")).isFalse();
		assertThat(filter.test("... twxt")).isTrue();
		assertThat(filter.test("text")).isTrue();

	}

}
