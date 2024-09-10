/*
 * Copyright 2020-2024 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlDataSet.Builder}.
 *
 * @author Dmytro Nosan
 */
class DefaultCqlDataSetBuilderTests {

	private static final String CQL_SCRIPT =
			"CREATE KEYSPACE test WITH REPLICATION = " + "{ 'class' : 'SimpleStrategy', 'replication_factor' : 1 }";

	@Test
	void shouldBuildCqlDataSet() {
		CqlDataSet dataSet = CqlDataSet.builder().addStatements("1", "2", "3").addStatements(List.of("4", "5"))
				.addScript(CQL_SCRIPT).addScript(new StringCqlScript(CQL_SCRIPT))
				.addResource(new ClassPathResource("schema.cql"))
				.addResource(new ClassPathResource("schema.cql"), StandardCharsets.UTF_8).build();
		List<String> statements = dataSet.getStatements();
		assertThat(statements).containsExactly("1", "2", "3", "4", "5", CQL_SCRIPT, CQL_SCRIPT, CQL_SCRIPT, CQL_SCRIPT);
	}

}
