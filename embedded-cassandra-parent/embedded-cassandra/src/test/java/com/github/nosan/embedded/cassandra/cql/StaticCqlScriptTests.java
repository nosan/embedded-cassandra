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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StaticCqlScript}.
 *
 * @author Dmytro Nosan
 */
class StaticCqlScriptTests {

	private static final String STATEMENT = "CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )";

	@Test
	void getStatements() {
		assertThat(new StaticCqlScript(STATEMENT).getStatements()).containsExactly(STATEMENT);
	}

	@Test
	void helpers() {
		assertThat(new StaticCqlScript(STATEMENT)).isEqualTo(new StaticCqlScript(STATEMENT));
		assertThat(new StaticCqlScript(STATEMENT).toString()).contains("Static CQL Statements '1'");
	}

}
