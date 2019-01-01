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
	private final String glob;

	@Nonnull
	private final String[] statements;

	public ClassPathGlobCqlScriptTests(@Nonnull String glob, @Nonnull Array statements) {
		this.glob = glob;
		this.statements = statements.statements;
	}

	@Test
	public void assertStatements() {
		ClassPathGlobCqlScript classPathCqlScript = new ClassPathGlobCqlScript(this.glob);
		assertThat(classPathCqlScript.getStatements()).containsExactly(this.statements);
	}

	@Test
	public void assertEquals() {
		ClassPathGlobCqlScript script = new ClassPathGlobCqlScript(this.glob);
		assertThat(script).isEqualTo(script);
		assertThat(script).isNotEqualTo(new ClassPathGlobCqlScript("sometext"));
		assertThat(script.hashCode()).isEqualTo(script.hashCode());
		assertThat(script.toString()).isEqualTo(this.glob);
	}

	@Parameterized.Parameters(name = "{0} {1}")
	public static Iterable<Object[]> globs() {
		List<Object[]> parameters = new ArrayList<>();
		parameters.add(new Object[]{"**/**.cql", new Array(KEYSPACE)});
		parameters.add(new Object[]{"**/*.cql", new Array(KEYSPACE)});
		parameters.add(new Object[]{"**.cql", new Array(KEYSPACE, ROLE)});
		parameters.add(new Object[]{"**.cql", new Array(KEYSPACE, ROLE)});
		parameters.add(new Object[]{"**{roles,keyspace}.cql", new Array(KEYSPACE, ROLE)});
		parameters.add(new Object[]{"{roles,keyspace}.cql", new Array(ROLE)});
		parameters.add(new Object[]{"**/{roles,keyspace}.cql", new Array(KEYSPACE)});
		parameters.add(new Object[]{"**{keyspace}.cql", new Array(KEYSPACE)});
		parameters.add(new Object[]{"*/*.cql", new Array()});
		parameters.add(new Object[]{"**/key*.cql", new Array(KEYSPACE)});
		parameters.add(new Object[]{"com/*/*/embe*ed/**/keyspa?e.cql", new Array(KEYSPACE)});
		parameters.add(new Object[]{"roles.cql", new Array(ROLE)});
		parameters.add(new Object[]{"/roles.cql", new Array()});
		parameters.add(new Object[]{"*.cql", new Array(ROLE)});
		parameters.add(new Object[]{"rol?s.cql", new Array(ROLE)});
		return parameters;
	}

	private static final class Array {

		@Nonnull
		private final String[] statements;

		Array(@Nonnull String... statements) {
			this.statements = statements;
		}

		@Override
		@Nonnull
		public String toString() {
			return String.format("(%s) statements", this.statements.length);
		}
	}

}
