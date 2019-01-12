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

import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassPathGlobCqlScript}.
 *
 * @author Dmytro Nosan
 */
@RunWith(Parameterized.class)
public class ClassPathGlobCqlScriptTests {

	private static final String ROLE = "CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)";

	private static final String KEYSPACE =
			"CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}";

	@Nonnull
	private final String pattern;

	@Nonnull
	private final String name;

	@Nonnull
	private final String[] statements;

	public ClassPathGlobCqlScriptTests(AssertData assertData) {
		this.pattern = assertData.pattern;
		this.name = assertData.name;
		this.statements = assertData.statements;
	}

	@Test
	public void test() {
		ClassPathGlobCqlScript script = new ClassPathGlobCqlScript(this.pattern);
		assertThat(script.toString()).isEqualTo(this.name);
		assertThat(script).isNotEqualTo(new ClassPathGlobCqlScript("sometext"));
		assertThat(script).isEqualTo(script);
		assertThat(script.getStatements()).containsExactly(this.statements);
	}

	@Parameterized.Parameters(name = "{index} {0}")
	public static Iterable<AssertData> patterns() {
		List<AssertData> parameters = new ArrayList<>();
		parameters.add(new AssertData("/.cql", ".cql"));
		parameters.add(new AssertData("/*.cql", "*.cql", new String[]{ROLE}));
		parameters.add(new AssertData("**/**.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("**/*.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("**.cql", new String[]{KEYSPACE, ROLE}));
		parameters.add(new AssertData("**{roles,keyspace}.cql", new String[]{KEYSPACE, ROLE}));
		parameters.add(new AssertData("{roles,keyspace}.cql", new String[]{ROLE}));
		parameters.add(new AssertData("**/{roles,keyspace}.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("**{keyspace}.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("*/*.cql"));
		parameters.add(new AssertData("**/key*.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("**\\key*.cql", "**/key*.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("com/*/*/embe*ed/**/keyspa?e.cql", new String[]{KEYSPACE}));
		parameters.add(new AssertData("roles.cql", new String[]{ROLE}));
		parameters.add(new AssertData("/roles.cql", "roles.cql", new String[]{ROLE}));
		parameters.add(new AssertData("\\roles.cql", "roles.cql", new String[]{ROLE}));
		parameters.add(new AssertData("*.cql", new String[]{ROLE}));
		parameters.add(new AssertData("rol?s.cql", new String[]{ROLE}));
		return parameters;
	}

	private static final class AssertData {

		@Nonnull
		private final String[] statements;

		@Nonnull
		private final String pattern;

		@Nonnull
		private final String name;

		AssertData(@Nonnull String pattern, @Nonnull String name, @Nonnull String[] statements) {
			this.pattern = pattern;
			this.name = name;
			this.statements = statements;
		}

		AssertData(@Nonnull String pattern, @Nonnull String name) {
			this(pattern, name, new String[0]);
		}

		AssertData(@Nonnull String pattern, @Nonnull String[] statements) {
			this(pattern, pattern, statements);
		}

		AssertData(@Nonnull String pattern) {
			this(pattern, pattern, new String[0]);
		}

		@Override
		@Nonnull
		public String toString() {
			return String.format("(%s) (%s) (%s) statements", this.pattern, this.name, this.statements.length);
		}
	}
}
