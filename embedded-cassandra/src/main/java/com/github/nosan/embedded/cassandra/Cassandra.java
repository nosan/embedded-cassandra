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
import java.io.UncheckedIOException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
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
 * 			CqlScriptUtils.executeScripts(cassandra.getSession(), new ClassPathCqlScript("init.cql"));
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
 * @see ClusterFactory
 * @see CqlScriptUtils
 */
public class Cassandra {

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

	private final ClusterFactory clusterFactory;

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	private volatile CassandraExecutable executable;

	private volatile Cluster cluster;

	private volatile Session session;


	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		this.runtimeConfig = (runtimeConfig != null ? runtimeConfig : new RuntimeConfigBuilder(log).build());
		this.executableConfig = (executableConfig != null ? executableConfig : new ExecutableConfigBuilder().build());
		this.clusterFactory = (clusterFactory != null ? clusterFactory : new DefaultClusterFactory());
	}


	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this(runtimeConfig, executableConfig, null);
	}

	public Cassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, null, null);
	}

	public Cassandra(ExecutableConfig executableConfig) {
		this(null, executableConfig, null);
	}

	public Cassandra(IRuntimeConfig runtimeConfig, ClusterFactory clusterFactory) {
		this(runtimeConfig, null, clusterFactory);
	}

	public Cassandra(ExecutableConfig executableConfig, ClusterFactory clusterFactory) {
		this(null, executableConfig, clusterFactory);
	}

	public Cassandra(ClusterFactory clusterFactory) {
		this(null, null, clusterFactory);
	}

	public Cassandra() {
		this(null, null, null);
	}

	/**
	 * Retrieves {@link ExecutableConfig Executable Config}.
	 *
	 * @return Executable Config.
	 */
	public ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	/**
	 * Retrieves {@link IRuntimeConfig Runtime Config}.
	 *
	 * @return Runtime Config.
	 */
	public IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
	}

	/**
	 * Retrieves {@link ClusterFactory Cluster Factory}.
	 *
	 * @return Cluster Factory to use.
	 */
	public ClusterFactory getClusterFactory() {
		return this.clusterFactory;
	}

	/**
	 * Lazy initialize Cassandra's {@link Cluster Cluster} using {@link ClusterFactory ClusterFactory}.
	 * Note! This method should be called only after {@link #start()} method.
	 *
	 * @return Cassandra's Cluster.
	 * @see ClusterFactory
	 */
	public Cluster getCluster() {
		if (this.cluster == null) {
			synchronized (this) {
				if (this.cluster == null) {
					ExecutableConfig executableConfig = getExecutableConfig();
					this.cluster = getClusterFactory()
							.getCluster(executableConfig.getConfig(), executableConfig.getVersion());
				}
			}
		}
		return this.cluster;
	}

	/**
	 * Lazy initialize Cassandra's {@link Session Session} using {@link #getCluster() Cluster}.
	 * Note! This method should be called only after {@link #start()} method.
	 *
	 * @return Cassandra's Session
	 * @see #getCluster()
	 */
	public Session getSession() {
		if (this.session == null) {
			synchronized (this) {
				if (this.session == null) {
					this.session = getCluster().connect();
				}
			}
		}
		return this.session;
	}


	/**
	 * Starts the Cassandra. {@code Session} and {@code Cluster} will not be initialized.
	 * This method registers a shutdown hook if {@link IRuntimeConfig#isDaemonProcess()} was set as a {@code true}.
	 *
	 * @throws UncheckedIOException Cassandra's process has not been started correctly.
	 * @throws IllegalStateException Cassandra has already been initialized.
	 * @see CassandraStarter
	 */
	public void start() {
		if (this.executable == null) {
			synchronized (this) {
				if (this.executable == null) {
					IRuntimeConfig runtimeConfig = getRuntimeConfig();
					ExecutableConfig executableConfig = getExecutableConfig();
					if (runtimeConfig.isDaemonProcess()) {
						Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
					}
					try {
						CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig);
						this.executable = cassandraStarter.prepare(executableConfig);
						this.executable.start();
					}
					catch (IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}
				else {
					throw new IllegalStateException("Cassandra has already been initialized");
				}
			}
		}
		else {
			throw new IllegalStateException("Cassandra has already been initialized");
		}
	}

	/**
	 * Stops the Cassandra. This method stops not only Cassandra  but calls a close method against {@code
	 * Cluster},
	 * {@code Session}.
	 */
	public void stop() {
		synchronized (this) {
			if (this.session != null) {
				close(() -> this.session.close());
				this.session = null;
			}
			if (this.cluster != null) {
				close(() -> this.cluster.close());
				this.cluster = null;
			}
			if (this.executable != null) {
				close(() -> this.executable.stop());
				this.executable = null;
			}
		}
	}

	private static void close(Runnable runnable) {
		try {
			runnable.run();
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
