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

package com.github.nosan.embedded.cassandra;

import javax.annotation.Nonnull;


/**
 * Simple interface that allows the Cassandra to be {@link #start() started} and {@link #stop()
 * stopped}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public interface Cassandra {

	/**
	 * Starts the Cassandra. Calling this method on an already started {@code Cassandra} has no
	 * effect.
	 *
	 * @throws CassandraException if the Cassandra cannot be started
	 */
	void start() throws CassandraException;

	/**
	 * Stops the Cassandra. Calling this method on an already stopped {@code Cassandra} has no
	 * effect.
	 *
	 * @throws CassandraException if the Cassandra cannot be stopped
	 */
	void stop() throws CassandraException;

	/**
	 * Returns the settings this Cassandra is running on.
	 *
	 * @return the settings
	 * @throws CassandraException if Cassandra has not been started
	 */
	@Nonnull
	Settings getSettings() throws CassandraException;

}

