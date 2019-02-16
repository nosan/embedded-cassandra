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

package com.github.nosan.embedded.cassandra.test.spring;

import java.util.Collections;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedLocalCassandraContextCustomizerFactory}.
 *
 * @author Dmytro Nosan
 */
public class EmbeddedLocalCassandraContextCustomizerFactoryTests {

	private final EmbeddedCassandraContextCustomizerFactory factory = new EmbeddedCassandraContextCustomizerFactory();

	@Test
	public void shouldCreateContextCustomizer() {
		assertThat(this.factory.createContextCustomizer(Annotated.class, Collections.emptyList()))
				.isNotNull();
	}

	@Test
	public void shouldNotCreateContextCustomizer() {
		assertThat(this.factory.createContextCustomizer(NotAnnotated.class, Collections.emptyList()))
				.isNull();
	}

	@EmbeddedLocalCassandra
	private static class Annotated {

	}

	private static class NotAnnotated {

	}
}