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

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScripts}.
 *
 * @author Dmytro Nosan
 */
public class CqlScriptsTests {

	@Test
	public void getStatements() {
		CqlScripts cqlScripts =
				new CqlScripts(new ClassPathCqlScript("keyspace.cql", getClass()), new ClassPathCqlScript("roles.cql"));

		assertThat(cqlScripts.getStatements())
				.hasSize(2)
				.contains("CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION " +
						"= {'class':'SimpleStrategy', 'replication_factor':1}")
				.contains("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	public void helpers() {
		assertThat(new CqlScripts(new ClassPathCqlScript("keyspace.cql", getClass()), new ClassPathCqlScript(
				"roles.cql")))
				.isEqualTo(new CqlScripts(Arrays.asList(new ClassPathCqlScript("keyspace.cql", getClass()),
						new ClassPathCqlScript("roles.cql"))));

		assertThat(new CqlScripts(new ClassPathCqlScript("keyspace.cql", getClass()), new ClassPathCqlScript(
				"roles.cql")).toString()).contains("keyspace.cql").contains("roles.cql");

	}

}
