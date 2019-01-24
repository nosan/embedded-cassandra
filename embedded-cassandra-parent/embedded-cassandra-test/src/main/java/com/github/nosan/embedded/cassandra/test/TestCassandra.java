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

package com.github.nosan.embedded.cassandra.test;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import org.apiguardian.api.API;
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
 * {@link #stop() stopped}. {@link TestCassandra} does not launch {@link Cassandra} itself, it simply delegates calls to
 * the underlying {@link Cassandra}.
 * <p>
 * In addition to the basic functionality includes utility methods to test {@code Cassandra} code.
 *
 * @author Dmytro Nosan
 * @see TestCassandraBuilder
 * @see CassandraFactory
 * @see CqlScriptUtils
 * @see CqlUtils
 * @see CqlScript
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public class TestCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(TestCassandra.class);

	private final boolean registerShutdownHook;

	@Nonnull
	private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

	@Nonnull
	private final Object lock = new Object();

	@Nonnull
	private final CqlScript[] scripts;

	@Nonnull
	private final ClusterFactory clusterFactory;

	@Nonnull
	private final CassandraFactory cassandraFactory;

	@Nullable
	private volatile Cassandra cassandra;

	@Nullable
	private volatile Cluster cluster;

	@Nullable
	private volatile Session session;

	private volatile boolean started;

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CqlScript... scripts) {
		this(true, null, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public TestCassandra(boolean registerShutdownHook, @Nullable CqlScript... scripts) {
		this(registerShutdownHook, null, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nonnull ClusterFactory clusterFactory, @Nonnull CqlScript... scripts) {
		this(true, null, clusterFactory, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory, @Nullable CqlScript... scripts) {
		this(true, cassandraFactory, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public TestCassandra(boolean registerShutdownHook, @Nonnull ClusterFactory clusterFactory,
			@Nonnull CqlScript... scripts) {
		this(registerShutdownHook, null, clusterFactory, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public TestCassandra(boolean registerShutdownHook, @Nullable CassandraFactory cassandraFactory,
			@Nullable CqlScript... scripts) {
		this(registerShutdownHook, cassandraFactory, null, scripts);
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
		this(true, cassandraFactory, clusterFactory, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param clusterFactory factory to create a {@link Cluster}
	 * @param scripts CQL scripts to execute
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	public TestCassandra(boolean registerShutdownHook, @Nullable CassandraFactory cassandraFactory,
			@Nullable ClusterFactory clusterFactory, @Nullable CqlScript... scripts) {
		this.cassandraFactory = (cassandraFactory != null) ? cassandraFactory : new LocalCassandraFactory();
		this.scripts = (scripts != null) ? scripts : new CqlScript[0];
		this.clusterFactory = (clusterFactory != null) ? clusterFactory : new DefaultClusterFactory();
		this.registerShutdownHook = registerShutdownHook;
	}

	@Override
	public void start() throws CassandraException {
		synchronized (this.lock) {
			if (this.started) {
				return;
			}
			if (this.registerShutdownHook && this.shutdownHookRegistered.compareAndSet(false, true)) {
				try {
					Runtime.getRuntime().addShutdownHook(new Thread(this::stopSilently, "Test Cassandra Hook"));
				}
				catch (Throwable ex) {
					throw new CassandraException("Test Cassandra shutdown hook is not registered", ex);
				}
			}
			try {
				start0();
			}
			catch (InterruptedException ex) {
				if (log.isDebugEnabled()) {
					log.debug("Test Cassandra launch was interrupted");
				}
				stopSilently();
				Thread.currentThread().interrupt();
			}
			catch (Throwable ex) {
				stopSilently();
				throw new CassandraException("Unable to start Test Cassandra", ex);
			}
		}
	}

	@Override
	public void stop() throws CassandraException {
		synchronized (this.lock) {
			if (!this.started) {
				return;
			}
			try {
				stop0();
			}
			catch (Throwable ex) {
				throw new CassandraException("Unable to stop Test Cassandra", ex);
			}

		}
	}

	@Nonnull
	@Override
	public Settings getSettings() throws CassandraException {
		synchronized (this.lock) {
			Cassandra cassandra = this.cassandra;
			Settings settings = (cassandra != null) ? cassandra.getSettings() : null;
			return Optional.ofNullable(settings)
					.orElseThrow(() -> new CassandraException(
							"Test Cassandra is not initialized. Please start it before calling this method."));
		}
	}

	/**
	 * Lazy initialize a {@link Cluster}.
	 *
	 * @return a cluster
	 */
	@Nonnull
	public Cluster getCluster() {
		if (this.cluster == null) {
			synchronized (this.lock) {
				if (this.cluster == null) {
					Settings settings = getSettings();
					this.cluster = this.clusterFactory.create(settings);
					if (log.isDebugEnabled()) {
						log.debug("Initialize a cluster ({})", this.cluster);
					}
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
			synchronized (this.lock) {
				if (this.session == null) {
					this.session = getCluster().connect();
					if (log.isDebugEnabled()) {
						log.debug("Initialize a session ({})", this.session);
					}
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

	/**
	 * Executes the provided statement.
	 *
	 * @param statement the CQL statement to execute
	 * @return the result of the query. That result will never be null
	 * but can be empty (and will be for any non SELECT query).
	 * @since 1.2.8
	 */
	@Nonnull
	public ResultSet executeStatement(@Nonnull Statement statement) {
		return CqlUtils.executeStatement(getSession(), statement);
	}

	@Nonnull
	@Override
	public String toString() {
		return String.format("%s [%s]", getClass().getSimpleName(), this.cassandra);
	}

	private void start0() throws InterruptedException {
		Cassandra cassandra = this.cassandraFactory.create();
		Objects.requireNonNull(cassandra, "Cassandra must not be null");
		if (log.isDebugEnabled()) {
			log.debug("Starts Test Cassandra ({})", cassandra);
		}
		this.cassandra = cassandra;
		this.started = true;
		cassandra.start();
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		CqlScript[] scripts = this.scripts;
		if (scripts.length > 0) {
			executeScripts(scripts);
		}
		if (log.isDebugEnabled()) {
			log.debug("Test Cassandra ({}) has been started", cassandra);
		}
	}

	private void stop0() {
		Cassandra cassandra = this.cassandra;
		if (log.isDebugEnabled()) {
			log.debug("Stops Test Cassandra ({})", cassandra);
		}
		try {
			Session session = this.session;
			if (session != null) {
				if (log.isDebugEnabled()) {
					log.debug("Closes a session ({})", session);
				}
				session.close();
			}
		}
		catch (Throwable ex) {
			log.error(String.format("Session (%s) has not been closed", this.session), ex);
		}
		this.session = null;
		try {
			Cluster cluster = this.cluster;
			if (cluster != null) {
				if (log.isDebugEnabled()) {
					log.debug("Closes a cluster ({})", cluster);
				}
				cluster.close();
			}
		}
		catch (Throwable ex) {
			log.error(String.format("Cluster (%s) has not been closed", this.cluster), ex);
		}
		this.cluster = null;
		if (cassandra != null) {
			cassandra.stop();
		}
		this.cassandra = null;
		this.started = false;
		if (log.isDebugEnabled()) {
			log.debug("Test Cassandra ({}) has been stopped", cassandra);
		}
	}

	private void stopSilently() {
		try {
			stop();
		}
		catch (Throwable ex) {
			if (log.isDebugEnabled()) {
				log.error("Unable to stop Test Cassandra", ex);
			}
		}
	}
}
