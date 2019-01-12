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
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Basic implementation of the {@link Settings}.
 *
 * @author Dmytro Nosan
 * @since 1.2.10
 */
class NodeSettings implements Settings {

	@Nonnull
	private final Version version;

	@Nonnull
	private final Map<?, ?> properties;

	/**
	 * Creates a new {@link NodeSettings}.
	 *
	 * @param version a version
	 * @param properties a node properties
	 */
	NodeSettings(@Nonnull Version version, @Nullable Map<?, ?> properties) {
		this.version = version;
		this.properties = Collections.unmodifiableMap(
				(properties != null) ? new LinkedHashMap<>(properties) : Collections.emptyMap());
	}

	@Nonnull
	@Override
	public Version getVersion() {
		return this.version;
	}

	@Nullable
	@Override
	public String getClusterName() {
		return get("cluster_name", this.properties).orElse(Settings.super.getClusterName());
	}

	@Override
	public int getStoragePort() {
		return getInt("storage_port", this.properties).orElse(Settings.super.getStoragePort());
	}

	@Override
	public int getSslStoragePort() {
		return getInt("ssl_storage_port", this.properties).orElse(Settings.super.getSslStoragePort());
	}

	@Nullable
	@Override
	public String getListenAddress() {
		return get("listen_address", this.properties).orElse(Settings.super.getListenAddress());
	}

	@Nullable
	@Override
	public String getListenInterface() {
		return get("listen_interface", this.properties).orElse(Settings.super.getListenInterface());
	}

	@Nullable
	@Override
	public String getBroadcastAddress() {
		return get("broadcast_address", this.properties).orElse(Settings.super.getBroadcastAddress());
	}

	@Nullable
	@Override
	public String getRpcAddress() {
		return get("rpc_address", this.properties).orElse(Settings.super.getRpcAddress());
	}

	@Nullable
	@Override
	public String getRpcInterface() {
		return get("rpc_interface", this.properties).orElse(Settings.super.getRpcInterface());
	}

	@Nullable
	@Override
	public String getBroadcastRpcAddress() {
		return get("broadcast_rpc_address", this.properties).orElse(Settings.super.getBroadcastRpcAddress());
	}

	@Override
	public boolean isStartNativeTransport() {
		return getBool("start_native_transport", this.properties).orElse(Settings.super.isStartNativeTransport());
	}

	@Override
	public int getPort() {
		return getInt("native_transport_port", this.properties).orElse(Settings.super.getPort());
	}

	@Nullable
	@Override
	public Integer getSslPort() {
		return getInt("native_transport_port_ssl", this.properties).orElse(Settings.super.getSslPort());
	}

	@Override
	public boolean isStartRpc() {
		return getBool("start_rpc", this.properties).orElse(Settings.super.isStartRpc());
	}

	@Override
	public int getRpcPort() {
		return getInt("rpc_port", this.properties).orElse(Settings.super.getRpcPort());
	}

	@Override
	public boolean isListenOnBroadcastAddress() {
		return getBool("listen_on_broadcast_address", this.properties)
				.orElse(Settings.super.isListenOnBroadcastAddress());
	}

	@Override
	public boolean isListenInterfacePreferIpv6() {
		return getBool("listen_interface_prefer_ipv6", this.properties)
				.orElse(Settings.super.isListenInterfacePreferIpv6());
	}

	@Override
	public boolean isRpcInterfacePreferIpv6() {
		return getBool("rpc_interface_prefer_ipv6", this.properties)
				.orElse(Settings.super.isRpcInterfacePreferIpv6());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", "[", "]")
				.add("clusterName=" + getClusterName())
				.add("storagePort=" + getStoragePort())
				.add("sslStoragePort=" + getSslStoragePort())
				.add("listenAddress=" + getListenAddress())
				.add("listenInterface=" + getListenInterface())
				.add("broadcastAddress=" + getBroadcastAddress())
				.add("rpcAddress=" + getRpcAddress())
				.add("rpcInterface=" + getRpcInterface())
				.add("broadcastRpcAddress=" + getBroadcastRpcAddress())
				.add("startNativeTransport=" + isStartNativeTransport())
				.add("port=" + getPort())
				.add("sslPort=" + getSslPort())
				.add("startRpc=" + isStartRpc())
				.add("rpcPort=" + getRpcPort())
				.add("listenOnBroadcastAddress=" + isListenOnBroadcastAddress())
				.add("listenInterfacePreferIpv6=" + isListenInterfacePreferIpv6())
				.add("rpcInterfacePreferIpv6=" + isRpcInterfacePreferIpv6())
				.add("realAddress=" + getRealAddress())
				.add("realListenAddress=" + getRealListenAddress())
				.toString();
	}

	private static Optional<Integer> getInt(String name, Map<?, ?> source) {
		return get(name, source).filter(StringUtils::hasText).map(asInt());
	}

	private static Optional<Boolean> getBool(String name, Map<?, ?> source) {
		return get(name, source).filter(StringUtils::hasText).map(Boolean::valueOf);
	}

	private static Optional<String> get(String name, Map<?, ?> source) {
		Object val = source.get(name);
		return Optional.ofNullable(val).map(String::valueOf);
	}

	private static Function<String, Integer> asInt() {
		return source -> {
			try {
				return Integer.decode(source);
			}
			catch (NumberFormatException ex) {
				return null;
			}
		};
	}
}
