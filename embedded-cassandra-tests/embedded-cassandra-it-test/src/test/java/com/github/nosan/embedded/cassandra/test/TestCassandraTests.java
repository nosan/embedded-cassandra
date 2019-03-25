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

package com.github.nosan.embedded.cassandra.test;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactoryBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
class TestCassandraTests {

	private static final TestCassandra cassandra = new TestCassandra(
			new LocalCassandraFactoryBuilder()
					.setDeleteWorkingDirectory(true)
					.setConfigurationFile(TestCassandraTests.class.getResource("/cassandra.yaml"))
					.setJvmOptions("-Dcassandra.superuser_setup_delay_ms=1850")
					.build());

	private static final String KEYSPACE_NAME = "test";

	@BeforeAll
	static void startCassandra() {
		cassandra.start();
	}

	@AfterAll
	static void stopCassandra() {
		cassandra.stop();
	}

	@AfterEach
	void deleteKeyspace() {
		cassandra.dropKeyspaces(KEYSPACE_NAME);
	}

	@BeforeEach
	void createKeyspace() {
		cassandra.executeScripts(CqlScript.classpath("init.cql"));
	}

	@Test
	void dropTables() {
		KeyspaceMetadata keyspace = getKeyspace();
		assertThat(keyspace).isNotNull();
		assertThat(keyspace.getTable("users")).isNotNull();
		cassandra.dropTables("test.users");
		assertThat(keyspace.getTable("users")).isNull();
	}

	@Test
	void getCount() {
		assertThat(cassandra.getRowCount("test.users")).isEqualTo(1);
	}

	@Test
	void deleteFromTables() {
		assertThat(cassandra.getRowCount("test.users")).isEqualTo(1);
		cassandra.deleteFromTables("test.users");
		assertThat(cassandra.getRowCount("test.users")).isEqualTo(0);
	}

	@Test
	void executeStatement() {
		Row row = cassandra.executeStatement("SELECT * FROM test.users WHERE user_id = ?", "frodo").one();
		assertColumnValue(row, "first_name", "$'Frodo;'");
		assertColumnValue(row, "last_name", "'$$Baggins");

		Row row1 = cassandra.executeStatement(QueryBuilder.select("first_name").from("test", "users").limit(1))
				.one();
		assertColumnValue(row1, "first_name", "$'Frodo;'");
	}

	private static KeyspaceMetadata getKeyspace() {
		return cassandra.getCluster().getMetadata().getKeyspace(TestCassandraTests.KEYSPACE_NAME);
	}

	private static void assertColumnValue(Row row, String column, String value) {
		assertThat(row.get(column, String.class)).isEqualTo(value);
	}

}
