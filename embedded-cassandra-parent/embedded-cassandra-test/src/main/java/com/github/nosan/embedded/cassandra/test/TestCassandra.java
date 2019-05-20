/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.CassandraInterruptedException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;

/**
 * Test {@link Cassandra} that allows the Cassandra to be {@link #start() started} and {@link #stop() stopped}. {@link
 * TestCassandra} does not launch {@link Cassandra} itself, it simply delegates calls to the underlying {@link
 * Cassandra}. It is also possible to get a {@code connection} to the underlying {@link Cassandra}. A connection will be
 * created by {@link ConnectionFactory} and will be managed by this {@code TestCassandra}.
 *
 * @author Dmytro Nosan
 * @see CassandraFactory
 * @see ConnectionFactory
 * @see CqlScript
 * @since 1.0.0
 */
public class TestCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(TestCassandra.class);

	private final Cassandra cassandra;

	private final ConnectionFactory connectionFactory;

	private final List<CqlScript> scripts;

	private volatile boolean started = false;

	@Nullable
	private volatile Connection connection;

	/**
	 * Creates a {@link TestCassandra}.
	 */
	public TestCassandra() {
		this(null, null, new CqlScript[0]);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(CqlScript... scripts) {
		this(null, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory, CqlScript... scripts) {
		this(cassandraFactory, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param scripts CQL scripts to execute
	 * @since 2.0.2
	 */
	public TestCassandra(@Nullable ConnectionFactory connectionFactory, CqlScript... scripts) {
		this(null, connectionFactory, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 * @since 2.0.2
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory,
			@Nullable ConnectionFactory connectionFactory, CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.scripts = Collections.unmodifiableList(Arrays.asList(scripts));
		Cassandra cassandra = ((cassandraFactory != null) ? cassandraFactory : new LocalCassandraFactory()).create();
		this.cassandra = Objects.requireNonNull(cassandra, "Cassandra must not be null");
		this.connectionFactory = (connectionFactory != null) ? connectionFactory : new DefaultConnectionFactory();
	}

	/**
	 * Starts the underlying {@link Cassandra}. Calling this method on an already started {@code Cassandra} has no
	 * effect. Causes the current thread to wait, until the {@code Cassandra} has started.
	 *
	 * @throws CassandraException if the underlying {@code Cassandra} cannot be started
	 * @throws CassandraInterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another
	 * thread
	 */
	@Override
	public void start() throws CassandraException {
		synchronized (this) {
			if (this.started) {
				return;
			}
			try {
				doStart();
				this.started = true;
			}
			catch (CassandraException ex) {
				doStopSafely();
				throw ex;
			}
			catch (Throwable ex) {
				doStopSafely();
				throw new CassandraException(String.format("Unable to start %s", toString()), ex);
			}
		}
	}

	/**
	 * Stops the underlying {@link Cassandra}. Calling this method on an already stopped
	 * {@code Cassandra} has no effect. Causes the current thread to wait, until the {@code Cassandra} has stopped.
	 *
	 * @throws CassandraException if the underlying {@code Cassandra} cannot be stopped
	 * @throws CassandraInterruptedException if the current thread is {@link Thread#interrupt() interrupted}
	 * by another thread
	 */
	@Override
	public void stop() throws CassandraException {
		synchronized (this) {
			if (!this.started) {
				return;
			}
			try {
				doStop();
				this.started = false;
			}
			catch (CassandraException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new CassandraException(String.format("Unable to stop %s", toString()), ex);
			}
		}

	}

	/**
	 * Returns the settings of the underlying {@code Cassandra}.
	 *
	 * @return the settings
	 * @throws IllegalStateException If the underlying {@link Cassandra} is not running
	 */
	@Override
	public Settings getSettings() throws IllegalStateException {
		synchronized (this) {
			return this.cassandra.getSettings();
		}
	}

	/**
	 * Returns the {@link Version version} of this {@code Cassandra}.
	 *
	 * @return a version
	 * @since 2.0.0
	 */
	@Override
	public Version getVersion() {
		return this.cassandra.getVersion();
	}

	/**
	 * Returns the {@link State state} of the underlying {@code Cassandra}.
	 *
	 * @return a state
	 * @since 1.4.1
	 */
	@Override
	public State getState() {
		return this.cassandra.getState();
	}

	/**
	 * Returns the {@link Connection connection} to the underlying {@code Cassandra}. This connection is
	 * managed by {@code TestCassandra} and will be closed when {@link #stop()} is called.
	 * There is only one connection per {@code TestCassandra}.
	 *
	 * @return a connection
	 * @since 2.0.2
	 */
	public Connection getConnection() {
		Connection connection = this.connection;
		if (connection == null) {
			synchronized (this) {
				connection = this.connection;
				if (connection == null) {
					connection = this.connectionFactory.create(getSettings());
					this.connection = Objects.requireNonNull(connection, "Connection must not be null");
				}
			}
		}
		return connection;
	}

	/**
	 * Executes the given {@link CqlScript scripts} using the {@link #getConnection() connection}.
	 *
	 * @param scripts the scripts
	 * @see #getConnection()
	 * @since 2.0.0
	 */
	public void executeScripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		getConnection().executeScripts(scripts);
	}

	@Override
	public String toString() {
		return String.format("%s %s", getClass().getSimpleName(), getVersion());
	}

	private void doStart() {
		if (log.isDebugEnabled()) {
			log.debug("Start {}", toString());
		}
		this.cassandra.start();
		if (!this.scripts.isEmpty()) {
			executeScripts(this.scripts.toArray(new CqlScript[0]));
		}
		if (log.isDebugEnabled()) {
			log.debug("{} is started", toString());
		}
	}

	private void doStop() {
		if (log.isDebugEnabled()) {
			log.debug("Stop {}", toString());
		}
		Connection connection = this.connection;
		if (connection != null) {
			try {
				connection.close();
			}
			catch (Throwable ex) {
				log.error(String.format("Can not close a connection '%s'", connection), ex);
			}
			this.connection = null;
		}
		this.cassandra.stop();
		if (log.isDebugEnabled()) {
			log.debug("{} is stopped", toString());
		}
	}

	private void doStopSafely() {
		try {
			doStop();
		}
		catch (CassandraInterruptedException ex) {
			Thread.currentThread().interrupt();
			log.error(String.format("%s can be still alive. Shutdown has been interrupted", toString()), ex);
		}
		catch (Throwable ex) {
			log.error(String.format("Unable to stop %s", toString()), ex);
		}

	}

}
