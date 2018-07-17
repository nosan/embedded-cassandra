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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.process.CassandraExecutable;
import com.github.nosan.embedded.cassandra.process.CassandraStarter;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * Simple class for running an Embedded Cassandra. <pre>public class CassandraTests {
 *        &#64;Test
 * 	public void test() throws IOException {
 * 		Cassandra cassandra = new Cassandra();
 * 		try {
 * 			cassandra.start();
 * 			// test me
 *        }
 * 		finally {
 * 			cassandra.stop();
 *        }
 *    }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 */
public class Cassandra {

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

	private CassandraExecutable executable;

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	private boolean initialized = false;

	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this.runtimeConfig = (runtimeConfig != null ? runtimeConfig : new RuntimeConfigBuilder(log).build());
		this.executableConfig = (executableConfig != null ? executableConfig : new ExecutableConfigBuilder().build());
	}

	public Cassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, null);
	}

	public Cassandra(ExecutableConfig executableConfig) {
		this(null, executableConfig);
	}

	public Cassandra() {
		this(null, null);
	}

	/**
	 * Retrieves {@link ExecutableConfig Executable Config}.
	 *
	 * @return executable config.
	 */
	public ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	/**
	 * Retrieves {@link IRuntimeConfig Runtime Config}.
	 *
	 * @return runtime config.
	 */
	public IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
	}

	/**
	 * Start the Cassandra Server.
	 *
	 * @throws IOException Cassandra's process has not been started correctly.
	 */
	public void start() throws IOException {
		if (this.initialized) {
			throw new IOException("Cassandra has already been started");
		}
		ExecutableConfig executableConfig = getExecutableConfig();
		IRuntimeConfig runtimeConfig = getRuntimeConfig();
		CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig);
		this.executable = cassandraStarter.prepare(executableConfig);
		this.executable.start();
		this.initialized = true;
	}

	/**
	 * Stop the Cassandra Server.
	 *
	 * @see CassandraExecutable#stop
	 */
	public void stop() {
		if (this.executable != null) {
			this.executable.stop();
			this.initialized = false;
		}
	}

}
