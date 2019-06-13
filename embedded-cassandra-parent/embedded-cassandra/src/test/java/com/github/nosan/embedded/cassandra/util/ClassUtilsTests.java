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

import com.github.nosan.embedded.cassandra.Cassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassUtils}.
 *
 * @author Dmytro Nosan
 */
class ClassUtilsTests {

	@Test
	void getPackageName() {
		assertThat(ClassUtils.getPackageName(null)).isEmpty();
		assertThat(ClassUtils.getPackageName(Object[].class)).isEqualTo("java.lang");
		assertThat(ClassUtils.getPackageName(ClassUtils.class)).isEqualTo("com.github.nosan.embedded.cassandra.util");
	}

	@Test
	void classIsPresent() {
		assertThat(ClassUtils.isPresent(Cassandra.class.getTypeName(), null)).isTrue();
	}

	@Test
	void classIsPresentNotInitialize() {
		assertThat(ClassUtils.isPresent(MyClass.class.getTypeName(), null)).isTrue();
	}

	@Test
	void classIsNotPresent() {
		assertThat(ClassUtils.isPresent("org.springframework.util.StringUtils", getClass().getClassLoader())).isFalse();
	}

	private static final class MyClass {

		static {
			if (Boolean.TRUE) {
				throw new IllegalStateException();
			}
		}
	}

}
