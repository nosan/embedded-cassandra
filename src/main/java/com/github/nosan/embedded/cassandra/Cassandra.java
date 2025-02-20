/*
 * Copyright 2020-2025 the original author or authors.
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

import java.nio.file.Path;

/**
 * A simple interface that allows {@code Cassandra} to be {@link #start() started} and {@link #stop() stopped}.
 * <p>
 * Refer to the <a href="package-summary.html#package_description">package overview</a> for more information.
 * </p>
 *
 * @author Dmytro Nosan
 * @see CassandraBuilder
 * @since 4.0.0
 */
public interface Cassandra {

	/**
	 * Starts {@code Cassandra}. Calling this method on an already started {@code Cassandra} has no effect. Causes the
	 * current thread to wait until {@code Cassandra} has started.
	 *
	 * @throws CassandraException if {@code Cassandra} cannot be started
	 */
	void start() throws CassandraException;

	/**
	 * Stops {@code Cassandra}. Calling this method on an already stopped {@code Cassandra} has no effect. Causes the
	 * current thread to wait until {@code Cassandra} has stopped.
	 *
	 * @throws CassandraException if {@code Cassandra} cannot be stopped
	 */
	void stop() throws CassandraException;

	/**
	 * Checks whether this {@code Cassandra} instance is running.
	 *
	 * @return {@code true} if {@code Cassandra} is running, otherwise {@code false}
	 */
	boolean isRunning();

	/**
	 * Gets the name of this {@code Cassandra} instance.
	 *
	 * @return the name of the instance, never {@code null}
	 */
	String getName();

	/**
	 * Retrieves the {@link Version} of this {@code Cassandra} instance.
	 *
	 * @return the version of the instance, never {@code null}
	 */
	Version getVersion();

	/**
	 * Retrieves the working directory of this {@code Cassandra} instance.
	 *
	 * @return the working directory, never {@code null}
	 */
	Path getWorkingDirectory();

	/**
	 * Retrieves the {@link Settings} of this {@code Cassandra} instance. The settings can only be obtained if
	 * {@code Cassandra} was successfully started. Causes the current thread to wait until {@code Cassandra} has either
	 * started or stopped.
	 *
	 * @return the settings of the instance
	 * @throws IllegalStateException if {@code Cassandra} was not started
	 */
	Settings getSettings() throws IllegalStateException;

}
