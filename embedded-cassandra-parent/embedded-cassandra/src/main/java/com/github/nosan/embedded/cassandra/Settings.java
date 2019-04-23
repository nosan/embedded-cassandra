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
	 * The native transport is started.
	 *
	 * @return native transport is started
	 */
	boolean isTransportStarted();

	/**
	 * RPC transport is started.
	 *
	 * @return rpc transport is started
	 */
	boolean isRpcTransportStarted();

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address to listen for the clients on, or {@code empty} if RPC and Native transports are not started
	 * @see #getRequiredAddress()
	 */
	Optional<InetAddress> getAddress();

	/**
	 * The native transport {@code unencrypted} port to listen for the clients on.
	 *
	 * @return native transport port, or {@code empty}, if {@code unencrypted} transport is not started
	 * @see #getRequiredPort()
	 */
	Optional<Integer> getPort();

	/**
	 * The native transport {@code encrypted} port to listen for the clients on.
	 *
	 * @return native SSL transport port, or {@code empty}, if {@code encrypted} native transport is not started
	 * @see #getRequiredSslPort()
	 */
	Optional<Integer> getSslPort();

	/**
	 * Thrift port for client connections.
	 *
	 * @return the thrift port, or {@code empty} if RPC transport is not started
	 * @see #getRequiredRpcPort()
	 */
	Optional<Integer> getRpcPort();

	/**
	 * The address to listen for the clients on.
	 *
	 * @return the address to listen for the clients on
	 * @throws IllegalStateException if RPC and native transport are not started
	 * @since 2.0.1
	 */
	default InetAddress getRequiredAddress() throws IllegalStateException {
		if (!isRpcTransportStarted() && !isTransportStarted()) {
			throw new IllegalStateException("RPC and Native Transport are not started");
		}
		return getAddress().orElseThrow(() -> new IllegalStateException(
				"RPC or Native transport is started, but Address is not present"));
	}

	/**
	 * The native transport port to listen for the clients on.
	 *
	 * @return native transport port
	 * @throws IllegalStateException if {@code unencrypted} native transport is not started
	 * @since 2.0.1
	 */
	default int getRequiredPort() throws IllegalStateException {
		if (!isTransportStarted()) {
			throw new IllegalStateException("Native Transport is not started");
		}
		return getPort().orElseThrow(() -> new IllegalStateException(
				"Native transport is started, but <unencrypted> port is not present"));
	}

	/**
	 * The native transport SSL port to listen for the clients on.
	 *
	 * @return native SSL transport port or empty
	 * @throws IllegalStateException if {@code encrypted} native transport is not started
	 * @since 2.0.1
	 */
	default int getRequiredSslPort() throws IllegalStateException {
		if (!isTransportStarted()) {
			throw new IllegalStateException("Native transport is not started");
		}
		return getSslPort().orElseThrow(() -> new IllegalStateException(
				"Native transport is started, but <encrypted> port is not present"));
	}

	/**
	 * Thrift port for client connections.
	 *
	 * @return the thrift port
	 * @throws IllegalStateException if RPC transport is not started
	 * @since 2.0.1
	 */
	default int getRequiredRpcPort() throws IllegalStateException {
		if (!isRpcTransportStarted()) {
			throw new IllegalStateException("RPC transport is not started");
		}
		return getRpcPort().orElseThrow(() -> new IllegalStateException(
				"RPC transport is started, but rpc port is not present"));
	}

}
