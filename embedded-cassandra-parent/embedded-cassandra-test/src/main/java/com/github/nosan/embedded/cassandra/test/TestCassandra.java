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

package com.github.nosan.embedded.cassandra.test;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.util.CqlScriptUtils;

/**
 * Test {@link Cassandra } that allows the Cassandra to be {@link #start() started} and
 * {@link #stop() stopped}.
 * <p>
 * In addition to the basic functionality includes {@link #getCluster()} and {@link #getSession()} methods.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class TestCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(TestCassandra.class);

	@Nonnull
	private final CqlScript[] scripts;

	@Nonnull
	private final Cassandra cassandra;

	@Nonnull
	private final ClusterFactory clusterFactory;

	@Nullable
	private volatile Cluster cluster;

	private volatile boolean initialized;


	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CqlScript... scripts) {
		this(null, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nonnull ClusterFactory clusterFactory,
			@Nonnull CqlScript... scripts) {
		this(null, clusterFactory, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory, @Nullable CqlScript... scripts) {
		this(cassandraFactory, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory,
			@Nullable ClusterFactory clusterFactory, @Nullable CqlScript... scripts) {
		this.cassandra = (cassandraFactory != null) ? cassandraFactory.create() : new LocalCassandraFactory().create();
		this.scripts = (scripts != null) ? scripts : new CqlScript[0];
		this.clusterFactory = (clusterFactory != null) ? clusterFactory : new DefaultClusterFactory();
	}


	@Override
	public void start() throws CassandraException {
		if (!this.initialized) {
			synchronized (this) {
				try {
					try {
						if (!this.initialized) {
							this.cassandra.start();
							CqlScript[] scripts = this.scripts;
							if (scripts.length > 0) {
								try (Session session = getSession()) {
									CqlScriptUtils.executeScripts(session, scripts);
								}
							}
						}
					}
					finally {
						this.initialized = true;
					}
				}
				catch (Throwable ex) {
					try {
						stop();
					}
					catch (Throwable suppress) {
						ex.addSuppressed(suppress);
					}
					if (ex instanceof CassandraException) {
						throw ex;
					}
					throw new CassandraException("Unable to Start Cassandra", ex);
				}
			}
		}

	}

	@Override
	public void stop() throws CassandraException {
		if (this.initialized) {
			synchronized (this) {
				if (this.initialized) {
					try {
						Cluster cluster = this.cluster;
						if (cluster != null) {
							try {
								cluster.close();
							}
							catch (Throwable ex) {
								log.error(ex.getMessage(), ex);
							}
						}
						this.cassandra.stop();
					}
					finally {
						this.cluster = null;
						this.initialized = false;
					}
				}
			}
		}
	}


	@Nonnull
	@Override
	public Settings getSettings() throws CassandraException {
		return this.cassandra.getSettings();
	}


	/**
	 * Lazy initialize {@link Cluster}.
	 *
	 * @return a cluster
	 */
	@Nonnull
	public Cluster getCluster() {
		if (this.cluster == null) {
			synchronized (this) {
				if (this.cluster == null) {
					Settings settings = getSettings();
					this.cluster = this.clusterFactory.create(settings);
				}
			}
		}
		return Objects.requireNonNull(this.cluster, "Cluster is not initialized");
	}

	/**
	 * Lazy initialize {@link Session} using {@link #getCluster() Cluster}.
	 *
	 * @return a session
	 */
	@Nonnull
	public Session getSession() {
		return getCluster().newSession();
	}
}
