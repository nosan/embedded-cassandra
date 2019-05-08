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

package com.github.nosan.embedded.cassandra;

import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Configuration properties for the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public interface Settings {

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address
	 * @throws NoSuchElementException if address is not present
	 */
	default InetAddress getAddress() throws NoSuchElementException {
		return getOptionalAddress().orElseThrow(() -> new NoSuchElementException("Address is not present"));
	}

	/**
	 * The port for client connections.
	 *
	 * @return the port
	 * @throws NoSuchElementException if port is not present
	 */
	default int getPort() throws NoSuchElementException {
		return getOptionalPort().orElseThrow(() -> new NoSuchElementException("Port is not present"));
	}

	/**
	 * SSL port for client connections.
	 *
	 * @return SSL port
	 * @throws NoSuchElementException if SSL port is not present
	 */
	default int getSslPort() throws NoSuchElementException {
		return getOptionalSslPort().orElseThrow(() -> new NoSuchElementException("SSL port is not present"));
	}

	/**
	 * RPC port for client connections.
	 *
	 * @return RPC port
	 * @throws NoSuchElementException if RPC port is not present
	 */
	default int getRpcPort() throws NoSuchElementException {
		return getOptionalRpcPort().orElseThrow(() -> new NoSuchElementException("RPC port is not present"));
	}

	/**
	 * The native transport is started.
	 *
	 * @return native transport is started
	 * @throws NoSuchElementException if transport is not present
	 */
	default boolean isTransportStarted() throws NoSuchElementException {
		return getOptionalTransportStarted()
				.orElseThrow(() -> new NoSuchElementException("Transport is not present"));
	}

	/**
	 * RPC transport is started.
	 *
	 * @return rpc transport is started
	 * @throws NoSuchElementException if RPC transport is not present
	 */
	default boolean isRpcTransportStarted() throws NoSuchElementException {
		return getOptionalRpcTransportStarted()
				.orElseThrow(() -> new NoSuchElementException("RPC transport is not present"));
	}

	/**
	 * Returns the {@link Version version}.
	 *
	 * @return a version
	 */
	Version getVersion();

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address, or {@code empty}
	 * @see #getAddress()
	 * @since 2.0.1
	 */
	Optional<InetAddress> getOptionalAddress();

	/**
	 * The port for client connections.
	 *
	 * @return the port, or {@code empty}
	 * @see #getPort()
	 * @since 2.0.1
	 */
	Optional<Integer> getOptionalPort();

	/**
	 * SSL port for client connections.
	 *
	 * @return SSL port, or {@code empty}
	 * @see #getSslPort()
	 * @since 2.0.1
	 */
	Optional<Integer> getOptionalSslPort();

	/**
	 * RPC port for client connections.
	 *
	 * @return RPC port, or {@code empty}
	 * @see #getRpcPort()
	 * @since 2.0.1
	 */
	Optional<Integer> getOptionalRpcPort();

	/**
	 * The native transport is started.
	 *
	 * @return native transport is started, or {@code empty}
	 * @see #isTransportStarted()
	 * @since 2.0.1
	 */
	Optional<Boolean> getOptionalTransportStarted();

	/**
	 * RPC transport is started.
	 *
	 * @return rpc transport is started, or {@code empty}
	 * @see #isRpcTransportStarted()
	 * @since 2.0.1
	 */
	Optional<Boolean> getOptionalRpcTransportStarted();

}
