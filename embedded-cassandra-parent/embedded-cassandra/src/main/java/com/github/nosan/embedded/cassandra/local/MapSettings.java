/*
 * Copyright 2018-2019 the original author or authors.
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Map {@link Settings}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
final class MapSettings implements Settings {

	private final int port;

	private final int rpcPort;

	private final int storagePort;

	private final int sslStoragePort;

	private final boolean startNativeTransport;

	private final boolean startRpc;

	@Nonnull
	private final Version version;

	@Nullable
	private final String clusterName;

	@Nullable
	private final Integer sslPort;

	@Nullable
	private final String rpcAddress;

	@Nullable
	private final String listenAddress;

	@Nullable
	private final String broadcastAddress;

	@Nullable
	private final String broadcastRpcAddress;

	@Nullable
	private final String listenInterface;

	@Nullable
	private final String rpcInterface;

	private final boolean rpcInterfacePreferIpv6;

	private final boolean listenInterfacePreferIpv6;

	private final boolean listenOnBroadcastAddress;

	/**
	 * Creates a new {@link MapSettings}.
	 *
	 * @param source a source {@code Map}
	 * @param version a version
	 */
	MapSettings(@Nullable Map<?, ?> source, @Nonnull Version version) {
		Map<?, ?> values = new LinkedHashMap<>((source != null) ? source : Collections.emptyMap());

		this.version = version;
		this.clusterName = get("cluster_name", values).orElse("Test Cluster");

		this.port = getInt("native_transport_port", values).orElse(9042);
		this.rpcPort = getInt("rpc_port", values).orElse(9160);
		this.storagePort = getInt("storage_port", values).orElse(7000);
		this.sslStoragePort = getInt("ssl_storage_port", values).orElse(7001);
		this.sslPort = getInt("native_transport_port_ssl", values).orElse(null);

		this.startNativeTransport = getBool("start_native_transport", values).orElse(version.getMajor() > 2);
		this.startRpc = getBool("start_rpc", values).orElse(version.getMajor() < 4);

		this.rpcAddress = get("rpc_address", values).orElse(null);
		this.rpcInterface = get("rpc_interface", values).orElse(null);
		this.rpcInterfacePreferIpv6 = getBool("rpc_interface_prefer_ipv6", values).orElse(false);
		this.broadcastRpcAddress = get("broadcast_rpc_address", values).orElse(null);

		this.listenInterface = get("listen_interface", values).orElse(null);
		this.listenAddress = get("listen_address", values).orElse(null);
		this.broadcastAddress = get("broadcast_address", values).orElse(null);
		this.listenInterfacePreferIpv6 = getBool("listen_interface_prefer_ipv6", values).orElse(false);
		this.listenOnBroadcastAddress = getBool("listen_on_broadcast_address", values).orElse(false);
	}

	@Nullable
	@Override
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

	@Nullable
	@Override
	public String getListenAddress() {
		return this.listenAddress;
	}

	@Nullable
	@Override
	public String getListenInterface() {
		return this.listenInterface;
	}

	@Nullable
	@Override
	public String getBroadcastAddress() {
		return this.broadcastAddress;
	}

	@Nullable
	@Override
	public String getRpcAddress() {
		return this.rpcAddress;
	}

	@Nullable
	@Override
	public String getRpcInterface() {
		return this.rpcInterface;
	}

	@Nullable
	@Override
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

	@Nonnull
	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public boolean isListenOnBroadcastAddress() {
		return this.listenOnBroadcastAddress;
	}

	@Override
	public boolean isListenInterfacePreferIpv6() {
		return this.listenInterfacePreferIpv6;
	}

	@Override
	public boolean isRpcInterfacePreferIpv6() {
		return this.rpcInterfacePreferIpv6;

	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		MapSettings that = (MapSettings) other;
		return this.port == that.port &&
				this.rpcPort == that.rpcPort &&
				this.storagePort == that.storagePort &&
				this.sslStoragePort == that.sslStoragePort &&
				this.startNativeTransport == that.startNativeTransport &&
				this.startRpc == that.startRpc &&
				this.rpcInterfacePreferIpv6 == that.rpcInterfacePreferIpv6 &&
				this.listenInterfacePreferIpv6 == that.listenInterfacePreferIpv6 &&
				this.listenOnBroadcastAddress == that.listenOnBroadcastAddress &&
				Objects.equals(this.clusterName, that.clusterName) &&
				Objects.equals(this.sslPort, that.sslPort) &&
				Objects.equals(this.rpcAddress, that.rpcAddress) &&
				Objects.equals(this.listenAddress, that.listenAddress) &&
				Objects.equals(this.broadcastAddress, that.broadcastAddress) &&
				Objects.equals(this.broadcastRpcAddress, that.broadcastRpcAddress) &&
				Objects.equals(this.listenInterface, that.listenInterface) &&
				Objects.equals(this.rpcInterface, that.rpcInterface) &&
				Objects.equals(this.version, that.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.port, this.rpcPort, this.storagePort, this.sslStoragePort, this.startNativeTransport,
				this.startRpc, this.clusterName, this.sslPort, this.rpcAddress, this.listenAddress,
				this.broadcastAddress, this.broadcastRpcAddress, this.listenInterface, this.rpcInterface, this.version,
				this.rpcInterfacePreferIpv6, this.listenInterfacePreferIpv6, this.listenOnBroadcastAddress);
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", MapSettings.class.getSimpleName() + "[", "]")
				.add("port=" + this.port)
				.add("rpcPort=" + this.rpcPort)
				.add("storagePort=" + this.storagePort)
				.add("sslStoragePort=" + this.sslStoragePort)
				.add("startNativeTransport=" + this.startNativeTransport)
				.add("startRpc=" + this.startRpc)
				.add("clusterName='" + this.clusterName + "'")
				.add("sslPort=" + this.sslPort)
				.add("rpcAddress='" + this.rpcAddress + "'")
				.add("listenAddress='" + this.listenAddress + "'")
				.add("broadcastAddress='" + this.broadcastAddress + "'")
				.add("broadcastRpcAddress='" + this.broadcastRpcAddress + "'")
				.add("listenInterface='" + this.listenInterface + "'")
				.add("rpcInterface='" + this.rpcInterface + "'")
				.add("version=" + this.version)
				.add("rpcInterfacePreferIpv6=" + this.rpcInterfacePreferIpv6)
				.add("listenInterfacePreferIpv6=" + this.listenInterfacePreferIpv6)
				.add("listenOnBroadcastAddress=" + this.listenOnBroadcastAddress)
				.toString();
	}

	private static Optional<Integer> getInt(String name, Map<?, ?> source) {
		return get(name, source).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<Boolean> getBool(String name, Map<?, ?> source) {
		return get(name, source).map(Boolean::valueOf);
	}

	private static Optional<String> get(String name, Map<?, ?> source) {
		Object val = source.get(name);
		return Optional.ofNullable(val).map(String::valueOf);
	}

}
