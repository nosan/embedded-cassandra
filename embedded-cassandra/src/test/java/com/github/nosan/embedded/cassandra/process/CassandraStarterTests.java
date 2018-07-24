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
import java.net.ConnectException;
import java.net.Socket;
import java.time.Duration;

import com.datastax.driver.core.Cluster;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.JvmOptions;
import com.github.nosan.embedded.cassandra.cql.ClassPathCqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * Tests for {@link CassandraStarter}.
 *
 * @author Dmytro Nosan
 */
public class CassandraStarterTests {

	private static final Logger log = LoggerFactory
			.getLogger(CassandraStarterTests.class);

	@Rule
	public ExpectedException throwable = ExpectedException.none();

	@Test
	public void shouldStartCassandraWithNativeTransport() throws Exception {
		ExecutableConfig executableConfig = execBuilder().build();
		run(executableConfig, () -> {
			try (Cluster cluster = cluster(executableConfig.getConfig())) {
				CqlScriptUtils.executeScripts(cluster.connect(), new ClassPathCqlScript("init.cql"));
			}
		});
	}

	@Test
	public void shouldBeRestartedUsingNativeTransportPort() throws Exception {
		ExecutableConfig executableConfig =
				execBuilder().build();
		Config config = new Config();
		config.setNativeTransportPort(9042);
		executableConfig.setConfig(config);
		run(executableConfig);
		run(executableConfig);
	}

	@Test
	public void shouldBePossibleToStartMultiplyInstances() throws Exception {
		run(execBuilder().build(), () -> run(execBuilder().build()));
	}

	@Test
	public void shouldFailWithInvalidConfigurationError() throws Exception {
		this.throwable.expect(IOException.class);
		this.throwable.expectMessage("Missing required directive CommitLogSync");
		ExecutableConfig executableConfig = execBuilder().build();
		executableConfig.getConfig().setCommitlogSync(null);
		run(executableConfig);
	}

	@Test
	public void shouldFailWithTimeoutError() throws Exception {
		this.throwable.expect(IOException.class);
		this.throwable.expectMessage("Could not start a process");
		ExecutableConfig executableConfig = new ExecutableConfigBuilder()
				.timeout(Duration.ofSeconds(1)).build();
		run(executableConfig);
	}

	@Test
	public void shouldStartCassandraUsingRpcTransport() throws Exception {
		ExecutableConfig executableConfig = execBuilder().build();
		Config config = executableConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(true);
		run(executableConfig, () -> new Socket(executableConfig.getConfig().getRpcAddress(),
				executableConfig.getConfig().getRpcPort()));
	}

	@Test
	public void shouldStartCassandraWithDisableRpcAndNativeTransportPorts()
			throws Exception {
		this.throwable.expect(ConnectException.class);
		this.throwable.expectMessage("Connection refused");
		ExecutableConfig executableConfig = execBuilder().build();
		Config config = executableConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(false);
		run(executableConfig, () -> new Socket(executableConfig.getConfig().getRpcAddress(),
				executableConfig.getConfig().getRpcPort()));

	}

	private static ExecutableConfigBuilder execBuilder() {
		return new ExecutableConfigBuilder().jvmOptions(new JvmOptions("-Xmx384m", "-Xms384m"));
	}

	private static Cluster cluster(Config config) {
		return Cluster.builder().addContactPoint(config.getRpcAddress())
				.withPort(config.getNativeTransportPort()).build();
	}

	private static void run(ExecutableConfig executableConfig) throws Exception {
		run(executableConfig, () -> {
		});
	}

	private static void run(ExecutableConfig executableConfig, Callback callback)
			throws Exception {
		CassandraExecutable executable =
				new CassandraStarter(new RuntimeConfigBuilder(log).build()).prepare(executableConfig);
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
