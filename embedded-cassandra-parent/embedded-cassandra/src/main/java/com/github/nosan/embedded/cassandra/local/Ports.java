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

package com.github.nosan.embedded.cassandra.local;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link Cassandra} ports.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class Ports {

	@Nullable
	private final Integer port;

	@Nullable
	private final Integer rpcPort;

	@Nullable
	private final Integer storagePort;

	@Nullable
	private final Integer sslStoragePort;

	@Nullable
	private final Integer jmxLocalPort;

	Ports(@Nullable Integer port, @Nullable Integer rpcPort, @Nullable Integer storagePort,
			@Nullable Integer sslStoragePort, @Nullable Integer jmxLocalPort) {
		this.port = port;
		this.rpcPort = rpcPort;
		this.storagePort = storagePort;
		this.sslStoragePort = sslStoragePort;
		this.jmxLocalPort = jmxLocalPort;
	}

	/**
	 * The native transport port to listen for the clients on.
	 * This value will be added as {@code -Dcassandra.native_transport_port} system property.
	 *
	 * @return native transport port
	 */
	@Nullable
	Integer getPort() {
		return this.port;
	}

	/**
	 * Thrift port for client connections.
	 * This value will be added as {@code -Dcassandra.rpc_port} system property.
	 *
	 * @return the thrift port
	 */
	@Nullable
	Integer getRpcPort() {
		return this.rpcPort;
	}

	/**
	 * The port for inter-node communication.
	 * This value will be added as {@code -Dcassandra.storage_port} system property.
	 *
	 * @return storage port
	 */
	@Nullable
	Integer getStoragePort() {
		return this.storagePort;
	}

	/**
	 * The ssl port for inter-node communication.
	 * <p>
	 * This value will be added as {@code -Dcassandra.ssl_storage_port} system property.
	 *
	 * @return storage ssl port
	 */
	@Nullable
	Integer getSslStoragePort() {
		return this.sslStoragePort;
	}

	/**
	 * JMX port to listen on.
	 * <p>
	 * This value will be added as {@code -Dcassandra.jmx.local.port} system property.
	 *
	 * @return The value of the {@code jmxPort} attribute
	 */
	@Nullable
	Integer getJmxLocalPort() {
		return this.jmxLocalPort;
	}

}
