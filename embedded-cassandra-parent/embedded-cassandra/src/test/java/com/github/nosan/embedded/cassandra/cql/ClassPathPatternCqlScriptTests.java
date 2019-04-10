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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Tests for {@link ClassPathPatternCqlScript}.
 *
 * @author Dmytro Nosan
 */
class ClassPathPatternCqlScriptTests {

	private static final String ROLE = "CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)";

	private static final String KEYSPACE = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = "
			+ "{'class':'SimpleStrategy', 'replication_factor':1}";

	static Stream<Arguments> patterns() {
		List<Arguments> parameters = new ArrayList<>();
		parameters.add(arguments("/.cql", new String[0]));
		parameters.add(arguments("/*.cql", new String[]{ROLE}));
		parameters.add(arguments("**/**.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("**/*.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("**.cql", new String[]{KEYSPACE, ROLE}));
		parameters.add(arguments("**{roles,keyspace}.cql", new String[]{KEYSPACE, ROLE}));
		parameters.add(arguments("{roles,keyspace}.cql", new String[]{ROLE}));
		parameters.add(arguments("**/{roles,keyspace}.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("**{keyspace}.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("*/*.cql", new String[0]));
		parameters.add(arguments("**/key*.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("**\\key*.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("com/*/*/embe*ed/**/keyspa?e.cql", new String[]{KEYSPACE}));
		parameters.add(arguments("roles.cql", new String[]{ROLE}));
		parameters.add(arguments("/roles.cql", new String[]{ROLE}));
		parameters.add(arguments("\\roles.cql", new String[]{ROLE}));
		parameters.add(arguments("*.cql", new String[]{ROLE}));
		parameters.add(arguments("rol?s.cql", new String[]{ROLE}));
		return parameters.stream();
	}

	@ParameterizedTest
	@MethodSource("patterns")
	void test(String pattern, String[] statements) {
		assertThat(new ClassPathPatternCqlScript(pattern).getStatements()).containsExactly(statements);
	}

}
