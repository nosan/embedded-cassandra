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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.time.Duration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import org.junit.Test;

/**
 * Tests for {@link CassandraServerStarter}.
 *
 * @author Dmytro Nosan
 */
public class CassandraServerStarterTests {

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

	private static CassandraProcessConfig config() {
		CassandraProcessConfig cassandraProcessConfig = new CassandraProcessConfig();
		CassandraConfig cassandraConfig = new CassandraConfig();
		CassandraPortUtils.setRandomPorts(cassandraConfig);
		cassandraProcessConfig.setConfig(cassandraConfig);
		return cassandraProcessConfig;

	}

	@Test
	public void nativeTransport() throws Exception {
		CassandraProcessConfig config = config();
		CassandraServerStarter cassandraServerStarter = new CassandraServerStarter();
		cassandraServerStarter.prepare(config).start();
		try (Cluster cluster = cluster(config.getConfig())) {
			Session session = cluster.connect();
			keyspace("test", session);
			table("test", "user", session);
		}

	}

	@Test
	public void multiplyInstances() throws IOException {
		new CassandraServerStarter().prepare(config()).start();
		new CassandraServerStarter().prepare(config()).start();
	}

	@Test(expected = IOException.class)
	public void invalidConfig() throws IOException {
		CassandraProcessConfig config = config();
		config.getConfig().setCommitlogSync(null);
		new CassandraServerStarter().prepare(config).start();
	}

	@Test(expected = IOException.class)
	public void timeout() throws IOException {
		CassandraProcessConfig config = config();
		config.setTimeout(Duration.ofSeconds(1));
		new CassandraServerStarter().prepare(config).start();
	}

	@Test
	public void rpcTransport() throws IOException {
		CassandraProcessConfig cassandraProcessConfig = config();
		CassandraConfig config = cassandraProcessConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(true);
		new CassandraServerStarter().prepare(cassandraProcessConfig).start();
	}

	@Test
	public void disableTransport() throws IOException {
		CassandraProcessConfig cassandraProcessConfig = config();
		CassandraConfig config = cassandraProcessConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(false);
		new CassandraServerStarter().prepare(cassandraProcessConfig).start();
	}

}
