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

import javax.annotation.Nullable;


/**
 * Configuration properties for the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public interface Settings {

	/**
	 * The name of the cluster. This is mainly used to prevent machines in
	 * one logical cluster from joining another.
	 *
	 * @return The value of the {@code clusterName} attribute
	 */
	@Nullable
	String getClusterName();

	/**
	 * The port for inter-node communication.
	 *
	 * @return The value of the {@code storagePort} attribute or {@code -1}
	 */
	int getStoragePort();

	/**
	 * SSL port, for encrypted communication.
	 *
	 * @return The value of the {@code sslStoragePort} attribute or {@code -1}
	 */
	int getSslStoragePort();

	/**
	 * Address to bind to and tell other Cassandra nodes to connect to.
	 *
	 * @return The value of the {@code listenAddress} attribute
	 */
	@Nullable
	String getListenAddress();

	/**
	 * The interface that Cassandra binds to for connecting to other Cassandra nodes.
	 *
	 * @return The value of the {@code listenInterface} attribute
	 */
	@Nullable
	String getListenInterface();

	/**
	 * The IP address a node tells other nodes in the cluster to contact it by.
	 *
	 * @return The value of the {@code broadcastAddress} attribute
	 */
	@Nullable
	String getBroadcastAddress();

	/**
	 * The address to bind the native transport server to.
	 *
	 * @return The value of the {@code address} attribute
	 */
	@Nullable
	String getAddress();

	/**
	 * The listen RPC interface for client connections.
	 *
	 * @return The value of the {@code rpcInterface} attribute
	 */
	@Nullable
	String getRpcInterface();

	/**
	 * RPC address to broadcast to drivers and other Cassandra nodes.
	 *
	 * @return The value of the {@code broadcastRpcAddress} attribute
	 */
	@Nullable
	String getBroadcastRpcAddress();

	/**
	 * Whether native transport is started or not.
	 *
	 * @return The value of the {@code startNativeTransport} attribute
	 */
	boolean isStartNativeTransport();

	/**
	 * Port for the CQL native transport to listen for clients on.
	 *
	 * @return The value of the {@code nativeTransportPort} attribute or {@code -1}
	 */
	int getPort();

	/**
	 * SSL Port for the CQL native transport.
	 *
	 * @return The value of the {@code nativeTransportPortSsl} attribute
	 */
	@Nullable
	Integer getSslPort();

	/**
	 * Whether RPC transport is started or not.
	 *
	 * @return The value of the {@code startRpc} attribute
	 */
	boolean isStartRpc();

	/**
	 * Thrift port for client connections.
	 *
	 * @return The value of the {@code rpcPort} attribute or {@code -1}
	 */
	int getRpcPort();

}
