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

import java.net.InetAddress;
import java.util.Optional;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Default implementation of the {@link Settings}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class NodeSettings implements Settings {

	private final Version version;

	@Nullable
	private volatile InetAddress address;

	@Nullable
	private volatile Integer port;

	@Nullable
	private volatile Integer sslPort;

	@Nullable
	private volatile Integer rpcPort;

	@Nullable
	private volatile Boolean rpcTransportEnabled;

	@Nullable
	private volatile Boolean transportEnabled;

	NodeSettings(Version version) {
		this.version = version;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public boolean isTransportEnabled() {
		return getTransportEnabled().orElse(false);
	}

	@Override
	public boolean isRpcTransportEnabled() {
		return getRpcTransportEnabled().orElse(false);
	}

	@Override
	public Optional<InetAddress> getAddress() {
		return Optional.ofNullable(this.address);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getAddress} attribute.
	 *
	 * @param address The value for address
	 */
	void setAddress(@Nullable InetAddress address) {
		this.address = address;
	}

	@Override
	public Optional<Integer> getPort() {
		return Optional.ofNullable(this.port);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getPort} attribute.
	 *
	 * @param port The value for port
	 */
	void setPort(@Nullable Integer port) {
		this.port = port;
	}

	@Override
	public Optional<Integer> getSslPort() {
		return Optional.ofNullable(this.sslPort);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getSslPort} attribute.
	 *
	 * @param sslPort The value for sslPort
	 */
	void setSslPort(@Nullable Integer sslPort) {
		this.sslPort = sslPort;
	}

	@Override
	public Optional<Integer> getRpcPort() {
		return Optional.ofNullable(this.rpcPort);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getRpcPort} attribute.
	 *
	 * @param rpcPort The value for rpcPort
	 */
	void setRpcPort(@Nullable Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", getClass().getSimpleName() + " [", "]")
				.add("address=" + this.address)
				.add("port=" + this.port)
				.add("sslPort=" + this.sslPort)
				.add("rpcPort=" + this.rpcPort)
				.add("rpcTransportEnabled=" + this.rpcTransportEnabled)
				.add("transportEnabled=" + this.transportEnabled)
				.add("version=" + this.version)
				.toString();
	}

	/**
	 * RPC transport is started or not.
	 *
	 * @return rpc transport is enabled, or {@code empty} if not present.
	 */
	Optional<Boolean> getRpcTransportEnabled() {
		return Optional.ofNullable(this.rpcTransportEnabled);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#isRpcTransportEnabled()} attribute.
	 *
	 * @param rpcTransportEnabled The value for rpcTransportEnabled
	 */
	void setRpcTransportEnabled(@Nullable Boolean rpcTransportEnabled) {
		this.rpcTransportEnabled = rpcTransportEnabled;
	}

	/**
	 * Native transport is started or not.
	 *
	 * @return native transport is enabled, or {@code empty} if not present.
	 */
	Optional<Boolean> getTransportEnabled() {
		return Optional.ofNullable(this.transportEnabled);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#isTransportEnabled()} attribute.
	 *
	 * @param transportEnabled The value for transportEnabled
	 */
	void setTransportEnabled(@Nullable Boolean transportEnabled) {
		this.transportEnabled = transportEnabled;
	}

}
