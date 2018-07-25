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

import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.Socket;
import java.time.Duration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.nosan.embedded.cassandra.cql.ClassPathCqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;

/**
 * Tests for {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
public class CassandraTests {


	@Rule
	public ExpectedException throwable = ExpectedException.none();

	@Test
	public void shouldNotStartCassandraIfCassandraHasBeenAlreadyStarted() throws Exception {
		this.throwable.expect(IllegalStateException.class);
		this.throwable.expectMessage("Cassandra has already been initialized");
		Cassandra cassandra = new Cassandra(execBuilder().build());
		run(cassandra, cassandra::start);
	}

	@Test
	public void shouldBeAbleToRestartCassandra() throws Exception {
		Cassandra cassandra = new Cassandra(execBuilder().build());
		run(cassandra, () -> executeScripts(cassandra));
		run(cassandra, () -> executeScripts(cassandra));
	}


	@Test
	public void shouldBePossibleToStartMultiplyInstances() throws Exception {
		Cassandra cassandra = new Cassandra(execBuilder().build());
		run(cassandra, () -> {
			executeScripts(cassandra);
			Cassandra cassandra1 = new Cassandra(execBuilder().build());
			run(cassandra1, () -> executeScripts(cassandra1));
		});
	}

	@Test
	public void shouldFailWithInvalidConfigurationError() {
		this.throwable.expect(UncheckedIOException.class);
		this.throwable.expectMessage("Missing required directive CommitLogSync");
		ExecutableConfig executableConfig = execBuilder().build();
		executableConfig.getConfig().setCommitlogSync(null);
		run(new Cassandra(executableConfig));
	}

	@Test
	public void shouldFailWithTimeoutError() {
		this.throwable.expect(UncheckedIOException.class);
		this.throwable.expectMessage("Please increase startup timeout");
		ExecutableConfig executableConfig = new ExecutableConfigBuilder()
				.timeout(Duration.ofSeconds(1)).build();
		run(new Cassandra(executableConfig));
	}

	@Test
	public void shouldStartCassandraUsingRpcTransport() throws Exception {
		ExecutableConfig executableConfig = execBuilder().build();
		Config config = executableConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(true);
		run(new Cassandra(executableConfig), () -> new Socket(executableConfig.getConfig().getRpcAddress(),
				executableConfig.getConfig().getRpcPort()));
	}

	@Test
	public void shouldStartCassandraWithDisableRpcAndNativeTransportPorts() throws Exception {
		this.throwable.expect(ConnectException.class);
		this.throwable.expectMessage("Connection refused");
		ExecutableConfig executableConfig = execBuilder().build();
		Config config = executableConfig.getConfig();
		config.setStartNativeTransport(false);
		config.setStartRpc(false);
		run(new Cassandra(executableConfig), () -> new Socket(executableConfig.getConfig().getRpcAddress(),
				executableConfig.getConfig().getRpcPort()));

	}

	private static ExecutableConfigBuilder execBuilder() {
		return new ExecutableConfigBuilder().jvmOptions(new JvmOptions("-Xmx384m", "-Xms384m"));
	}


	private void executeScripts(Cassandra cassandra) {
		CqlScriptUtils.executeScripts(cassandra.getSession(), new ClassPathCqlScript("init.cql"));
	}

	private static void run(Cassandra cassandra) {
		try {
			cassandra.start();
		}
		finally {
			cassandra.stop();
		}
	}

	private static void run(Cassandra cassandra, ExceptionRunnable runnable) throws Exception {
		try {
			cassandra.start();
			runnable.run();
		}
		finally {
			cassandra.stop();
		}
	}

	interface ExceptionRunnable {
		void run() throws Exception;
	}


}
