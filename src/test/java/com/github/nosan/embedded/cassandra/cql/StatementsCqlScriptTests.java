/*
 * Copyright 2020-2024 the original author or authors.
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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StatementsCqlScript}.
 *
 * @author Dmytro Nosan
 */
class StatementsCqlScriptTests {

	private final String rawScript = "CREATE TABLE test.users ( id text PRIMARY KEY )";

	private final StatementsCqlScript script = new StatementsCqlScript(List.of(this.rawScript));

	@Test
	void testEquals() {
		assertThat(this.script.equals(this.script)).isTrue();
		assertThat(this.script.equals(null)).isFalse();
		assertThat(this.script.equals("CREATE TABLE test.roles ( id text PRIMARY KEY )")).isFalse();
		assertThat(this.script.equals(
				new StatementsCqlScript(List.of("CREATE TABLE test.users ( id text PRIMARY KEY )")))).isTrue();
	}

	@Test
	void testToString() {
		assertThat(this.script.toString()).contains("test.users");
	}

	@Test
	void testGetStatements() {
		List<String> statements = this.script.getStatements();
		assertThat(statements).containsExactly("CREATE TABLE test.users ( id text PRIMARY KEY )");
	}

}
