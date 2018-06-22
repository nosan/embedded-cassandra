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
import com.github.nosan.embedded.cassandra.config.CassandraConfigBuilder;
import com.github.nosan.embedded.cassandra.config.Config;
import com.github.nosan.embedded.cassandra.customizer.JVMOptionsFileCustomizer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link CassandraStarter}.
 *
 * @author Dmytro Nosan
 */
public class CassandraStarterTests {

	private static final Logger log = LoggerFactory
			.getLogger(CassandraStarterTests.class);

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
	public void nativeTransport() throws Exception {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		start(cassandraConfig, () -> {
			try (Cluster cluster = cluster(cassandraConfig.getConfig())) {
				Session session = cluster.connect();
				keyspace("test", session);
				table("test", "user", session);
			}
		});

	}

	@Test
	public void multiplyInstances() throws Exception {
		start(new CassandraConfigBuilder().useRandomPorts(true).build(),
				() -> start(new CassandraConfigBuilder().useRandomPorts(true).build()));
	}

	@Test(expected = IOException.class)
	public void multiplyInstancesDoesNotWorkJmxNotDisable() throws Exception {
		start(new CassandraConfigBuilder().useRandomPorts(true)
				.fileCustomizer(new JVMOptionsFileCustomizer()).build(),
				() -> start(new CassandraConfigBuilder().useRandomPorts(true)
						.fileCustomizer(new JVMOptionsFileCustomizer()).build()));
	}

	@Test(expected = IOException.class)
	public void multiplyInstancesDoesNotWorkRandomPortNotEnable() throws Exception {
		start(new CassandraConfigBuilder().build(),
				() -> start(new CassandraConfigBuilder().build()));
	}

	@Test(expected = IOException.class)
	public void invalidConfig() throws Exception {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		cassandraConfig.getConfig().setCommitlogSync(null);
		start(cassandraConfig);
	}

	@Test(expected = IOException.class)
	public void timeout() throws Exception {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.timeout(Duration.ofSeconds(1)).build();
		start(cassandraConfig);
	}

	@Test
	public void rpcTransport() throws Exception {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		Config config = cassandraConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(true);
		start(cassandraConfig);
	}

	@Test
	public void disableTransport() throws Exception {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		Config config = cassandraConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(false);

		start(cassandraConfig);

	}

	private static void start(CassandraConfig cassandraConfig) throws Exception {
		start(cassandraConfig, () -> {
		});
	}

	private static void start(CassandraConfig cassandraConfig, Callback callback)
			throws Exception {
		CassandraExecutable executable = new CassandraStarter(log)
				.prepare(cassandraConfig);
		try {
			executable.start();
			callback.run();
		}
		finally {
			executable.stop();
		}
	}

	interface Callback {

		void run() throws Exception;

	}

}
