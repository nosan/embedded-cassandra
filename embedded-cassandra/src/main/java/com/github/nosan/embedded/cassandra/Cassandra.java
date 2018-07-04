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
import java.util.Objects;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.config.Config;
import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.config.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.config.RuntimeConfigBuilder;

/**
 * Simple class for starting an Embedded Cassandra using {@link IRuntimeConfig} and
 * {@link ExecutableConfig}. In general this class provides much easier way to start
 * Cassandra than {@link CassandraStarter}. Moreover it provides helper methods
 * {@link #getCluster()} and {@link #getSession()} for working with it.
 * <pre>public class CassandraTests {
 *
 *
 *        &#64;Test
 * 	public void test() throws IOException {
 * 		EmbeddedCassandra embeddedCassandra = new EmbeddedCassandra();
 * 		try {
 * 			embeddedCassandra.start();
 * 			Session session = embeddedCassandra.getSession();
 * 			// test me
 * 		}
 * 		finally {
 * 			embeddedCassandra.stop();
 * 		}
 * 	}
 *
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 * @see CassandraStarter
 */
public class Cassandra {

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

	private CassandraExecutable executable;

	private Cluster cluster;

	private Session session;

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this.runtimeConfig = Objects.requireNonNull(runtimeConfig,
				"RuntimeConfig must not be null");
		this.executableConfig = Objects.requireNonNull(executableConfig,
				"Cassandra Config must not be null");
	}

	public Cassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, new ExecutableConfigBuilder().build());
	}

	public Cassandra(ExecutableConfig executableConfig) {
		this(new RuntimeConfigBuilder(log).build(), executableConfig);
	}

	public Cassandra() {
		this(new RuntimeConfigBuilder(log).build(),
				new ExecutableConfigBuilder().build());
	}

	/**
	 * Retrieves an Embedded Cassandra Runtime config.
	 * @return Cassandra {@link IRuntimeConfig}.
	 */
	public IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
	}

	/**
	 * Retrieves an Embedded Cassandra config.
	 * @return Cassandra {@link ExecutableConfig}.
	 */
	public ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	/**
	 * Retrieves {@link Session} based on the {@link #getCluster() cluster }}.
	 * @return Cassandra {@link Session}.
	 * @see Session
	 */
	public Session getSession() {
		return this.session;
	}

	/**
	 * Retrieves {@link Cluster} based on the {@link #getExecutableConfig() config}.
	 * @return Cassandra {@link Cluster}.
	 * @see Cluster
	 */
	public Cluster getCluster() {
		return this.cluster;
	}

	/**
	 * Start the Embedded Cassandra Server using {@link #getExecutableConfig() Executable
	 * Config} and {@link #getRuntimeConfig() Runtime Config}.
	 * @throws IOException Cassandra's process has not been started correctly.
	 */
	public void start() throws IOException {
		ExecutableConfig executableConfig = getExecutableConfig();
		IRuntimeConfig runtimeConfig = getRuntimeConfig();

		CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig);

		this.executable = cassandraStarter.prepare(executableConfig);
		this.executable.start();
		this.cluster = getCluster(executableConfig);
		this.session = getSession(getCluster());
	}

	/**
	 * Stop the Embedded Cassandra.
	 * @see CassandraExecutable#stop
	 * @see Cluster#close()
	 * @see Session#close()
	 */
	public void stop() {
		if (this.session != null) {
			close(this.session::close);
		}
		if (this.cluster != null) {
			close(this.cluster::close);
		}
		if (this.executable != null) {
			close(this.executable::stop);
		}
	}

	/**
	 * Creating a new {@link Cluster} with default configurations based on the config.
	 * @param executableConfig Executable Config.
	 * @return {@link Cluster} cluster.
	 */
	protected Cluster getCluster(ExecutableConfig executableConfig) {
		Config config = executableConfig.getConfig();
		return Cluster.builder().withPort(config.getNativeTransportPort())
				.addContactPoint(
						ObjectUtils.defaultIfNull(config.getListenAddress(), "localhost"))
				.withoutJMXReporting().build();
	}

	/**
	 * Creating a {@link Session} using provided {@link Cluster}.
	 * @param cluster Cassandra's Cluster.
	 * @return {@link Session} session.
	 * @see Cluster#connect()
	 */
	protected Session getSession(Cluster cluster) {
		return cluster.connect();
	}

	private void close(Runnable action) {
		try {
			action.run();
		}
		catch (Throwable ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
