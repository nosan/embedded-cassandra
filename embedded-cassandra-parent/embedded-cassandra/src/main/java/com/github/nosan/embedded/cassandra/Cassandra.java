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
	 * Returns the settings this {@code Cassandra} is running on. These settings can be retrieved only if {@code
	 * Cassandra} state is {@link State#STARTED started}.
	 *
	 * @return the settings
	 * @throws CassandraException if {@code Cassandra} is not started.
	 */
	Settings getSettings() throws CassandraException;

	/**
	 * Returns the {@link State state} of the {@code Cassandra}.
	 *
	 * @return the state
	 * @since 1.4.1
	 */
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
		 * {@code Cassandra} has been starting and has not yet ready to allow the connection requests.
		 */
		STARTING,
		/**
		 * The {@link #start() startup} has failed.
		 */
		START_FAILED,
		/**
		 * The {@link #start() startup} has {@link Thread#interrupt() interrupted}.
		 */
		START_INTERRUPTED,
		/**
		 * {@code Cassandra} has been started and is ready to accept new connections.
		 */
		STARTED,
		/**
		 * {@code Cassandra} has been stopping.
		 */
		STOPPING,
		/**
		 * The {@link #stop() shutdown} has failed.
		 */
		STOP_FAILED,
		/**
		 * The {@link #stop() shutdown} has {@link Thread#interrupt() interrupted}.
		 */
		STOP_INTERRUPTED,
		/**
		 * {@code Cassandra} has been stopped.
		 */
		STOPPED,
		/**
		 * The state of the {@code Cassandra} is unknown.
		 */
		UNKNOWN
	}

}

