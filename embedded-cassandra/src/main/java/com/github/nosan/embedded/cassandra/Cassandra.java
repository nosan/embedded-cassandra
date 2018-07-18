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

	private CassandraExecutable executable;

	private Cluster cluster;

	private Session session;

	private boolean initialized = false;


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
	 * Retrieves Cassandra's {@link Cluster Cluster} using {@link ClusterFactory ClusterFactory}.
	 *
	 * @return Cassandra's Cluster.
	 * @see ClusterFactory
	 */
	public Cluster getCluster() {
		if (this.cluster == null) {
			ExecutableConfig executableConfig = getExecutableConfig();
			this.cluster = this.clusterFactory.getCluster(executableConfig.getConfig(), executableConfig.getVersion());
		}
		return this.cluster;
	}

	/**
	 * Retrieves Cassandra's {@link Session Session} using {@link #getCluster()}.
	 *
	 * @return Cassandra's Session.
	 * @see #getCluster()
	 */

	public Session getSession() {
		if (this.session == null) {
			this.session = getCluster().connect();
		}
		return this.session;
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
		if (this.cluster != null) {
			this.cluster.closeAsync();
			this.cluster = null;
			this.session = null;
		}
		if (this.executable != null) {
			this.executable.stop();
		}
		this.initialized = false;
	}

}
