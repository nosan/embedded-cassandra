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
import java.util.Objects;

import com.github.nosan.embedded.cassandra.CassandraExecutable;
import com.github.nosan.embedded.cassandra.CassandraStarter;
import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.config.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Abstract Class for running an embedded cassandra.
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 */
public abstract class AbstractCassandraTests {

	private CassandraExecutable executable;

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	public AbstractCassandraTests(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig) {
		this.runtimeConfig = Objects.requireNonNull(runtimeConfig,
				"RuntimeConfig must not be null");
		this.executableConfig = Objects.requireNonNull(executableConfig,
				"Cassandra Config must not be null");
	}

	public AbstractCassandraTests(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	public AbstractCassandraTests(ExecutableConfig executableConfig) {
		this(new RuntimeConfigBuilder().build(), executableConfig);
	}

	public AbstractCassandraTests() {
		this(new RuntimeConfigBuilder().build(),
				new ExecutableConfigBuilder().useRandomPorts(true).build());
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
	public ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	/**
	 * Startup an embedded cassandra.
	 * @throws IOException Process could not be started.
	 */
	@BeforeClass
	public void startCassandra() throws IOException {
		IRuntimeConfig runtimeConfig = getRuntimeConfig();
		ExecutableConfig executableConfig = getExecutableConfig();
		CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig);
		this.executable = cassandraStarter.prepare(executableConfig);
		this.executable.start();

	}

	/**
	 * Stop an embedded cassandra.
	 */
	@AfterClass
	public void stopCassandra() {
		if (this.executable != null) {
			this.executable.stop();
		}
	}

}
