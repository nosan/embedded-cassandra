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

package com.github.nosan.embedded.cassandra.cql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlStatements}.
 *
 * @author Dmytro Nosan
 */
class CqlStatementsTests {

	private static final String ROLES = "CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )";

	@Test
	void assertStatements() {
		assertThat(statements(ROLES).getStatements()).containsExactly(ROLES);
	}

	@Test
	void assertHashCode() {
		assertThat(statements(ROLES)).hasSameHashCodeAs(statements(ROLES));
		assertThat(statements(ROLES).hashCode()).isNotEqualTo(statements());
	}

	@Test
	void assertEquals() {
		assertThat(statements(ROLES).getStatements()).isEqualTo(statements(ROLES).getStatements())
				.isNotEqualTo(statements());
	}

	@Test
	void assertToString() {
		assertThat(statements(ROLES).toString()).contains(ROLES);
	}

	private CqlStatements statements(String... statements) {
		return new CqlStatements(statements);
	}

}
