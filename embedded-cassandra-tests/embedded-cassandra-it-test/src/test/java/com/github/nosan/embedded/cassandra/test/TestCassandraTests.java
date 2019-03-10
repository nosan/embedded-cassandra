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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactoryBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
public class TestCassandraTests {

	private static final TestCassandra cassandra = new TestCassandra(
			new LocalCassandraFactoryBuilder()
					.setConfigurationFile(TestCassandraTests.class.getResource("/cassandra.yaml"))
					.setJvmOptions("-Dcassandra.superuser_setup_delay_ms=0")
					.build());

	private static final String KEYSPACE_NAME = "test";

	@BeforeClass
	public static void startCassandra() {
		cassandra.start();
	}

	@AfterClass
	public static void stopCassandra() {
		cassandra.stop();
	}

	@After
	public void deleteKeyspace() {
		cassandra.dropKeyspaces(KEYSPACE_NAME);
	}

	@Before
	public void createKeyspace() {
		cassandra.executeScripts(CqlScript.classpath("init.cql"));
	}

	@Test
	public void dropTables() {
		KeyspaceMetadata keyspace = getKeyspace(KEYSPACE_NAME);
		assertThat(keyspace).isNotNull();
		assertThat(keyspace.getTable("users")).isNotNull();
		cassandra.dropTables("test.users");
		assertThat(keyspace.getTable("users")).isNull();
	}

	@Test
	public void getCount() {
		assertThat(cassandra.getRowCount("test.users")).isEqualTo(1);
	}

	@Test
	public void deleteFromTables() {
		assertThat(cassandra.getRowCount("test.users")).isEqualTo(1);
		cassandra.deleteFromTables("test.users");
		assertThat(cassandra.getRowCount("test.users")).isEqualTo(0);
	}

	@Test
	public void executeStatement() {
		Row row = cassandra.executeStatement("SELECT * FROM test.users WHERE user_id = ?", "frodo").one();
		assertString(row, "first_name", "$'Frodo;'");
		assertString(row, "last_name", "'$$Baggins");

		Row row1 = cassandra.executeStatement(QueryBuilder.select("first_name").from("test", "users").limit(1))
				.one();
		assertString(row1, "first_name", "$'Frodo;'");
	}

	private static KeyspaceMetadata getKeyspace(String name) {
		return cassandra.getCluster().getMetadata().getKeyspace(name);
	}

	private static void assertString(Row row, String column, String value) {
		assertThat(row.get(column, String.class)).isEqualTo(value);
	}

}
