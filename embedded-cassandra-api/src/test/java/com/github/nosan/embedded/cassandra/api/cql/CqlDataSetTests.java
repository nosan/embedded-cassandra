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

package com.github.nosan.embedded.cassandra.api.cql;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlDataSet}.
 *
 * @author Dmytro Nosan
 */
class CqlDataSetTests {

	@Test
	void ofStrings() {
		CqlDataSet dataSet = CqlDataSet.ofStrings(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };",
				"CREATE TABLE test.roles ( id text PRIMARY KEY );");
		assertStatements(dataSet);
		assertThat(dataSet.getScripts()).hasSize(2);
	}

	@Test
	void ofResources() {
		CqlDataSet dataSet = CqlDataSet.ofResources(new ClassPathResource("schema.cql"));
		assertStatements(dataSet);
		assertThat(dataSet.getScripts()).hasSize(1);
	}

	@Test
	void ofClasspaths() {
		CqlDataSet dataSet = CqlDataSet.ofClasspaths("schema.cql");
		assertStatements(dataSet);
		assertThat(dataSet.getScripts()).hasSize(1);
	}

	@Test
	void testAdd() {
		CqlDataSet d1 = CqlDataSet.ofStrings(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
		CqlDataSet d2 = CqlDataSet.ofStrings("CREATE TABLE test.roles ( id text PRIMARY KEY )");
		CqlDataSet d3 = d1.add(d2);
		assertStatements(d3);
		assertThat(d3.getScripts()).hasSize(2);
	}

	@Test
	void ofScripts() {
		CqlDataSet d1 = CqlDataSet.ofScripts(
				CqlScript.ofString(
						"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy',"
								+ " 'replication_factor' : 1 };"),
				CqlDataSet.ofStrings("CREATE TABLE test.roles ( id text PRIMARY KEY )"));
		assertStatements(d1);
		assertThat(d1.getScripts()).hasSize(2);
	}

	@Test
	void ofEmpty() {
		assertThat(CqlDataSet.empty().getScripts()).isEmpty();
	}

	@Test
	void forEachScript() {
		List<CqlScript> scripts = new ArrayList<>();
		CqlDataSet.ofClasspaths("schema.cql").forEachScript(scripts::add);
		assertThat(scripts).hasSize(1);
	}

	@Test
	void forEachStatements() {
		List<String> statements = new ArrayList<>();
		CqlDataSet.ofClasspaths("schema.cql").forEachStatement(statements::add);
		assertThat(statements).hasSize(2);
	}

	private static void assertStatements(CqlDataSet dataSet) {
		assertThat(dataSet.getStatements()).containsExactly(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }",
				"CREATE TABLE test.roles ( id text PRIMARY KEY )");
	}

}
