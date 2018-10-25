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

package com.github.nosan.embedded.cassandra.local;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Map {@link Settings}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class MapSettings implements Settings {

	@Nullable
	private final String clusterName;

	private final int port;

	private final int rpcPort;

	private final int storagePort;

	private final int sslStoragePort;

	private final boolean startNativeTransport;

	private final boolean startRpc;

	@Nullable
	private final Integer sslPort;

	@Nullable
	private final String address;

	@Nullable
	private final String listenAddress;

	@Nullable
	private final String listenInterface;

	@Nullable
	private final String broadcastAddress;

	@Nullable
	private final String rpcInterface;

	@Nullable
	private final String broadcastRpcAddress;

	/**
	 * Creates a new settings.
	 *
	 * @param source a source {@code Map}
	 */
	MapSettings(@Nonnull Map<?, ?> source) {
		this.clusterName = get("cluster_name", source);
		this.port = getInt("native_transport_port", source);
		this.address = get("rpc_address", source);
		this.rpcPort = getInt("rpc_port", source);
		this.storagePort = getInt("storage_port", source);
		this.sslStoragePort = getInt("ssl_storage_port", source);
		this.startNativeTransport = getBool("start_native_transport", source);
		this.startRpc = getBool("start_rpc", source);
		this.sslPort = getInteger("native_transport_port_ssl", source);
		this.listenAddress = get("listen_address", source);
		this.listenInterface = get("listen_interface", source);
		this.broadcastAddress = get("broadcast_address", source);
		this.rpcInterface = get("rpc_interface", source);
		this.broadcastRpcAddress = get("broadcast_rpc_address", source);
	}

	@Override
	@Nullable
	public String getClusterName() {
		return this.clusterName;
	}

	@Override
	public int getStoragePort() {
		return this.storagePort;
	}

	@Override
	public int getSslStoragePort() {
		return this.sslStoragePort;
	}

	@Override
	@Nullable
	public String getListenAddress() {
		return this.listenAddress;
	}

	@Override
	@Nullable
	public String getListenInterface() {
		return this.listenInterface;
	}

	@Override
	@Nullable
	public String getBroadcastAddress() {
		return this.broadcastAddress;
	}

	@Override
	@Nullable
	public String getAddress() {
		return this.address;
	}

	@Override
	@Nullable
	public String getRpcInterface() {
		return this.rpcInterface;
	}

	@Override
	@Nullable
	public String getBroadcastRpcAddress() {
		return this.broadcastRpcAddress;
	}

	@Override
	public boolean isStartNativeTransport() {
		return this.startNativeTransport;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	@Nullable
	public Integer getSslPort() {
		return this.sslPort;
	}

	@Override
	public boolean isStartRpc() {
		return this.startRpc;
	}

	@Override
	public int getRpcPort() {
		return this.rpcPort;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.clusterName, this.port, this.rpcPort, this.storagePort, this.sslStoragePort,
				this.startNativeTransport, this.startRpc, this.sslPort, this.address, this.listenAddress,
				this.listenInterface, this.broadcastAddress, this.rpcInterface, this.broadcastRpcAddress);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Settings)) {
			return false;
		}
		Settings settings = (Settings) other;
		return this.port == settings.getPort() &&
				this.rpcPort == settings.getRpcPort() &&
				this.storagePort == settings.getStoragePort() &&
				this.sslStoragePort == settings.getSslStoragePort() &&
				this.startNativeTransport == settings.isStartNativeTransport() &&
				this.startRpc == settings.isStartRpc() &&
				Objects.equals(this.clusterName, settings.getClusterName()) &&
				Objects.equals(this.sslPort, settings.getSslPort()) &&
				Objects.equals(this.address, settings.getAddress()) &&
				Objects.equals(this.listenAddress, settings.getListenAddress()) &&
				Objects.equals(this.listenInterface, settings.getListenInterface()) &&
				Objects.equals(this.broadcastAddress, settings.getBroadcastAddress()) &&
				Objects.equals(this.rpcInterface, settings.getRpcInterface()) &&
				Objects.equals(this.broadcastRpcAddress, settings.getBroadcastRpcAddress());
	}

	@Override
	@Nonnull
	public String toString() {
		return '{' +
				"clusterName=" + this.clusterName +
				", port=" + this.port +
				", rpcPort=" + this.rpcPort +
				", storagePort=" + this.storagePort +
				", sslStoragePort=" + this.sslStoragePort +
				", startNativeTransport=" + this.startNativeTransport +
				", startRpc=" + this.startRpc +
				", sslPort=" + this.sslPort +
				", address=" + this.address +
				", listenAddress=" + this.listenAddress +
				", listenInterface=" + this.listenInterface +
				", broadcastAddress=" + this.broadcastAddress +
				", rpcInterface=" + this.rpcInterface +
				", broadcastRpcAddress=" + this.broadcastRpcAddress +
				'}';
	}


	@Nullable
	private static Integer getInteger(String name, Map<?, ?> source) {
		String value = get(name, source);
		return StringUtils.hasText(value) ? Integer.parseInt(value) : null;
	}

	private static int getInt(String name, Map<?, ?> source) {
		String value = get(name, source);
		return StringUtils.hasText(value) ? Integer.parseInt(value) : -1;

	}

	private static boolean getBool(String name, Map<?, ?> source) {
		return Boolean.valueOf(get(name, source));
	}

	@Nullable
	private static String get(String name, Map<?, ?> source) {
		Object val = source.get(name);
		return (val != null) ? val.toString() : null;
	}


}
