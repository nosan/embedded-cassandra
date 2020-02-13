/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

/**
 * Simple interface that allows the {@code Cassandra} to be {@link #start() started} and {@link #stop() stopped}.
 *
 * @author Dmytro Nosan
 * @see CassandraBuilder
 * @since 4.0.0
 */
public interface Cassandra {

	/**
	 * Starts the {@code Cassandra}. Calling this method on an already started {@code Cassandra} has no effect. Causes
	 * the current thread to wait, until the {@code Cassandra} has started.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be started
	 */
	void start() throws CassandraException;

	/**
	 * Stops the {@code Cassandra}. Calling this method on an already stopped {@code Cassandra} has no effect. Causes
	 * the current thread to wait, until the {@code Cassandra} has stopped.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be stopped
	 */
	void stop() throws CassandraException;

	/**
	 * Checks whether this Cassandra is running.
	 *
	 * @return {@code true} if the Cassandra is running
	 */
	boolean isRunning();

	/**
	 * Gets the name of this {@code Cassandra} instance.
	 *
	 * @return name of this {@code Cassandra} instance
	 */
	String getName();

	/**
	 * Gets the {@link Version} of this {@code Cassandra} instance.
	 *
	 * @return a version
	 */
	Version getVersion();

	/**
	 * Gets the {@link Settings} of this {@code Cassandra} instance. The settings can be obtained only if Cassandra was
	 * successfully started. Causes the current thread to wait, until either the {@code Cassandra} has started or
	 * stopped.
	 *
	 * @return the settings
	 * @throws IllegalStateException if Cassandra was not started
	 */
	Settings getSettings() throws IllegalStateException;

}
