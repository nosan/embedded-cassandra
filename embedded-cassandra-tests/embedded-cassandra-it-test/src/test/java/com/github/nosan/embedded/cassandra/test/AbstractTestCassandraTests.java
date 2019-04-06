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

package com.github.nosan.embedded.cassandra.test;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.github.nosan.embedded.cassandra.cql.CqlScript;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTestCassandraTests {

	private final TestCassandra cassandra;

	AbstractTestCassandraTests(TestCassandra cassandra) {
		this.cassandra = cassandra;
	}

	@BeforeAll
	void startCassandra() {
		this.cassandra.start();
	}

	@AfterAll
	void stopCassandra() {
		this.cassandra.stop();
	}

	@BeforeEach
	void initAllKeyspaces() {
		this.cassandra.executeScripts(CqlScript.classpath("init.cql"));
	}

	@AfterEach
	void dropAllKeyspaces() {
		this.cassandra.dropAllNonSystemKeyspaces();
	}

	@Test
	void deleteFromAllNonSystemTables() {
		assertThat(this.cassandra.getRowCount("test.users")).isEqualTo(1);
		assertThat(this.cassandra.getRowCount("test.roles")).isEqualTo(1);
		this.cassandra.deleteFromAllNonSystemTables();
		assertThat(this.cassandra.getRowCount("test.users")).isZero();
		assertThat(this.cassandra.getRowCount("test.roles")).isZero();
	}

	@Test
	void dropAllNonSystemTables() {
		assertThat(this.cassandra.getRowCount("test.users")).isEqualTo(1);
		assertThat(this.cassandra.getRowCount("test.roles")).isEqualTo(1);
		this.cassandra.dropAllNonSystemTables();
		KeyspaceMetadata keyspace = this.cassandra.getCluster().getMetadata().getKeyspace("test");
		assertThat(keyspace).isNotNull();
		assertThat(keyspace.getTables()).isEmpty();
	}

	@Test
	void dropKeyspaces() {
		this.cassandra.dropKeyspaces("test");
		KeyspaceMetadata keyspace = this.cassandra.getCluster().getMetadata().getKeyspace("test");
		assertThat(keyspace).isNull();
	}

	@Test
	void dropTables() {
		KeyspaceMetadata keyspace = this.cassandra.getCluster().getMetadata().getKeyspace("test");
		assertThat(keyspace).isNotNull();
		assertThat(keyspace.getTable("users")).isNotNull();
		this.cassandra.dropTables("test.users");
		assertThat(keyspace.getTable("users")).isNull();
	}

	@Test
	void getCount() {
		assertThat(this.cassandra.getRowCount("test.users")).isEqualTo(1);
	}

	@Test
	void deleteFromTables() {
		assertThat(this.cassandra.getRowCount("test.users")).isEqualTo(1);
		this.cassandra.deleteFromTables("test.users");
		assertThat(this.cassandra.getRowCount("test.users")).isEqualTo(0);
	}

	@Test
	void statement() {
		Row row = this.cassandra.executeStatement("SELECT * FROM test.users WHERE user_id = ?", "frodo").one();
		assertColumnValue(row, "first_name", "Frodo");
		assertColumnValue(row, "last_name", "Baggins");

		Row row1 = this.cassandra.executeStatement(QueryBuilder.select("first_name").from("test", "users").limit(1))
				.one();
		assertColumnValue(row1, "first_name", "Frodo");
	}

	private static void assertColumnValue(Row row, String column, String value) {
		assertThat(row).isNotNull();
		assertThat(row.get(column, String.class)).isEqualTo(value);
	}

}
