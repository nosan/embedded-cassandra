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

package com.github.nosan.embedded.cassandra.junit;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import org.junit.ClassRule;
import org.junit.Test;

/**
 *
 * Tests for {@link EmbeddedCassandraServer}.
 *
 * @author Dmytro Nosan
 */
public class EmbeddedCassandraServerTests {

	@ClassRule
	public static EmbeddedCassandraServer embeddedCassandraServer = new EmbeddedCassandraServer();

	private static void keyspace(String keyspace, Session session) {
		session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace
				+ "  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
	}

	private static void table(String keyspace, String table, Session session) {
		session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + "." + table + " ( "
				+ "  id text PRIMARY KEY )");
	}

	private static Cluster cluster(CassandraConfig config) {
		return Cluster.builder().addContactPoint(config.getListenAddress())
				.withPort(config.getNativeTransportPort()).build();
	}

	@Test
	public void testRule() {
		CassandraConfig config = embeddedCassandraServer.getConfig();
		try (Cluster cluster = cluster(config)) {
			Session session = cluster.connect();
			keyspace("boot", session);
			table("boot", "user", session);
		}

	}

}
