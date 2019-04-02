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

package com.github.nosan.embedded.cassandra.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StringUtils}.
 *
 * @author Dmytro Nosan
 */
class StringUtilsTests {

	@Test
	void hasText() {
		assertThat(StringUtils.hasText(" ")).isFalse();
		assertThat(StringUtils.hasText("\t")).isFalse();
		assertThat(StringUtils.hasText(" a ")).isTrue();
		assertThat(StringUtils.hasText(null)).isFalse();

	}

	@Test
	void hasLength() {
		assertThat(StringUtils.hasLength(" ")).isTrue();
		assertThat(StringUtils.hasLength("\t")).isTrue();
		assertThat(StringUtils.hasLength("\n")).isTrue();
		assertThat(StringUtils.hasLength("")).isFalse();
		assertThat(StringUtils.hasLength(null)).isFalse();
	}

	@Test
	void isEmpty() {
		assertThat(StringUtils.isEmpty(" ")).isFalse();
		assertThat(StringUtils.isEmpty("\t")).isFalse();
		assertThat(StringUtils.isEmpty("\n")).isFalse();
		assertThat(StringUtils.isEmpty("")).isTrue();
		assertThat(StringUtils.isEmpty(null)).isTrue();
	}

}
