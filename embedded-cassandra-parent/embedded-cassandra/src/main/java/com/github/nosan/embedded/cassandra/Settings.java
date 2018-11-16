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

import java.net.InetAddress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.util.NetworkUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;


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
	 * @return The value of the {@code storagePort} attribute or {@code 7000}
	 */
	int getStoragePort();

	/**
	 * SSL port, for encrypted communication.
	 *
	 * @return The value of the {@code sslStoragePort} attribute or {@code 7001}
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
	 * The address to bind the native/rpc transport server to.
	 * Note! use {@link #getRealAddress()} to connect to the cassandra.
	 *
	 * @return The value of the {@code rpcAddress} attribute
	 * @see #getRealAddress()
	 */
	@Nullable
	String getRpcAddress();

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
	 * @return The value of the {@code nativeTransportPort} attribute or {@code 9042}
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
	 * @return The value of the {@code rpcPort} attribute or {@code 9160}
	 */
	int getRpcPort();

	/**
	 * Cassandra version.
	 *
	 * @return The value of the {@code version} attribute
	 * @since 1.1.0
	 */
	@Nonnull
	Version getVersion();

	/**
	 * When using multiple physical network interfaces, set this to true to listen on broadcast_address in addition
	 * to the {@code listen_address}, allowing nodes to communicate in both interfaces.
	 *
	 * @return The value of the {@code listenOnBroadcastAddress} attribute
	 * @since 1.1.0
	 */
	boolean isListenOnBroadcastAddress();

	/**
	 * If you choose to specify the interface by name and the interface has an IPv4 and an IPv6 address you can
	 * specify which should be chosen using {@code listen_interface_prefer_ipv6}. If {@code false} the first IPv4
	 * address will be used. If {@code true} the first IPv6 address will be used.
	 *
	 * @return The value of the {@code listenInterfacePreferIpv6} attribute
	 * @since 1.1.0
	 */
	boolean isListenInterfacePreferIpv6();

	/**
	 * If you choose to specify the interface by name and the interface has an IPv4 and an IPv6 address you can
	 * specify which should be chosen using {@code rpc_interface_prefer_ipv6}. If {@code false} the first IPv4 address
	 * will be used. If {@code true} the first IPv6 address will be used.
	 *
	 * @return The value of the {@code rpcInterfacePreferIpv6} attribute
	 * @since 1.1.0
	 */
	boolean isRpcInterfacePreferIpv6();

	/**
	 * The {@code real} address to bind the native/rpc transport server to.
	 *
	 * @return The value of the {@code rpcAddress} or determine address from {@code rpcInterface} attribute
	 * @since 1.1.0
	 */
	@Nonnull
	default InetAddress getRealAddress() {
		String rpcAddress = getRpcAddress();
		if (StringUtils.hasText(rpcAddress)) {
			return NetworkUtils.getInetAddress(rpcAddress);
		}
		String rpcInterface = getRpcInterface();
		if (StringUtils.hasText(rpcInterface)) {
			return NetworkUtils.getAddressByInterface(rpcInterface, isRpcInterfacePreferIpv6());
		}
		return NetworkUtils.getLocalhost();
	}

	/**
	 * the {@code real} address to bind to and tell other Cassandra nodes to connect to.
	 *
	 * @return The value of the {@code listenAddress} or determine address from {@code listenInterface} attribute
	 * @since 1.1.0
	 */
	@Nonnull
	default InetAddress getRealListenAddress() {
		String listenAddress = getListenAddress();
		if (StringUtils.hasText(listenAddress)) {
			return NetworkUtils.getInetAddress(listenAddress);
		}
		String listenInterface = getListenInterface();
		if (StringUtils.hasText(listenInterface)) {
			return NetworkUtils.getAddressByInterface(listenInterface, isListenInterfacePreferIpv6());
		}
		return NetworkUtils.getLocalhost();
	}

}
