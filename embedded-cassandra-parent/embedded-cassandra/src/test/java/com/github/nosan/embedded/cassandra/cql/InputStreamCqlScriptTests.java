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

package com.github.nosan.embedded.cassandra.cql;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link InputStreamCqlScript}.
 *
 * @author Dmytro Nosan
 */
class InputStreamCqlScriptTests {

	private static final String ROLES = "/roles.cql";

	@Test
	void assertStatements() {
		assertThat(classpath(ROLES).getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void assertHashCode() {
		assertThat(classpath(ROLES).hashCode()).isNotEqualTo(classpath(ROLES).hashCode());
	}

	@Test
	void assertEquals() {
		assertThat(classpath(ROLES)).isNotEqualTo(classpath(ROLES));
	}

	@Test
	void assertToString() {
		assertThat(classpath(ROLES).toString()).contains("InputStream CQL Statements");
	}

	@Test
	void assertExceptionThrown() throws IOException {
		InputStreamCqlScript script = classpath(ROLES);
		script.getInputStream().close();
		assertThatThrownBy(script::getStatements).isInstanceOf(UncheckedIOException.class);
	}

	private InputStreamCqlScript classpath(String url) {
		return new InputStreamCqlScript(getClass().getResourceAsStream(url));
	}

}
