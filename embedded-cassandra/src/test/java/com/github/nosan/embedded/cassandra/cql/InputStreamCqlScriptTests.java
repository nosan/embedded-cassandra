/*
 * Copyright 2012-2018 the original author or authors.
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InputStreamCqlScript}.
 *
 * @author Dmytro Nosan
 */
public class InputStreamCqlScriptTests {


	@Test
	public void getStatements() {
		assertThat(
				new InputStreamCqlScript(ClassLoader.getSystemResourceAsStream("test.cql")).getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )");
	}
}
