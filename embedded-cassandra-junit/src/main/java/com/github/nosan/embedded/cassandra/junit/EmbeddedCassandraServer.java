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

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;

import com.github.nosan.embedded.cassandra.CassandraServerExecutable;
import com.github.nosan.embedded.cassandra.CassandraServerProcess;
import com.github.nosan.embedded.cassandra.CassandraServerStarter;
import com.github.nosan.embedded.cassandra.CassandraVersion;
import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for running an embedded cassandra.
 *
 * @author Dmytro Nosan
 */
public class EmbeddedCassandraServer implements TestRule {

	private final CassandraVersion version;

	private final CassandraConfig config;

	private final Duration timeout;

	private final boolean randomPorts;

	public EmbeddedCassandraServer(CassandraVersion version, CassandraConfig config,
			Duration timeout, boolean randomPorts) {
		this.version = version;
		this.config = config;
		this.timeout = timeout;
		this.randomPorts = randomPorts;
	}

	public EmbeddedCassandraServer(CassandraConfig config, Duration timeout,
			boolean randomPorts) {
		this(CassandraVersion.LATEST, config, timeout, randomPorts);
	}

	public EmbeddedCassandraServer(CassandraConfig config, Duration timeout) {
		this(CassandraVersion.LATEST, config, timeout, true);

	}

	public EmbeddedCassandraServer(CassandraConfig config, boolean randomPorts) {
		this(CassandraVersion.LATEST, config, Duration.ofMinutes(1), randomPorts);
	}

	public EmbeddedCassandraServer(CassandraConfig config) {
		this(CassandraVersion.LATEST, config, Duration.ofMinutes(1), true);
	}

	public EmbeddedCassandraServer(Duration timeout, boolean randomPorts) {
		this(CassandraVersion.LATEST, new CassandraConfig(), timeout, randomPorts);

	}

	public EmbeddedCassandraServer(boolean randomPorts) {
		this(CassandraVersion.LATEST, new CassandraConfig(), Duration.ofMinutes(1),
				randomPorts);
	}

	public EmbeddedCassandraServer(Duration timeout) {
		this(CassandraVersion.LATEST, new CassandraConfig(), timeout, true);
	}

	public EmbeddedCassandraServer() {
		this(CassandraVersion.LATEST, new CassandraConfig(), Duration.ofMinutes(1), true);
	}

	/**
	 * Retrieves an embedded cassandra config.
	 * @return Cassandra config.
	 */
	public CassandraConfig getConfig() {
		return this.config;
	}

	/**
	 * Retrieves an embedded cassandra version.
	 * @return Cassandra version.
	 */
	public CassandraVersion getVersion() {
		return this.version;
	}

	/**
	 * Retrieves startup timeout.
	 * @return Startup timeout.
	 */
	public Duration getTimeout() {
		return this.timeout;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		if (this.randomPorts) {
			setRandomPorts(this.config);
		}

		Duration timeout = this.timeout;
		CassandraVersion version = this.version;
		CassandraConfig config = this.config;

		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				CassandraServerStarter cassandraServerStarter = new CassandraServerStarter();

				CassandraProcessConfig cassandraProcessConfig = new CassandraProcessConfig();
				cassandraProcessConfig.setConfig(config);
				cassandraProcessConfig.setTimeout(timeout);
				cassandraProcessConfig.setVersion(version);

				CassandraServerExecutable executable = cassandraServerStarter
						.prepare(cassandraProcessConfig);

				CassandraServerProcess process = executable.start();

				try {
					base.evaluate();
				}
				finally {
					process.stop();
				}

			}
		};
	}

	private void setRandomPorts(CassandraConfig config) {
		int[] freeServerPorts = getFreeServerPorts();
		config.setRpcPort(freeServerPorts[0]);
		config.setNativeTransportPort(freeServerPorts[1]);
		config.setSslStoragePort(freeServerPorts[2]);
		config.setStoragePort(freeServerPorts[3]);
		if (config.getNativeTransportPortSsl() != null) {
			config.setNativeTransportPortSsl(freeServerPorts[4]);
		}
	}

	private int[] getFreeServerPorts() {
		try {
			return Network.getFreeServerPorts(InetAddress.getLocalHost(), 5);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
