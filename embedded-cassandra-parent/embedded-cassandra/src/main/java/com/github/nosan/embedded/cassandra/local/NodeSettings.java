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
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Basic implementation of the {@link Settings}.
 *
 * @author Dmytro Nosan
 * @since 1.2.10
 */
class NodeSettings implements Settings {

	private final Version version;

	private final Map<Object, Object> properties;

	/**
	 * Creates a new {@link NodeSettings}.
	 *
	 * @param version a version
	 * @param properties a node properties
	 */
	NodeSettings(Version version, @Nullable Map<?, ?> properties) {
		this.version = version;
		this.properties = Collections.unmodifiableMap(
				(properties != null) ? new LinkedHashMap<>(properties) : Collections.emptyMap());
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public Map<Object, Object> getProperties() {
		return this.properties;
	}

	@Nullable
	@Override
	public String getClusterName() {
		return getString("cluster_name", this.properties).orElse(Settings.super.getClusterName());
	}

	@Override
	public int getStoragePort() {
		return getInteger("storage_port", this.properties).orElse(Settings.super.getStoragePort());
	}

	@Override
	public int getSslStoragePort() {
		return getInteger("ssl_storage_port", this.properties).orElse(Settings.super.getSslStoragePort());
	}

	@Nullable
	@Override
	public String getListenAddress() {
		return getString("listen_address", this.properties).orElse(Settings.super.getListenAddress());
	}

	@Nullable
	@Override
	public String getListenInterface() {
		return getString("listen_interface", this.properties).orElse(Settings.super.getListenInterface());
	}

	@Nullable
	@Override
	public String getBroadcastAddress() {
		return getString("broadcast_address", this.properties).orElse(Settings.super.getBroadcastAddress());
	}

	@Nullable
	@Override
	public String getRpcAddress() {
		return getString("rpc_address", this.properties).orElse(Settings.super.getRpcAddress());
	}

	@Nullable
	@Override
	public String getRpcInterface() {
		return getString("rpc_interface", this.properties).orElse(Settings.super.getRpcInterface());
	}

	@Nullable
	@Override
	public String getBroadcastRpcAddress() {
		return getString("broadcast_rpc_address", this.properties).orElse(Settings.super.getBroadcastRpcAddress());
	}

	@Override
	public boolean isStartNativeTransport() {
		return getBoolean("start_native_transport", this.properties).orElse(Settings.super.isStartNativeTransport());
	}

	@Override
	public int getPort() {
		return getInteger("native_transport_port", this.properties).orElse(Settings.super.getPort());
	}

	@Nullable
	@Override
	public Integer getSslPort() {
		return getInteger("native_transport_port_ssl", this.properties).orElse(Settings.super.getSslPort());
	}

	@Override
	public boolean isStartRpc() {
		return getBoolean("start_rpc", this.properties).orElse(Settings.super.isStartRpc());
	}

	@Override
	public int getRpcPort() {
		return getInteger("rpc_port", this.properties).orElse(Settings.super.getRpcPort());
	}

	@Override
	public boolean isListenOnBroadcastAddress() {
		return getBoolean("listen_on_broadcast_address", this.properties)
				.orElse(Settings.super.isListenOnBroadcastAddress());
	}

	@Override
	public boolean isListenInterfacePreferIpv6() {
		return getBoolean("listen_interface_prefer_ipv6", this.properties)
				.orElse(Settings.super.isListenInterfacePreferIpv6());
	}

	@Override
	public boolean isRpcInterfacePreferIpv6() {
		return getBoolean("rpc_interface_prefer_ipv6", this.properties)
				.orElse(Settings.super.isRpcInterfacePreferIpv6());
	}

	@Override
	public String toString() {
		Map<Object, Object> properties = new LinkedHashMap<>(getProperties());
		properties.put("version", getVersion());
		properties.put("real_listen_address", getRealListenAddress());
		properties.put("real_address", getRealAddress());
		properties.put("cluster_name", getClusterName());
		properties.put("storage_port", getStoragePort());
		properties.put("ssl_storage_port", getSslStoragePort());
		properties.put("listen_address", getListenAddress());
		properties.put("listen_interface", getListenInterface());
		properties.put("broadcast_address", getBroadcastAddress());
		properties.put("rpc_address", getRpcAddress());
		properties.put("rpc_interface", getRpcInterface());
		properties.put("broadcast_rpc_address", getBroadcastRpcAddress());
		properties.put("start_native_transport", isStartNativeTransport());
		properties.put("native_transport_port", getPort());
		properties.put("rpc_port", getRpcPort());
		properties.put("native_transport_port_ssl", getSslPort());
		properties.put("start_rpc", isStartRpc());
		properties.put("listen_on_broadcast_address", isListenOnBroadcastAddress());
		properties.put("listen_interface_prefer_ipv6", isListenInterfacePreferIpv6());
		properties.put("rpc_interface_prefer_ipv6", isRpcInterfacePreferIpv6());
		return properties.entrySet().stream().filter(entry -> entry.getValue() != null)
				.map(String::valueOf).collect(Collectors.joining(",", "{", "}"));
	}

	private static Optional<Integer> getInteger(String name, Map<?, ?> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<Boolean> getBoolean(String name, Map<?, ?> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Boolean::parseBoolean);
	}

	private static Optional<String> getString(String name, Map<?, ?> source) {
		return Optional.ofNullable(source.get(name)).map(String::valueOf);
	}

}
