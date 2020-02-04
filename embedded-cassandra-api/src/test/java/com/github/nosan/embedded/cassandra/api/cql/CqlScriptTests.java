/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.api.cql;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScript}.
 *
 * @author Dmytro Nosan
 */
class CqlScriptTests {

	@Test
	void ofStrings() {
		CqlScript script = CqlScript.ofString("CREATE TABLE test.roles ( id text PRIMARY KEY );");
		assertThat(script.getStatements()).containsExactly("CREATE TABLE test.roles ( id text PRIMARY KEY )");
	}

	@Test
	void ofResource() {
		CqlScript script = CqlScript.ofResource(new ClassPathResource("schema.cql"));
		assertThat(script.getStatements()).containsExactly(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }",
				"CREATE TABLE test.roles ( id text PRIMARY KEY )");
	}

	@Test
	void ofClasspaths() {
		CqlScript script = CqlScript.ofClasspath("schema.cql");
		assertThat(script.getStatements()).containsExactly(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }",
				"CREATE TABLE test.roles ( id text PRIMARY KEY )");
	}

	@Test
	void forEachStatements() {
		List<String> statements = new ArrayList<>();
		CqlScript.ofClasspath("schema.cql").forEachStatement(statements::add);
		assertThat(statements).hasSize(2);
	}

}
