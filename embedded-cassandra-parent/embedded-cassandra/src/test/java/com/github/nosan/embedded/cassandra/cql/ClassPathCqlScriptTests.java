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

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ClassPathCqlScript}.
 *
 * @author Dmytro Nosan
 */
class ClassPathCqlScriptTests {

	@Test
	void getStatementsWithClassloader() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("roles.cql");
		assertThat(classPathCqlScript.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void getStatementsWithClassLoaderAndLeadingSlash() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("/roles.cql");
		assertThat(classPathCqlScript.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void getStatementsWithClass() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("keyspace.cql", getClass());
		assertThat(classPathCqlScript.getStatements()).containsExactly(
				"CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class':'SimpleStrategy', " +
						"'replication_factor':1}");
	}

	@Test
	void getStatementsWithLeadingClass() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("/roles.cql", getClass());
		assertThat(classPathCqlScript.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void helpers() {
		assertThat(new ClassPathCqlScript("\\roles.cql", getClass()))
				.isEqualTo(new ClassPathCqlScript("/roles.cql"));

		assertThat(new ClassPathCqlScript("/hz.cql", getClass()))
				.isEqualTo(new ClassPathCqlScript("/hz.cql"));

		assertThat(new ClassPathCqlScript("/roles.cql", getClass(), StandardCharsets.UTF_16LE))
				.isNotEqualTo(new ClassPathCqlScript("/roles.cql", getClass()));

		assertThat(new ClassPathCqlScript("roles.cql", getClass()).toString())
				.isEqualTo("com/github/nosan/embedded/cassandra/cql/roles.cql");
		assertThat(new ClassPathCqlScript("/roles.cql", getClass()).toString())
				.isEqualTo("roles.cql");
		assertThat(new ClassPathCqlScript("/roles.cql").toString())
				.isEqualTo("roles.cql");
	}

	@Test
	void invalidResource() {
		assertThatThrownBy(() -> new ClassPathCqlScript("/hz.cql").getStatements())
				.isInstanceOf(UncheckedIOException.class);
	}

}
