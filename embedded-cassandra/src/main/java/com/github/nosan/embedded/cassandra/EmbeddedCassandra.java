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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import de.flapdoodle.embed.process.config.IRuntimeConfig;

import com.github.nosan.embedded.cassandra.cql.CqlResource;
import com.github.nosan.embedded.cassandra.cql.CqlScripts;


/**
 * Simple class for running an Embedded Cassandra in the test environment.
 * <pre>public class CassandraTests {
 *        &#64;Test
 * 	public void test() throws IOException {
 * 		EmbeddedCassandra cassandra = new EmbeddedCassandra();
 * 		try {
 * 		    cassandra.start();
 * 		    cassandra.executeScripts(new ClassPathCqlResource("init.cql"));
 * 			// test me
 *      }
 * 		finally {
 * 			cassandra.stop();
 *      }
 *    }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder
 * @see com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder
 * @see ClusterFactory
 */
public class EmbeddedCassandra extends Cassandra {

	private final ClusterFactory clusterFactory;

	private Cluster cluster;

	private Session session;


	public EmbeddedCassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		super(runtimeConfig, executableConfig);
		this.clusterFactory = (clusterFactory != null ? clusterFactory : new DefaultClusterFactory());
	}


	public EmbeddedCassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this(runtimeConfig, executableConfig, null);
	}

	public EmbeddedCassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, null, null);
	}

	public EmbeddedCassandra(ExecutableConfig executableConfig) {
		this(null, executableConfig, null);
	}

	public EmbeddedCassandra(IRuntimeConfig runtimeConfig, ClusterFactory clusterFactory) {
		this(runtimeConfig, null, clusterFactory);
	}

	public EmbeddedCassandra(ExecutableConfig executableConfig, ClusterFactory clusterFactory) {
		this(null, executableConfig, clusterFactory);
	}

	public EmbeddedCassandra(ClusterFactory clusterFactory) {
		this(null, null, clusterFactory);
	}

	public EmbeddedCassandra() {
		this(null, null, null);
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
	 * Executes the provided scripts.
	 *
	 * @param cqlResources the CQL resources to execute.
	 * @see #getSession()
	 * @see CqlResource
	 * @see CqlScripts
	 */
	public void executeScripts(CqlResource... cqlResources) {
		CqlScripts.executeScripts(getSession(), cqlResources);
	}

	@Override
	public void stop() {
		if (this.session != null) {
			this.session.close();
			this.session = null;
		}
		if (this.cluster != null) {
			this.cluster.close();
			this.cluster = null;
		}
		super.stop();
	}

}
