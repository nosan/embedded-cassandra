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

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.config.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Abstract Class for running an Embedded Cassandra. This class provides two new methods
 * {@link #startCassandra()} and {@link #stopCassandra()}. Cassandra will be started on
 * the random ports. <pre>
 * public class CassandraTests extends AbstractCassandraTestNG {
 * 	&#64;BeforeMethod
 * 	public void setUp() throws Exception {
 * 		CqlScriptUtils.executeScripts(getSession(), "init.cql");
 *    }
 * 	&#64;AfterMethod
 * 	public void tearDown() throws Exception {
 * 		CqlScriptUtils.executeScripts(getSession(), "drop.cql");
 *    }
 * 	&#64;Test
 * 	public void test() {
 * 	// test me...
 *    }
 * }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see com.github.nosan.embedded.cassandra.config.RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 * @see Cassandra
 * @see CqlScriptUtils
 */
public abstract class AbstractCassandraTestNG extends Cassandra {

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig,
			ExecutableConfig executableConfig) {
		super(runtimeConfig, executableConfig);
	}

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig) {
		super(runtimeConfig, new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	public AbstractCassandraTestNG(ExecutableConfig executableConfig) {
		super(executableConfig);
	}

	public AbstractCassandraTestNG() {
		this(new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	/**
	 * Startup an Embedded Cassandra.
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
