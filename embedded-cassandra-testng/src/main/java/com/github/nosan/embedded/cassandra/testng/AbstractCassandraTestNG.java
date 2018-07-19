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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.ClusterFactory;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * Abstract Class for running an Embedded Cassandra. This class provides two new methods
 * {@link #startCassandra()} and {@link #stopCassandra()}. Cassandra will be started on
 * the random ports. <pre>
 * public class AbstractCassandraTestNGTests extends AbstractCassandraTestNG {
 * &#64;BeforeMethod
 * public void setUp() {
 * CqlScriptUtils.executeScripts(getSession(), new ClassPathCqlScript("init.cql"));
 * }
 * &#64;Test
 * public void select() {
 * assertThat(getSession().execute(...).wasApplied())
 * .isTrue();
 * }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 * @see Cassandra
 */
public abstract class AbstractCassandraTestNG extends Cassandra {
	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		super(runtimeConfig, executableConfig, clusterFactory);
	}

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig) {
		super(runtimeConfig, executableConfig);
	}

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig) {
		super(runtimeConfig);
	}

	public AbstractCassandraTestNG(ExecutableConfig executableConfig) {
		super(executableConfig);
	}

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig,
			ClusterFactory clusterFactory) {
		super(runtimeConfig, clusterFactory);
	}

	public AbstractCassandraTestNG(ExecutableConfig executableConfig,
			ClusterFactory clusterFactory) {
		super(executableConfig, clusterFactory);
	}

	public AbstractCassandraTestNG(ClusterFactory clusterFactory) {
		super(clusterFactory);
	}

	public AbstractCassandraTestNG() {
	}

	/**
	 * Startup an Embedded Cassandra.
	 *
	 * @throws IOException Process could not be started.
	 * @see #start()
	 */
	@BeforeClass(alwaysRun = true)
	public void startCassandra() throws IOException {
		start();
	}

	/**
	 * Stop an Embedded Cassandra.
	 *
	 * @see #stop()
	 */
	@AfterClass(alwaysRun = true)
	public void stopCassandra() {
		stop();
	}

}
