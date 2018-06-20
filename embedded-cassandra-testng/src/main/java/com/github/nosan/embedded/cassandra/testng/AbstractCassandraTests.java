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

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.CassandraPortUtils;
import com.github.nosan.embedded.cassandra.CassandraServerExecutable;
import com.github.nosan.embedded.cassandra.CassandraServerStarter;
import com.github.nosan.embedded.cassandra.CassandraVersion;
import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import com.github.nosan.embedded.cassandra.config.CassandraRuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Abstract Class for running an embedded cassandra.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractCassandraTests {

	private CassandraServerExecutable executable;

	private final IRuntimeConfig runtimeConfig;

	private final CassandraVersion version;

	private final CassandraConfig config;

	private final Duration timeout;

	private final boolean useRandomPorts;

	public AbstractCassandraTests(CassandraVersion version, CassandraConfig config,
			Duration timeout, boolean useRandomPorts, IRuntimeConfig runtimeConfig) {
		this.version = Objects.requireNonNull(version, "Version must not be null");
		this.config = Objects.requireNonNull(config, "Config must not be null");
		this.timeout = Objects.requireNonNull(timeout, "Timeout must not be null");
		this.runtimeConfig = Objects.requireNonNull(runtimeConfig,
				"Runtime Config must " + "not be null");
		this.useRandomPorts = useRandomPorts;
	}

	public AbstractCassandraTests(CassandraConfig config, Duration timeout,
			boolean useRandomPorts, IRuntimeConfig runtimeConfig) {
		this(CassandraVersion.LATEST, config, timeout, useRandomPorts, runtimeConfig);
	}

	public AbstractCassandraTests(CassandraConfig config, Duration timeout,
			boolean useRandomPorts) {
		this(config, timeout, useRandomPorts,
				new CassandraRuntimeConfigBuilder().defaults().build());
	}

	public AbstractCassandraTests(CassandraConfig config, Duration timeout,
			IRuntimeConfig runtimeConfig) {
		this(config, timeout, true, runtimeConfig);
	}

	public AbstractCassandraTests(CassandraConfig config, boolean useRandomPorts) {
		this(config, Duration.ofMinutes(1), useRandomPorts);
	}

	public AbstractCassandraTests(CassandraConfig config, boolean useRandomPorts,
			IRuntimeConfig runtimeConfig) {
		this(config, Duration.ofMinutes(1), useRandomPorts, runtimeConfig);
	}

	public AbstractCassandraTests(CassandraConfig config) {
		this(config, Duration.ofMinutes(1), true);
	}

	public AbstractCassandraTests(CassandraConfig config, IRuntimeConfig runtimeConfig) {
		this(config, Duration.ofMinutes(1), true, runtimeConfig);
	}

	public AbstractCassandraTests(Duration timeout, boolean useRandomPorts) {
		this(new CassandraConfig(), timeout, useRandomPorts);

	}

	public AbstractCassandraTests(Duration timeout, boolean useRandomPorts,
			IRuntimeConfig runtimeConfig) {
		this(new CassandraConfig(), timeout, useRandomPorts, runtimeConfig);

	}

	public AbstractCassandraTests(boolean useRandomPorts) {
		this(new CassandraConfig(), useRandomPorts);
	}

	public AbstractCassandraTests(boolean useRandomPorts, IRuntimeConfig runtimeConfig) {
		this(new CassandraConfig(), useRandomPorts, runtimeConfig);
	}

	public AbstractCassandraTests(Duration timeout) {
		this(new CassandraConfig(), timeout, true);
	}

	public AbstractCassandraTests(Duration timeout, IRuntimeConfig runtimeConfig) {
		this(new CassandraConfig(), timeout, true, runtimeConfig);
	}

	public AbstractCassandraTests(IRuntimeConfig runtimeConfig) {
		this(new CassandraConfig(), Duration.ofMinutes(1), true, runtimeConfig);
	}

	public AbstractCassandraTests(CassandraConfig config, Duration timeout) {
		this(config, timeout, true);
	}

	public AbstractCassandraTests() {
		this(new CassandraConfig());
	}

	/**
	 * Whether or not use random ports.
	 * @return random ports was specified or not.
	 */
	public boolean isUseRandomPorts() {
		return this.useRandomPorts;
	}

	/**
	 * Retrieves an embedded cassandra runtime config.
	 * @return the runtime config.
	 */
	public IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
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

	@BeforeClass
	public void startCassandra() throws IOException {

		Duration timeout = getTimeout();
		CassandraVersion version = getVersion();
		CassandraConfig config = getConfig();
		IRuntimeConfig runtimeConfig = getRuntimeConfig();

		if (isUseRandomPorts()) {
			CassandraPortUtils.setRandomPorts(config);
		}

		CassandraServerStarter cassandraServerStarter = new CassandraServerStarter(
				runtimeConfig);

		CassandraProcessConfig cassandraProcessConfig = new CassandraProcessConfig();
		cassandraProcessConfig.setConfig(config);
		cassandraProcessConfig.setTimeout(timeout);
		cassandraProcessConfig.setVersion(version);

		this.executable = cassandraServerStarter.prepare(cassandraProcessConfig);
		this.executable.start();

	}

	@AfterClass
	public void stopCassandra() {
		if (this.executable != null) {
			this.executable.stop();
		}
	}

}
