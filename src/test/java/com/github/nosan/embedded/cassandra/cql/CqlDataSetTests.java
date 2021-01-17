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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlDataSet}
 *
 * @author Dmytro Nosan
 */
class CqlDataSetTests {

	@Test
	void ofResources() {
		CqlDataSet dataSet = CqlDataSet.ofResources(new ClassPathResource("schema.cql"));
		assertStatements(dataSet);
		assertThat(dataSet.getScripts()).hasSize(1);
	}

	@Test
	void ofClassPaths() {
		CqlDataSet dataSet = CqlDataSet.ofClassPaths("schema.cql");
		assertStatements(dataSet);
		assertThat(dataSet.getScripts()).hasSize(1);
	}

	@Test
	void ofScripts() {
		CqlDataSet d1 = CqlDataSet.ofScripts(CqlDataSet.ofClassPaths("schema.cql"));
		assertStatements(d1);
		assertThat(d1.getScripts()).hasSize(1);
	}

	@Test
	void ofScripts1() {
		CqlDataSet d1 = CqlDataSet.ofScripts(CqlScript.ofClassPath("schema.cql"));
		assertStatements(d1);
		assertThat(d1.getScripts()).hasSize(1);
	}

	@Test
	void forEachScript() {
		List<CqlScript> scripts = new ArrayList<>();
		CqlDataSet.ofClassPaths("schema.cql").forEachScript(scripts::add);
		assertThat(scripts).hasSize(1);
	}

	@Test
	void forEachStatements() {
		List<String> statements = new ArrayList<>();
		CqlDataSet.ofClassPaths("schema.cql").forEachStatement(statements::add);
		assertThat(statements).hasSize(1);
	}

	private static void assertStatements(CqlDataSet dataSet) {
		assertThat(dataSet.getStatements()).containsExactly(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
	}

}
