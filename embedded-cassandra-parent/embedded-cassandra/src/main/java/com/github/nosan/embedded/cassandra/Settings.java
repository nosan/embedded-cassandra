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
	 * Returns the {@link Version version}.
	 *
	 * @return a version
	 */
	Version getVersion();

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address, or {@code empty} if transports are not started
	 * @see #getAddress()
	 * @since 2.0.1
	 */
	default Optional<InetAddress> address() {
		return Optional.empty();
	}

	/**
	 * The port for client connections.
	 *
	 * @return the port, or {@code empty} if transport is not started
	 * @see #getPort()
	 * @since 2.0.1
	 */
	default Optional<Integer> port() {
		return Optional.empty();
	}

	/**
	 * SSL port for client connections.
	 *
	 * @return SSL port, or {@code empty} if SSL transport is not started
	 * @see #getSslPort()
	 * @since 2.0.1
	 */
	default Optional<Integer> sslPort() {
		return Optional.empty();
	}

	/**
	 * RPC port for client connections.
	 *
	 * @return RPC port, or {@code empty} if RPC transport is not started
	 * @see #getRpcPort()
	 * @since 2.0.1
	 */
	default Optional<Integer> rpcPort() {
		return Optional.empty();
	}

	/**
	 * The port or SSL port for client connections.
	 *
	 * @return the port, or {@code empty} if transport is not started
	 * @see #port()
	 * @see #sslPort()
	 * @see #getPortOrSslPort()
	 * @since 2.0.2
	 */
	default Optional<Integer> portOrSslPort() {
		Integer port = port().orElse(null);
		if (port != null) {
			return Optional.of(port);
		}
		return sslPort();
	}

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address
	 * @throws NoSuchElementException if address is not present
	 * @see #address()
	 */
	default InetAddress getAddress() throws NoSuchElementException {
		return address().orElseThrow(() -> new NoSuchElementException("Address is not present"));
	}

	/**
	 * The port for client connections.
	 *
	 * @return the port
	 * @throws NoSuchElementException if port is not present
	 * @see #port()
	 */
	default int getPort() throws NoSuchElementException {
		return port().orElseThrow(() -> new NoSuchElementException("Port is not present"));
	}

	/**
	 * SSL port for client connections.
	 *
	 * @return SSL port
	 * @throws NoSuchElementException if SSL port is not present
	 * @see #sslPort()
	 */
	default int getSslPort() throws NoSuchElementException {
		return sslPort().orElseThrow(() -> new NoSuchElementException("SSL port is not present"));
	}

	/**
	 * The port or SSL port for client connections.
	 *
	 * @return the port
	 * @throws NoSuchElementException if port or ssl port are not present
	 * @see #portOrSslPort()
	 * @since 2.0.2
	 */
	default int getPortOrSslPort() throws NoSuchElementException {
		return portOrSslPort().orElseThrow(() -> new NoSuchElementException("Port and SSL port are not present"));
	}

	/**
	 * RPC port for client connections.
	 *
	 * @return RPC port
	 * @throws NoSuchElementException if RPC port is not present
	 * @see #rpcPort()
	 */
	default int getRpcPort() throws NoSuchElementException {
		return rpcPort().orElseThrow(() -> new NoSuchElementException("RPC port is not present"));
	}

}
