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
	private volatile InetAddress rpcAddress;

	@Nullable
	private volatile Integer port;

	@Nullable
	private volatile Integer sslPort;

	@Nullable
	private volatile Integer rpcPort;

	@Nullable
	private volatile Boolean rpcTransportStarted;

	@Nullable
	private volatile Boolean transportStarted;

	NodeSettings(Version version) {
		this.version = version;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public Optional<InetAddress> getAddress() {
		InetAddress address = this.address;
		if (address != null) {
			return Optional.of(address);
		}
		return Optional.ofNullable(this.rpcAddress);
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
		return new StringJoiner(", ", NodeSettings.class.getSimpleName() + " [", "]")
				.add("version=" + getVersion())
				.add("address=" + getAddress().orElse(null))
				.add("port=" + getPort().orElse(null))
				.add("sslPort=" + getSslPort().orElse(null))
				.add("rpcPort=" + getRpcPort().orElse(null))
				.toString();
	}

	/**
	 * RPC transport is started or not.
	 *
	 * @return rpc transport is enabled, or {@code empty} if not present.
	 */
	Optional<Boolean> getRpcTransportStarted() {
		return Optional.ofNullable(this.rpcTransportStarted);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getRpcTransportStarted()} attribute.
	 *
	 * @param rpcTransportStarted The value for rpcTransportEnabled
	 */
	void setRpcTransportStarted(@Nullable Boolean rpcTransportStarted) {
		this.rpcTransportStarted = rpcTransportStarted;
	}

	/**
	 * Native transport is started or not.
	 *
	 * @return native transport is enabled, or {@code empty} if not present.
	 */
	Optional<Boolean> getTransportStarted() {
		return Optional.ofNullable(this.transportStarted);
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getTransportStarted()} attribute.
	 *
	 * @param transportStarted The value for transportEnabled
	 */
	void setTransportStarted(@Nullable Boolean transportStarted) {
		this.transportStarted = transportStarted;
	}

	/**
	 * Initializes the value for the rpc address attribute.
	 *
	 * @param rpcAddress The value for rpcAddress
	 */
	void setRpcAddress(@Nullable InetAddress rpcAddress) {
		this.rpcAddress = rpcAddress;
	}

}
