/*
 * Copyright 2020-2021 the original author or authors.
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

class AbstractCqlScriptTests {

	@Test
	void getStatementsEmpty() {
		assertThat(new EmptyScript().getStatements()).isEmpty();
	}

	@Test
	void getStatements() {
		assertThat(new StatementsScript().getStatements())
				.containsExactly("CREATE TABLE test.roles ( id text PRIMARY KEY )");
	}

	private static final class EmptyScript extends AbstractCqlScript {

		@Override
		protected String getScript() {
			return "";
		}

	}

	private static final class StatementsScript extends AbstractCqlScript {

		@Override
		protected String getScript() {
			return "CREATE TABLE test.roles ( id text PRIMARY KEY )";
		}

	}

}
