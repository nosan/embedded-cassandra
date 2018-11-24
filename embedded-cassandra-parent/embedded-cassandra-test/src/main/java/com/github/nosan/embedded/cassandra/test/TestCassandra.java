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
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.util.CqlScriptUtils;
import com.github.nosan.embedded.cassandra.test.util.CqlUtils;

/**
 * Test {@link Cassandra} that allows the Cassandra to be {@link #start() started} and
 * {@link #stop() stopped}.
 * <p>
 * In addition to the basic functionality includes {@link #getCluster()} and {@link #getSession()} methods.
 *
 * @author Dmytro Nosan
 * @see CassandraFactory
 * @see CqlScriptUtils
 * @see CqlUtils
 * @see CqlScript
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

	@Nullable
	private volatile Session session;

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
	public TestCassandra(@Nonnull ClusterFactory clusterFactory, @Nonnull CqlScript... scripts) {
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
		addShutdownHook();
		this.cassandra = (cassandraFactory != null) ? cassandraFactory.create() : new LocalCassandraFactory().create();
		this.scripts = (scripts != null) ? scripts : new CqlScript[0];
		this.clusterFactory = (clusterFactory != null) ? clusterFactory : new DefaultClusterFactory();
	}


	@Override
	public void start() throws CassandraException {
		if (!this.initialized) {
			synchronized (this) {
				if (!this.initialized) {
					this.initialized = true;
					try {
						this.cassandra.start();
						CqlScript[] scripts = this.scripts;
						if (scripts.length > 0) {
							executeScripts(scripts);
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

	}

	@Override
	public void stop() throws CassandraException {
		if (this.initialized) {
			synchronized (this) {
				if (this.initialized) {
					this.initialized = false;
					try {
						Cluster cluster = this.cluster;
						this.session = null;
						this.cluster = null;
						if (cluster != null) {
							cluster.close();
						}
					}
					catch (Throwable ex) {
						log.error("Cluster has not been closed", ex);
					}
					try {
						this.cassandra.stop();
					}
					catch (Throwable ex) {
						if (ex instanceof CassandraException) {
							throw ex;
						}
						throw new CassandraException("Unable to Start Cassandra", ex);
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
	 * Lazy initialize a {@link Cluster}.
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
	 * Lazy initialize a {@link Session} using a {@link #getCluster() Cluster}.
	 *
	 * @return a session
	 */
	@Nonnull
	public Session getSession() {
		if (this.session == null) {
			synchronized (this) {
				if (this.session == null) {
					this.session = getCluster().connect();
				}
			}
		}
		return Objects.requireNonNull(this.session, "Session is not initialized");
	}

	/**
	 * Delete all rows from the specified tables.
	 *
	 * @param tableNames the names of the tables to delete from
	 * @since 1.0.6
	 */
	public void deleteFromTables(@Nonnull String... tableNames) {
		CqlUtils.deleteFromTables(getSession(), tableNames);
	}

	/**
	 * Drop the specified tables.
	 *
	 * @param tableNames the names of the tables to drop
	 * @since 1.0.6
	 */
	public void dropTables(@Nonnull String... tableNames) {
		CqlUtils.dropTables(getSession(), tableNames);
	}

	/**
	 * Drop the specified keyspaces.
	 *
	 * @param keyspaceNames the names of the keyspaces to drop
	 * @since 1.0.6
	 */
	public void dropKeyspaces(@Nonnull String... keyspaceNames) {
		CqlUtils.dropKeyspaces(getSession(), keyspaceNames);
	}

	/**
	 * Count the rows in the given table.
	 *
	 * @param tableName name of the table to count rows in
	 * @return the number of rows in the table
	 * @since 1.0.6
	 */
	public long getRowCount(@Nonnull String tableName) {
		return CqlUtils.getRowCount(getSession(), tableName);
	}

	/**
	 * Executes the given scripts.
	 *
	 * @param scripts the CQL scripts to execute.
	 * @since 1.0.6
	 */
	public void executeScripts(@Nonnull CqlScript... scripts) {
		CqlScriptUtils.executeScripts(getSession(), scripts);
	}

	/**
	 * Executes the provided query using the provided values.
	 *
	 * @param statement the CQL query to execute.
	 * @param args values required for the execution of {@code query}. See {@link
	 * SimpleStatement#SimpleStatement(String, Object...)} for more details.
	 * @return the result of the query. That result will never be null but can be empty (and will be
	 * for any non SELECT query).
	 * @since 1.0.6
	 */
	@Nonnull
	public ResultSet executeStatement(@Nonnull String statement, @Nullable Object... args) {
		return CqlUtils.executeStatement(getSession(), statement, args);
	}


	private void addShutdownHook() {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Test Cassandra Shutdown Hook"));
		}
		catch (Throwable ex) {
			log.error(String.format("Shutdown hook is not registered for (%s)", getClass()), ex);
		}
	}

}
