/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.testng;

import com.datastax.driver.core.Cluster;
import org.apiguardian.api.API;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Base {@code test class} that allows the Cassandra to be {@link Cassandra#start() started} and
 * {@link Cassandra#stop() stopped}.
 * <p>
 * The typical usage is:
 * <pre>
 * public class CassandraTests extends CassandraTestNG {
 * public CassandraTests(){
 *     super(&#47;* constructor parameters *&#47;);
 * }
 * &#64;Test
 * public void test() {
 * //
 * }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see ClusterFactory
 * @see CassandraFactory
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public class CassandraTestNG extends TestCassandra {

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public CassandraTestNG(@Nullable CqlScript... scripts) {
		super(scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public CassandraTestNG(boolean registerShutdownHook, @Nullable CqlScript... scripts) {
		super(registerShutdownHook, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraTestNG(@Nullable ClusterFactory clusterFactory, @Nullable CqlScript... scripts) {
		super(clusterFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraTestNG(@Nullable CassandraFactory cassandraFactory, @Nullable CqlScript... scripts) {
		super(cassandraFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public CassandraTestNG(boolean registerShutdownHook, @Nullable ClusterFactory clusterFactory,
			@Nullable CqlScript... scripts) {
		super(registerShutdownHook, clusterFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public CassandraTestNG(boolean registerShutdownHook, @Nullable CassandraFactory cassandraFactory,
			@Nullable CqlScript... scripts) {
		super(registerShutdownHook, cassandraFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraTestNG(@Nullable CassandraFactory cassandraFactory,
			@Nullable ClusterFactory clusterFactory, @Nullable CqlScript... scripts) {
		super(cassandraFactory, clusterFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public CassandraTestNG(boolean registerShutdownHook, @Nullable CassandraFactory cassandraFactory,
			@Nullable ClusterFactory clusterFactory, @Nullable CqlScript... scripts) {
		super(registerShutdownHook, cassandraFactory, clusterFactory, scripts);
	}

	@BeforeClass(alwaysRun = true)
	@Override
	public void start() throws CassandraException {
		super.start();
	}

	@AfterClass(alwaysRun = true)
	@Override
	public void stop() throws CassandraException {
		super.stop();
	}

}
