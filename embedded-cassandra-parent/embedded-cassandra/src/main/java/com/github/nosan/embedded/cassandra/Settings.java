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
	 * The address to listen the native or rpc transport.
	 *
	 * @return the address to listen for the clients
	 * @throws IllegalStateException if rpc and native transport are disabled
	 */
	InetAddress getAddress() throws IllegalStateException;

	/**
	 * Whether native transport is started or not.
	 *
	 * @return native transport is enabled
	 */
	boolean isTransportEnabled();

	/**
	 * The native transport port to listen for the clients on.
	 *
	 * @return native transport port
	 * @throws IllegalStateException if native transport is disabled
	 */
	int getPort() throws IllegalStateException;

	/**
	 * The native transport ssl port to listen for the clients on.
	 *
	 * @return native ssl transport port or empty
	 * @throws IllegalStateException if native transport is disabled
	 */
	Optional<Integer> getSslPort() throws IllegalStateException;

	/**
	 * Whether RPC transport is started or not.
	 *
	 * @return rpc transport is enabled
	 */
	boolean isRpcTransportEnabled();

	/**
	 * Thrift port for client connections.
	 *
	 * @return the thrift port
	 * @throws IllegalStateException if rpc transport is disabled
	 */
	int getRpcPort() throws IllegalStateException;

	/**
	 * Returns the {@link Version version}.
	 *
	 * @return a version
	 */
	Version getVersion();

}
