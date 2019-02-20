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

package com.github.nosan.embedded.cassandra;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Simple interface that allows the {@code Cassandra} to be {@link #start() started} and {@link #stop()
 * stopped}.
 *
 * @author Dmytro Nosan
 * @see CassandraFactory
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public interface Cassandra {

	/**
	 * Starts the {@code Cassandra}. Calling this method on an already started {@code Cassandra} has no
	 * effect. Causes the current thread to wait, until the {@code Cassandra} has
	 * started.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be started
	 */
	void start() throws CassandraException;

	/**
	 * Stops the {@code Cassandra}. Calling this method on an already stopped {@code Cassandra} has no
	 * effect. Causes the current thread to wait, until the {@code Cassandra} has stopped.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be stopped
	 */
	void stop() throws CassandraException;

	/**
	 * Returns the settings this {@code Cassandra} is running on.
	 *
	 * @return the settings
	 * @throws CassandraException if {@code Cassandra} is not started.
	 */
	@Nonnull
	Settings getSettings() throws CassandraException;

	/**
	 * Returns the {@link State state} of the {@code Cassandra}.
	 *
	 * @return the state
	 * @since 1.4.1
	 */
	@Nonnull
	default State getState() {
		return State.UNKNOWN;
	}

	/**
	 * Enumeration of <em>states</em> that describes in which state {@code Cassandra} can be.
	 *
	 * @since 1.4.1
	 */
	@API(since = "1.4.1", status = API.Status.STABLE)
	enum State {

		/**
		 * {@code Cassandra} instance is newly created.
		 */
		NEW,
		/**
		 * {@code Cassandra} has been initializing, e.g. <em>Working Directory</em> is being prepared.
		 */
		INITIALIZING,
		/**
		 * {@code Cassandra} has been initialized.
		 */
		INITIALIZED,
		/**
		 * {@code Cassandra} has been starting and has not yet ready to allow the connection requests.
		 */
		STARTING,
		/**
		 * {@code Cassandra} has been started and is ready to accept new connections.
		 */
		STARTED,
		/**
		 * {@code Cassandra} has been stopping.
		 */
		STOPPING,
		/**
		 * {@code Cassandra} has been stopped.
		 */
		STOPPED,
		/**
		 * The {@link #start() startup} or {@link #stop() shutdown} failed.
		 */
		FAILED,
		/**
		 * The Startup {@link Thread#interrupt() interrupted}.
		 */
		INTERRUPTED,
		/**
		 * The state of the {@code Cassandra} is unknown.
		 */
		UNKNOWN
	}

}

