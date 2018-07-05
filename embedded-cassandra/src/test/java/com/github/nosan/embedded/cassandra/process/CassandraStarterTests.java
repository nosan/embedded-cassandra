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

package com.github.nosan.embedded.cassandra.process;

import java.io.IOException;
import java.time.Duration;

import com.datastax.driver.core.Cluster;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.CassandraConfig;
import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.support.CassandraConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * Tests for {@link CassandraStarter}.
 *
 * @author Dmytro Nosan
 */
public class CassandraStarterTests {

	private static final Logger log = LoggerFactory
			.getLogger(CassandraStarterTests.class);

	private static Cluster cluster(Config config) {
		return Cluster.builder().addContactPoint(config.getListenAddress())
				.withPort(config.getNativeTransportPort()).build();
	}

	@Test
	public void nativeTransport() throws Exception {
		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		start(executableConfig, () -> {
			try (Cluster cluster = cluster(executableConfig.getConfig())) {
				cluster.connect().execute("CREATE KEYSPACE IF NOT EXISTS test  "
						+ "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
			}
		});
	}

	@Test
	public void startStop() throws Exception {
		start(new CassandraConfigBuilder().build());
		start(new CassandraConfigBuilder().build());
	}

	@Test
	public void multiplyInstancesShouldWorkRandomPorts() throws Exception {
		start(new CassandraConfigBuilder().useRandomPorts(true).build(),
				() -> start(new CassandraConfigBuilder().useRandomPorts(true).build()));
	}

	@Test(expected = IOException.class)
	public void multiplyInstancesShouldNotWork() throws Exception {
		start(new CassandraConfigBuilder().build(),
				() -> start(new CassandraConfigBuilder().build()));
	}

	@Test(expected = IOException.class)
	public void multiplyInstancesDoesNotWorkRandomPortNotEnable() throws Exception {
		start(new CassandraConfigBuilder().build(),
				() -> start(new CassandraConfigBuilder().build()));
	}

	@Test(expected = IOException.class)
	public void invalidConfig() throws Exception {
		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		executableConfig.getConfig().setCommitlogSync(null);
		start(executableConfig);
	}

	@Test(expected = IOException.class)
	public void timeout() throws Exception {
		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.timeout(Duration.ofSeconds(1)).build();
		start(executableConfig);
	}

	@Test
	public void rpcTransport() throws Exception {
		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		Config config = executableConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(true);
		start(executableConfig);
	}

	@Test
	public void disableTransport() throws Exception {
		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		Config config = executableConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(false);

		start(executableConfig);

	}

	private static void start(CassandraConfig cassandraConfig) throws Exception {
		start(cassandraConfig, () -> {
		});
	}

	private static void start(CassandraConfig cassandraConfig, Callback callback)
			throws Exception {
		CassandraExecutable executable = new CassandraStarter(
				new RuntimeConfigBuilder(log).build())
						.prepare(new ExecutableConfig(cassandraConfig));
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
