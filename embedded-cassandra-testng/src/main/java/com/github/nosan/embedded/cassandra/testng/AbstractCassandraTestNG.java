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
import com.github.nosan.embedded.cassandra.CassandraConfig;
import com.github.nosan.embedded.cassandra.support.CassandraConfigBuilder;
import com.github.nosan.embedded.cassandra.support.RuntimeConfigBuilder;

/**
 * Abstract Class for running an Embedded Cassandra. This class provides two new methods
 * {@link #startCassandra()} and {@link #stopCassandra()}. Cassandra will be started on
 * the random ports. <pre>
 * public class CassandraTests extends AbstractCassandraTestNG {
 * 	&#64;BeforeMethod
 * 	public void setUp() throws Exception {
 * 		// before actions
 *    }
 * 	&#64;AfterMethod
 * 	public void tearDown() throws Exception {
 * 		// after actions
 *    }
 * 	&#64;Test
 * 	public void test() {
 * 	// test me...
 *    }
 * }
 * }</pre>
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see CassandraConfigBuilder
 * @see Cassandra
 */
public abstract class AbstractCassandraTestNG extends Cassandra {

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig,
			CassandraConfig cassandraConfig) {
		super(runtimeConfig, cassandraConfig);
	}

	public AbstractCassandraTestNG(IRuntimeConfig runtimeConfig) {
		super(runtimeConfig, new CassandraConfigBuilder().useRandomPorts(true).build());
	}

	public AbstractCassandraTestNG(CassandraConfig cassandraConfig) {
		super(cassandraConfig);
	}

	public AbstractCassandraTestNG() {
		super(new CassandraConfigBuilder().useRandomPorts(true).build());
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
