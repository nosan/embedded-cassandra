/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.jupiter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * JUnit {@link RegisterExtension Extension} that allows the Cassandra to be {@link Cassandra#start() started} and
 * {@link Cassandra#stop() stopped}.
 * <p>
 * The typical usage is:
 * <pre>
 * public class CassandraExtensionTests {
 * &#64;RegisterExtension
 * public static CassandraExtension cassandra = new CassandraExtension(cassandraFactory, clusterFactory, scripts[]);
 * &#64;Test
 * public void test() {
 *  //
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
public class CassandraExtension extends TestCassandra implements BeforeAllCallback, AfterAllCallback {


	/**
	 * Creates a {@link CassandraExtension}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public CassandraExtension(@Nullable CqlScript... scripts) {
		super(scripts);
	}

	/**
	 * Creates a {@link CassandraExtension}.
	 *
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraExtension(@Nonnull ClusterFactory clusterFactory, @Nonnull CqlScript... scripts) {
		super(clusterFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraExtension}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraExtension(@Nullable CassandraFactory cassandraFactory, @Nullable CqlScript... scripts) {
		super(cassandraFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraExtension}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraExtension(@Nullable CassandraFactory cassandraFactory, @Nullable ClusterFactory clusterFactory,
			@Nullable CqlScript... scripts) {
		super(cassandraFactory, clusterFactory, scripts);
	}

	@Override
	public void beforeAll(@Nonnull ExtensionContext context) {
		start();
	}

	@Override
	public void afterAll(@Nonnull ExtensionContext context) {
		stop();
	}

}
