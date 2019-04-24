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
	 * @return the address, or {@code empty}
	 * @see #getRequiredAddress()
	 */
	Optional<InetAddress> getAddress();

	/**
	 * The port for client connections.
	 *
	 * @return the port, or {@code empty}
	 * @see #getRequiredPort()
	 */
	Optional<Integer> getPort();

	/**
	 * SSL port for client connections.
	 *
	 * @return SSL port, or {@code empty}
	 * @see #getRequiredSslPort()
	 */
	Optional<Integer> getSslPort();

	/**
	 * RPC port for client connections.
	 *
	 * @return RPC port, or {@code empty}
	 * @see #getRequiredRpcPort()
	 */
	Optional<Integer> getRpcPort();

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address
	 * @throws NoSuchElementException if address is not present
	 * @since 2.0.1
	 */
	default InetAddress getRequiredAddress() throws NoSuchElementException {
		return getAddress().orElseThrow(() -> new NoSuchElementException("Address is not present"));
	}

	/**
	 * The port for client connections.
	 *
	 * @return the port
	 * @throws NoSuchElementException if port is not present
	 * @since 2.0.1
	 */
	default int getRequiredPort() throws NoSuchElementException {
		return getPort().orElseThrow(() -> new NoSuchElementException("Port is not present"));
	}

	/**
	 * SSL port for client connections.
	 *
	 * @return SSL port
	 * @throws NoSuchElementException if SSL port is not present
	 * @since 2.0.1
	 */
	default int getRequiredSslPort() throws NoSuchElementException {
		return getSslPort().orElseThrow(() -> new NoSuchElementException("SSL port is not present"));
	}

	/**
	 * RPC port for client connections.
	 *
	 * @return RPC port
	 * @throws NoSuchElementException if RPC port is not present
	 * @since 2.0.1
	 */
	default int getRequiredRpcPort() throws NoSuchElementException {
		return getRpcPort().orElseThrow(() -> new NoSuchElementException("RPC port is not present"));
	}

}
