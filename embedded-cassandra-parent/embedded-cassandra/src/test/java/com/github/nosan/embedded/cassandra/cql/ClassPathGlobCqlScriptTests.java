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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassPathGlobCqlScript}.
 *
 * @author Dmytro Nosan
 */
class ClassPathGlobCqlScriptTests {

	private static final String ROLE = "CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)";

	private static final String KEYSPACE =
			"CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}";

	@ParameterizedTest
	@MethodSource("patterns")
	void test(String pattern, String[] statements) {
		ClassPathGlobCqlScript script = new ClassPathGlobCqlScript(pattern);
		assertThat(script).isNotEqualTo(new ClassPathGlobCqlScript("sometext"));
		assertThat(script).isEqualTo(script);
		assertThat(script.getStatements()).containsExactly(statements);
	}

	static Stream<Arguments> patterns() {
		List<Arguments> parameters = new ArrayList<>();
		parameters.add(Arguments.arguments("/.cql", new String[0]));
		parameters.add(Arguments.arguments("/*.cql", new String[]{ROLE}));
		parameters.add(Arguments.arguments("**/**.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("**/*.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("**.cql", new String[]{KEYSPACE, ROLE}));
		parameters.add(Arguments.arguments("**{roles,keyspace}.cql", new String[]{KEYSPACE, ROLE}));
		parameters.add(Arguments.arguments("{roles,keyspace}.cql", new String[]{ROLE}));
		parameters.add(Arguments.arguments("**/{roles,keyspace}.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("**{keyspace}.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("*/*.cql", new String[0]));
		parameters.add(Arguments.arguments("**/key*.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("**\\key*.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("com/*/*/embe*ed/**/keyspa?e.cql", new String[]{KEYSPACE}));
		parameters.add(Arguments.arguments("roles.cql", new String[]{ROLE}));
		parameters.add(Arguments.arguments("/roles.cql", new String[]{ROLE}));
		parameters.add(Arguments.arguments("\\roles.cql", new String[]{ROLE}));
		parameters.add(Arguments.arguments("*.cql", new String[]{ROLE}));
		parameters.add(Arguments.arguments("rol?s.cql", new String[]{ROLE}));
		return parameters.stream();
	}

}
