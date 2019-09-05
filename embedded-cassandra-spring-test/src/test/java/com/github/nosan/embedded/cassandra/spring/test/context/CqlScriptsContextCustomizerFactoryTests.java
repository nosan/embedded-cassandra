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

package com.github.nosan.embedded.cassandra.spring.test.context;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextCustomizer;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.spring.test.CqlScripts;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScriptsContextCustomizerFactory}.
 *
 * @author Dmytro Nosan
 */
class CqlScriptsContextCustomizerFactoryTests {

	private final CqlScriptsContextCustomizerFactory factory = new CqlScriptsContextCustomizerFactory();

	@Test
	void shouldCreateContextCustomizerWhenCqlScriptsAnnotation() {
		assertThat(createContextCustomizer(CqlScriptsAnnotated.class)).isNotNull();
	}

	@Test
	void shouldNotCreateContextCustomizer() {
		assertThat(createContextCustomizer(NotAnnotated.class)).isNull();
	}

	@Test
	void contextCustomizerShouldBeEqualed() {
		assertThat(createContextCustomizer(CqlScriptsAnnotated.class)).isEqualTo(
				createContextCustomizer(CqlScriptsAnnotated.class));
	}

	@Nullable
	private ContextCustomizer createContextCustomizer(Class<?> testClass) {
		return this.factory.createContextCustomizer(testClass, Collections.emptyList());
	}

	@CqlScripts("schema.cql")
	private static class CqlScriptsAnnotated {

	}

	private static class NotAnnotated {

	}

}
