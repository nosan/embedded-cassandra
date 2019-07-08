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

import java.util.ArrayList;
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
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Test {@link Cassandra} that allows the Cassandra to be {@link #start() started} and {@link #stop() stopped}. {@link
 * TestCassandra} does not launch {@link Cassandra} itself, it simply delegates calls to the underlying {@link
 * Cassandra}. It is also possible to get a {@code connection} to the underlying {@link Cassandra}. Connection will be
 * managed by this {@code TestCassandra} and closed when {@link #stop()} is called. The typical usage is:
 * <pre>
 * class Scratch {
 * 	public static void main(String[] args) {
 * 		TestCassandra cassandra = new TestCassandra(CqlScript.classpath("schema.cql"));
 * 		cassandra.start();
 * 		try {
 * 			//if com.datastax.oss:java-driver-core is present
 * 			CqlSession session = cassandra.getNativeConnection(CqlSession.class);
 * 			//if com.datastax.cassandra:cassandra-driver-core is present
 * 			Cluster cluster = cassandra.getNativeConnection(Cluster.class);
 *                }
 * 		finally {
 * 			cassandra.stop();
 *        }        }
 * }</pre>
 * <p>
 * By default {@link DefaultConnectionFactory} is used. This factory detects the client
 * implementation based on the classpath. {@link DefaultConnectionFactory} can be overridden via constructor.
 * </p>
 * {@link LocalCassandraFactory} is a default factory, use constructor to override it with your own factory.
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

	private final List<? extends CqlScript> scripts;

	private final ConnectionFactory connectionFactory;

	private volatile boolean started = false;

	@Nullable
	private volatile Connection connection;

	/**
	 * Creates a {@link TestCassandra} with default settings.
	 */
	public TestCassandra() {
		this(new CqlScript[0]);
	}

	/**
	 * Creates a {@link TestCassandra} with the given scripts.
	 *
	 * @param scripts CQL scripts to execute.
	 */
	public TestCassandra(CqlScript... scripts) {
		this(null, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra} with the given scripts and cassandra factory.
	 *
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute.
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory, CqlScript... scripts) {
		this(cassandraFactory, null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra} with the given scripts and connection factory.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param scripts CQL scripts to execute.
	 * @since 2.0.4
	 */
	public TestCassandra(@Nullable ConnectionFactory connectionFactory, CqlScript... scripts) {
		this(null, connectionFactory, scripts);
	}

	/**
	 * Creates a {@link TestCassandra} with the given scripts , connection factory and cassandra factory.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute.
	 * @since 2.0.4
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory, @Nullable ConnectionFactory connectionFactory,
			CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.scripts = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(scripts)));
		Cassandra cassandra = ((cassandraFactory != null) ? cassandraFactory : new LocalCassandraFactory()).create();
		this.cassandra = Objects.requireNonNull(cassandra, "Cassandra must not be null");
		this.connectionFactory = (connectionFactory != null) ? connectionFactory : new DefaultConnectionFactory();
	}

	/**
	 * Starts the underlying {@link Cassandra} and executes {@link CqlScript scripts} which were used during {@code
	 * TestCassandra} initialization. Calling this method on an already started {@code Cassandra} has no effect.
	 * Causes the current thread to wait, until the {@code Cassandra} has started.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be started
	 * @throws CassandraInterruptedException if the {@code Cassandra} was interrupted.
	 */
	@Override
	public void start() throws CassandraInterruptedException, CassandraException {
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
	 * Stops the underlying {@link Cassandra} and closes the {@link Connection} to it. Calling this method on an
	 * already stopped {@code Cassandra} has no effect. Causes the current thread to wait,
	 * until the {@code Cassandra} has stopped.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be stopped
	 * @throws CassandraInterruptedException if the {@code Cassandra} was interrupted.
	 */
	@Override
	public void stop() throws CassandraInterruptedException, CassandraException {
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
	 * Returns the <b>singleton</b> {@link Connection connection} to the underlying {@code Cassandra}. This connection
	 * is managed by {@code TestCassandra} and will be closed when {@link #stop()} is called.
	 * There is only one connection per {@code TestCassandra}. By default, {@code Connection} is created by {@link
	 * DefaultConnectionFactory}. The native session such as {@code com.datastax.driver.core.Cluster} or {@code
	 * com.datastax.oss.driver.api.core.CqlSession} can be obtained by a {@link Connection#get()} method.
	 *
	 * @return a connection
	 * @see #createConnection()
	 * @since 2.0.2
	 */
	public Connection getConnection() {
		Connection connection = this.connection;
		if (connection == null) {
			synchronized (this) {
				connection = this.connection;
				if (connection == null) {
					connection = createConnection();
					this.connection = checkConnectionNotClosed(connection);
				}
			}
		}
		return checkConnectionNotClosed(connection);
	}

	/**
	 * Returns the {@link Connection#get() native connection} to the underlying {@code Cassandra}.
	 * The shortcut for {@code <T>.class.cast(getConnection().get())}.
	 *
	 * @param <T> the native connection type
	 * @param connectionType the connection type
	 * @return a native connection
	 * @throws ClassCastException if the native {@code connection} is not assignable to the type {@code T}.
	 * @see #getConnection()
	 * @see Connection#get()
	 * @since 2.0.4
	 */
	public <T> T getNativeConnection(Class<? extends T> connectionType) throws ClassCastException {
		Objects.requireNonNull(connectionType, "Connection Type must not be null");
		return connectionType.cast(getConnection().get());
	}

	/**
	 * Executes the given {@link CqlScript scripts} using the {@link #getConnection() connection}.
	 *
	 * @param scripts the scripts
	 * @since 2.0.0
	 */
	public void executeScripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		if (scripts.length > 0) {
			getConnection().execute(scripts);
		}
	}

	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		if (!StringUtils.hasText(name)) {
			name = TestCassandra.class.getSimpleName();
		}
		return String.format("%s %s", name, getVersion());
	}

	/**
	 * Creates a new connection to the underlying {@code Cassandra} using the {@link ConnectionFactory}.
	 *
	 * @return a connection
	 * @since 2.0.2
	 */
	protected Connection createConnection() {
		return checkConnectionNotClosed(this.connectionFactory.create(getSettings()));
	}

	private static Connection checkConnectionNotClosed(@Nullable Connection connection) {
		Objects.requireNonNull(connection, "Connection must not be null");
		if (connection.isClosed()) {
			throw new IllegalStateException(String.format("Connection '%s' is closed", connection));
		}
		return connection;
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
		if (connection != null && !connection.isClosed()) {
			try {
				connection.close();
			}
			catch (Throwable ex) {
				log.error(String.format("Can not close a connection '%s'", connection), ex);
			}
		}
		this.connection = null;
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
