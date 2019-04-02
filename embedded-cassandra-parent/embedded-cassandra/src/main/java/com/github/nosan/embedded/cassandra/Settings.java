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
import java.util.Collections;
import java.util.Map;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.NetworkUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Configuration properties for the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public interface Settings {

	/**
	 * The name of the cluster. This is mainly used to prevent machines in one logical cluster from joining another.
	 *
	 * @return The value of the {@code clusterName} attribute
	 */
	@Nullable
	default String getClusterName() {
		return null;
	}

	/**
	 * The port for inter-node communication.
	 *
	 * @return The value of the {@code storagePort} attribute or {@code 7000}
	 */
	default int getStoragePort() {
		return 7000;
	}

	/**
	 * SSL port, for encrypted communication.
	 *
	 * @return The value of the {@code sslStoragePort} attribute or {@code 7001}
	 */
	default int getSslStoragePort() {
		return 7001;
	}

	/**
	 * Address to bind to and tell other Cassandra nodes to connect to.
	 *
	 * @return The value of the {@code listenAddress} attribute
	 * @see #getRealListenAddress()
	 */
	@Nullable
	default String getListenAddress() {
		return null;
	}

	/**
	 * The interface that Cassandra binds to for connecting to other Cassandra nodes.
	 *
	 * @return The value of the {@code listenInterface} attribute
	 */
	@Nullable
	default String getListenInterface() {
		return null;
	}

	/**
	 * The IP address a node tells other nodes in the cluster to contact it by.
	 *
	 * @return The value of the {@code broadcastAddress} attribute
	 */
	@Nullable
	default String getBroadcastAddress() {
		return null;
	}

	/**
	 * The address to bind the native/rpc transport server to.
	 * <p><b>Note!</b> use {@link #getRealAddress()} to connect to the cassandra.
	 *
	 * @return The value of the {@code rpcAddress} attribute
	 * @see #getRealAddress()
	 */
	@Nullable
	default String getRpcAddress() {
		return null;
	}

	/**
	 * The listen RPC interface for client connections.
	 *
	 * @return The value of the {@code rpcInterface} attribute
	 */
	@Nullable
	default String getRpcInterface() {
		return null;
	}

	/**
	 * RPC address to broadcast to drivers and other Cassandra nodes.
	 *
	 * @return The value of the {@code broadcastRpcAddress} attribute
	 */
	@Nullable
	default String getBroadcastRpcAddress() {
		return null;
	}

	/**
	 * Whether native transport is started or not.
	 *
	 * @return The value of the {@code startNativeTransport} attribute
	 */
	default boolean isStartNativeTransport() {
		return getVersion().getMajor() > 2;
	}

	/**
	 * Port for the CQL native transport to listen for clients on.
	 *
	 * @return The value of the {@code nativeTransportPort} attribute or {@code 9042}
	 */
	default int getPort() {
		return 9042;
	}

	/**
	 * SSL Port for the CQL native transport.
	 *
	 * @return The value of the {@code nativeTransportPortSsl} attribute
	 */
	@Nullable
	default Integer getSslPort() {
		return null;
	}

	/**
	 * Whether RPC transport is started or not.
	 *
	 * @return The value of the {@code startRpc} attribute
	 */
	default boolean isStartRpc() {
		return getVersion().getMajor() < 4;
	}

	/**
	 * Thrift port for client connections.
	 *
	 * @return The value of the {@code rpcPort} attribute or {@code 9160} / {@code -1}.
	 */
	default int getRpcPort() {
		return getVersion().getMajor() < 4 ? 9160 : -1;
	}

	/**
	 * Cassandra version.
	 *
	 * @return The value of the {@code version} attribute
	 * @since 1.1.0
	 */
	default Version getVersion() {
		throw new UnsupportedOperationException("This method is not implemented");
	}

	/**
	 * When using multiple physical network interfaces, set this to true to listen on {@code broadcast_address} in
	 * addition to the {@code listen_address}, allowing nodes to communicate in both interfaces.
	 *
	 * @return The value of the {@code listenOnBroadcastAddress} attribute
	 * @since 1.1.0
	 */
	default boolean isListenOnBroadcastAddress() {
		return false;
	}

	/**
	 * If you choose to specify the interface by name and the interface has an IPv4 and an IPv6 address you can specify
	 * which should be chosen using {@code listen_interface_prefer_ipv6}. If {@code false} the first IPv4 address will
	 * be used. If {@code true} the first IPv6 address will be used.
	 *
	 * @return The value of the {@code listenInterfacePreferIpv6} attribute
	 * @since 1.1.0
	 */
	default boolean isListenInterfacePreferIpv6() {
		return false;
	}

	/**
	 * If you choose to specify the interface by name and the interface has an IPv4 and an IPv6 address you can specify
	 * which should be chosen using {@code rpc_interface_prefer_ipv6}. If {@code false} the first IPv4 address will be
	 * used. If {@code true} the first IPv6 address will be used.
	 *
	 * @return The value of the {@code rpcInterfacePreferIpv6} attribute
	 * @since 1.1.0
	 */
	default boolean isRpcInterfacePreferIpv6() {
		return false;
	}

	/**
	 * The {@code real} address to listen the native/rpc transport.
	 *
	 * @return The value of the {@code rpcAddress} or determine address from {@code rpcInterface} attribute
	 * @since 1.1.0
	 */
	default InetAddress getRealAddress() {
		String rpcAddress = getRpcAddress();
		if (StringUtils.hasText(rpcAddress)) {
			return NetworkUtils.getInetAddress(rpcAddress);
		}
		String rpcInterface = getRpcInterface();
		if (StringUtils.hasText(rpcInterface)) {
			return NetworkUtils.getAddressByInterface(rpcInterface, isRpcInterfacePreferIpv6()).orElseGet(
					() -> NetworkUtils.getAddressesByInterface(rpcInterface).stream().findFirst().orElseThrow(
							() -> new IllegalStateException(
									String.format("There is no address for interface '%s'", rpcInterface))));
		}
		return getRealListenAddress();
	}

	/**
	 * The {@code real} address to listen and tell other Cassandra nodes to connect to.
	 *
	 * @return The value of the {@code listenAddress} or determine address from {@code listenInterface} attribute
	 * @since 1.1.0
	 */
	default InetAddress getRealListenAddress() {
		String listenAddress = getListenAddress();
		if (StringUtils.hasText(listenAddress)) {
			return NetworkUtils.getInetAddress(listenAddress);
		}
		String listenInterface = getListenInterface();
		if (StringUtils.hasText(listenInterface)) {
			return NetworkUtils.getAddressByInterface(listenInterface, isListenInterfacePreferIpv6()).orElseGet(
					() -> NetworkUtils.getAddressesByInterface(listenInterface).stream().findFirst().orElseThrow(
							() -> new IllegalStateException(
									String.format("There is no address for interface '%s'", listenInterface))));
		}
		return NetworkUtils.getLocalhost();
	}

	/**
	 * The {@code Cassandra} <b>raw</b> properties.
	 *
	 * @return the node properties (may be empty)
	 * @since 1.3.0
	 */
	default Map<Object, Object> getProperties() {
		return Collections.emptyMap();
	}

}
