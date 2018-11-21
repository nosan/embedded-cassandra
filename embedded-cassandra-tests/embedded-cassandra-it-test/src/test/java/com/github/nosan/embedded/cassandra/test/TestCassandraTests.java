/*
 * Copyright 2018-2018 the original author or authors.
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
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactoryBuilder;
import com.github.nosan.embedded.cassandra.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
public class TestCassandraTests {

	private static final TestCassandra cassandra = new TestCassandra(
			new LocalCassandraFactoryBuilder()
					.setConfigurationFile(ClassUtils.getClassLoader().getResource("cassandra.yaml"))
					.setJvmOptions("-Dcassandra.superuser_setup_delay_ms=0")
					.build());

	private static final String KEYSPACE_NAME = "test";

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
		ResultSet resultSet = cassandra.executeStatement("SELECT * FROM test.users WHERE user_id = ?", "frodo");
		assertThat(resultSet.one().get("first_name", String.class)).isEqualTo("Frodo");
	}

	private KeyspaceMetadata getKeyspace(String name) {
		return cassandra.getCluster().getMetadata().getKeyspace(name);
	}

	@BeforeClass
	public static void startCassandra() {
		cassandra.start();

	}

	@AfterClass
	public static void stopCassandra() {
		cassandra.stop();
	}
}
