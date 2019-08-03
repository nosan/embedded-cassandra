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

package com.github.nosan.embedded.cassandra.api;

import java.net.InetAddress;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * Simple interface that allows the {@code Cassandra} to be  {@link #start() started} and {@link #stop() stopped}.
 *
 * @author Dmytro Nosan
 * @see CassandraFactory
 * @since 3.0.0
 */
public interface Cassandra {

	/**
	 * Starts the {@code Cassandra}. Calling this method on an already started {@code Cassandra} has no effect. Causes
	 * the current thread to wait, until the {@code Cassandra} has started.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be started
	 * @throws CassandraInterruptedException if the {@code Cassandra} has been interrupted.
	 */
	void start() throws CassandraException, CassandraInterruptedException;

	/**
	 * Stops the {@code Cassandra}. Calling this method on an already stopped {@code Cassandra} has no effect. Causes
	 * the current thread to wait, until the {@code Cassandra} has started.
	 *
	 * @throws CassandraException if the {@code Cassandra} cannot be stopped
	 * @throws CassandraInterruptedException if the {@code Cassandra} has been interrupted.
	 */
	void stop() throws CassandraException, CassandraInterruptedException;

	/**
	 * Returns the name of this {@code Cassandra} instance.
	 *
	 * @return name of this {@code Cassandra} instance
	 */
	String getName();

	/**
	 * Returns the {@link Version}.
	 *
	 * @return a version
	 */
	Version getVersion();

	/**
	 * Returns the address ({@code rpc_address}) this {@link Cassandra} is listening on.
	 *
	 * @return the address (or null if none)
	 */
	@Nullable
	default InetAddress getAddress() {
		return null;
	}

	/**
	 * Returns the native transport port ({@code native_transport_port}) or SSL port ({@code native_transport_port_ssl})
	 * this {@link Cassandra} is listening on.
	 *
	 * @return the port or SSL port (or -1 if none)
	 */
	default int getPort() {
		return -1;
	}

	/**
	 * Returns the native transport SSL port ({@code native_transport_port_ssl}) this {@link Cassandra} is listening
	 * on.
	 *
	 * @return the SSL port (or -1 if none)
	 */
	default int getSslPort() {
		return -1;
	}

	/**
	 * Returns the RPC transport port ({@code rpc_port}) this {@link Cassandra} is listening on.
	 *
	 * @return the RPC port (or -1 if none)
	 */
	default int getRpcPort() {
		return -1;
	}

}

