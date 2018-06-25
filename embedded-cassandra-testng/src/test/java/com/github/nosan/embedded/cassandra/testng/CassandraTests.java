/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.testng;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.github.nosan.embedded.cassandra.config.CassandraRuntimeConfigBuilder;
import com.github.nosan.embedded.cassandra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Tests for {@link AbstractCassandraTests}.
 *
 * @author Dmytro Nosan
 */
public class CassandraTests extends AbstractCassandraTests {

	private static final Logger log = LoggerFactory.getLogger(CassandraTests.class);

	public CassandraTests() {
		super(new CassandraRuntimeConfigBuilder(log).build());
	}

	private static void keyspace(String keyspace, Session session) {
		session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace
				+ "  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
	}

	private static void table(String keyspace, String table, Session session) {
		session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + "." + table + " ( "
				+ "  id text PRIMARY KEY )");
	}

	private static Cluster cluster(Config config) {
		return Cluster.builder().addContactPoint(config.getListenAddress())
				.withPort(config.getNativeTransportPort()).build();
	}

	@Test
	public void createUserTable() {
		try (Cluster cluster = cluster(getCassandraConfig().getConfig())) {
			Session session = cluster.connect();
			keyspace("test", session);
			table("test", "user", session);
		}
	}

	@Test
	public void createRolesTable() {
		try (Cluster cluster = cluster(getCassandraConfig().getConfig())) {
			Session session = cluster.connect();
			keyspace("test", session);
			table("test", "roles", session);
		}
	}

}
