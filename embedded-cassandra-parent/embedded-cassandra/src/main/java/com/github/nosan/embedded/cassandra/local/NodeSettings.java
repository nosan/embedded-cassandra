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

	private final Version version;

	NodeSettings(Version version) {
		this.version = version;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public InetAddress getAddress() throws IllegalStateException {
		if (!isRpcTransportEnabled() && !isTransportEnabled()) {
			throw new IllegalStateException("RPC and Native Transport are not enabled");
		}
		InetAddress address = this.address;
		if (address == null) {
			throw new IllegalStateException("RPC or Native transport is enabled, but Address is null");
		}
		return address;
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
	public boolean isTransportEnabled() {
		Boolean transportEnabled = this.transportEnabled;
		return transportEnabled != null && transportEnabled;
	}

	/**
	 * Initializes the value for the {@link NodeSettings#isTransportEnabled()} attribute.
	 *
	 * @param transportEnabled The value for transportEnabled
	 */
	void setTransportEnabled(@Nullable Boolean transportEnabled) {
		this.transportEnabled = transportEnabled;
	}

	@Override
	public int getPort() throws IllegalStateException {
		if (!isTransportEnabled()) {
			throw new IllegalStateException("Native Transport is not enabled");
		}
		Integer port = this.port;
		if (port == null) {
			throw new IllegalStateException("Native transport is enabled, but port is null");
		}
		return port;
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
	public Optional<Integer> getSslPort() throws IllegalStateException {
		if (!isTransportEnabled()) {
			throw new IllegalStateException("Native transport is not enabled");
		}
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
	public boolean isRpcTransportEnabled() {
		Boolean rpcTransportEnabled = this.rpcTransportEnabled;
		return rpcTransportEnabled != null && rpcTransportEnabled;
	}

	/**
	 * Initializes the value for the {@link NodeSettings#isRpcTransportEnabled()} attribute.
	 *
	 * @param rpcTransportEnabled The value for rpcTransportEnabled
	 */
	void setRpcTransportEnabled(@Nullable Boolean rpcTransportEnabled) {
		this.rpcTransportEnabled = rpcTransportEnabled;
	}

	@Override
	public int getRpcPort() throws IllegalStateException {
		if (!isRpcTransportEnabled()) {
			throw new IllegalStateException("RPC transport is not enabled");
		}
		Integer rpcPort = this.rpcPort;
		if (rpcPort == null) {
			throw new IllegalStateException("RPC transport is enabled, but RPC port is null");
		}
		return rpcPort;
	}

	/**
	 * Initializes the value for the {@link NodeSettings#getRpcPort} attribute.
	 *
	 * @param rpcPort The value for rpcPort
	 */
	void setRpcPort(@Nullable Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

}
