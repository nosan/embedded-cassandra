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
import java.util.stream.Collectors;

import com.datastax.driver.core.Cluster;
import com.datastax.oss.driver.api.core.CqlSession;
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
import com.github.nosan.embedded.cassandra.test.util.CqlSessionUtils;
import com.github.nosan.embedded.cassandra.test.util.SessionUtils;
import com.github.nosan.embedded.cassandra.util.ClassUtils;

/**
 * Test {@link Cassandra} that allows the Cassandra to be {@link #start() started} and {@link #stop() stopped}. {@link
 * TestCassandra} does not launch {@link Cassandra} itself, it simply delegates calls to the underlying {@link
 * Cassandra}.
 *
 * @author Dmytro Nosan
 * @see CassandraFactory
 * @see CqlSessionFactory
 * @see ClusterFactory
 * @see SessionUtils
 * @see CqlSessionUtils
 * @see CqlScript
 * @since 1.0.0
 */
public class TestCassandra {

	private static final Logger log = LoggerFactory.getLogger(TestCassandra.class);

	private static final String CQL_SESSION_CLASS = "com.datastax.oss.driver.api.core.CqlSession";

	private static final String CLUSTER_CLASS = "com.datastax.driver.core.Cluster";

	private static final String SESSION_CLASS = "com.datastax.driver.core.Session";

	private final Object monitor = new Object();

	private final Cassandra cassandra;

	private final List<CqlScript> scripts;

	/**
	 * Creates a {@link TestCassandra}.
	 */
	public TestCassandra() {
		this(null, new CqlScript[0]);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(CqlScript... scripts) {
		this(null, scripts);
	}

	/**
	 * Creates a {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public TestCassandra(@Nullable CassandraFactory cassandraFactory, CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.scripts = Collections.unmodifiableList(Arrays.asList(scripts));
		Cassandra cassandra = ((cassandraFactory != null) ? cassandraFactory : new LocalCassandraFactory()).create();
		this.cassandra = Objects.requireNonNull(cassandra, "Cassandra must not be null");
	}

	/**
	 * Starts the underlying {@link Cassandra}. Calling this method on an already started {@code Cassandra} has no
	 * effect. Causes the current thread to wait, until the {@code Cassandra} has started.
	 *
	 * @throws CassandraException if the underlying {@code Cassandra} cannot be started
	 * @throws CassandraInterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another
	 * thread
	 */
	public void start() throws CassandraException {
		synchronized (this.monitor) {
			try {
				startInternal();
			}
			catch (CassandraException ex) {
				stopInternalSilently();
				throw ex;
			}
			catch (Exception ex) {
				stopInternalSilently();
				throw new CassandraException(String.format("Unable to start Test Apache Cassandra '%s'", getVersion()));
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
	public void stop() throws CassandraException {
		synchronized (this.monitor) {
			try {
				stopInternal();
			}
			catch (CassandraException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new CassandraException(String.format("Unable to stop Test Apache Cassandra '%s'", getVersion()));
			}
		}

	}

	/**
	 * Returns the settings of the underlying {@code Cassandra}.
	 *
	 * @return the settings
	 * @throws IllegalStateException If the underlying {@link Cassandra} is not running
	 */
	public Settings getSettings() throws IllegalStateException {
		synchronized (this.monitor) {
			return this.cassandra.getSettings();
		}
	}

	/**
	 * Returns the {@link Version version} of this {@code Cassandra}.
	 *
	 * @return a version
	 * @since 2.0.0
	 */
	public Version getVersion() {
		return this.cassandra.getVersion();
	}

	@Override
	public String toString() {
		return String.format("Test Apache Cassandra '%s'", getVersion());
	}

	/**
	 * Executes the {@link CqlScript scripts}.
	 *
	 * @param scripts the scripts
	 * @since 2.0.0
	 */
	public void executeScripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		ClassLoader classLoader = getClass().getClassLoader();
		Settings settings = getSettings();
		if (ClassUtils.isPresent(CQL_SESSION_CLASS, classLoader)) {
			try (CqlSession session = new CqlSessionFactory().create(settings)) {
				CqlSessionUtils.executeScripts(session, scripts);
			}
		}
		else if (ClassUtils.isPresent(CLUSTER_CLASS, classLoader) && ClassUtils.isPresent(SESSION_CLASS, classLoader)) {
			try (Cluster cluster = new ClusterFactory().create(settings)) {
				SessionUtils.executeScripts(cluster.connect(), scripts);
			}
		}
		else {
			throw new IllegalStateException(String.format("There is no way to execute '%s'."
							+ " '%s' and ('%s' or '%s') classes are not present in the classpath.",
					Arrays.stream(scripts).map(String::valueOf).collect(Collectors.joining(",")),
					CQL_SESSION_CLASS, CLUSTER_CLASS, SESSION_CLASS));
		}
	}

	private void startInternal() {
		if (log.isDebugEnabled()) {
			log.debug("Starts Test Apache Cassandra '{}'", getVersion());
		}
		this.cassandra.start();
		if (!this.scripts.isEmpty()) {
			executeScripts(this.scripts.toArray(new CqlScript[0]));
		}
		if (log.isDebugEnabled()) {
			log.debug("Test Apache Cassandra '{}' is started", getVersion());
		}
	}

	private void stopInternal() {
		this.cassandra.stop();
		Cassandra.State state = this.cassandra.getState();
		if (log.isDebugEnabled() && state == Cassandra.State.STOPPED) {
			log.debug("Test Apache Cassandra '{}' is stopped", getVersion());
		}
	}

	private void stopInternalSilently() {
		try {
			stopInternal();
		}
		catch (CassandraInterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		catch (Exception ex) {
			log.error(String.format("Unable to stop Test Apache Cassandra '%s'", getVersion()), ex);
		}
	}

}
