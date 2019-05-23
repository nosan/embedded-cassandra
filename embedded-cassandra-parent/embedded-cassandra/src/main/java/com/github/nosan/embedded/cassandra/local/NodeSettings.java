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
 * The node {@link Settings}.
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
	public Optional<InetAddress> address() {
		Boolean transportStarted = this.transportStarted;
		Boolean rpcTransportStarted = this.rpcTransportStarted;
		if ((transportStarted == null || !transportStarted) && (rpcTransportStarted == null || !rpcTransportStarted)) {
			return Optional.empty();
		}
		InetAddress address = Optional.ofNullable(this.address)
				.orElse(this.rpcAddress);
		return Optional.ofNullable(address);
	}

	@Override
	public Optional<Integer> port() {
		Boolean transportStarted = this.transportStarted;
		if (transportStarted == null || !transportStarted) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.port);

	}

	@Override
	public Optional<Integer> sslPort() {
		Boolean transportStarted = this.transportStarted;
		if (transportStarted == null || !transportStarted) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.sslPort);
	}

	@Override
	public Optional<Integer> rpcPort() {
		Boolean rpcTransportStarted = this.rpcTransportStarted;
		if (rpcTransportStarted == null || !rpcTransportStarted) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.rpcPort);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", "[", "]")
				.add("version=" + this.version)
				.add("address=" + Optional.ofNullable(this.address).orElse(this.rpcAddress))
				.add("port=" + this.port)
				.add("sslPort=" + this.sslPort)
				.add("rpcPort=" + this.rpcPort)
				.add("rpcTransportStarted=" + this.rpcTransportStarted)
				.add("transportStarted=" + this.transportStarted)
				.toString();
	}

	void setAddress(@Nullable InetAddress address) {
		this.address = address;
	}

	void setRpcAddress(@Nullable InetAddress rpcAddress) {
		this.rpcAddress = rpcAddress;
	}

	void setPort(@Nullable Integer port) {
		this.port = port;
	}

	void setSslPort(@Nullable Integer sslPort) {
		this.sslPort = sslPort;
	}

	void setRpcPort(@Nullable Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	Optional<Boolean> rpcTransportStarted() {
		Boolean rpcTransportStarted = this.rpcTransportStarted;
		Version version = this.version;
		if (rpcTransportStarted == null && version.getMajor() > 3) {
			return Optional.of(false);
		}
		return Optional.ofNullable(rpcTransportStarted);
	}

	void setRpcTransportStarted(@Nullable Boolean rpcTransportStarted) {
		this.rpcTransportStarted = rpcTransportStarted;
	}

	Optional<Boolean> transportStarted() {
		Version version = this.version;
		Boolean transportStarted = this.transportStarted;
		if (transportStarted == null && version.getMajor() < 2) {
			return Optional.of(false);
		}
		return Optional.ofNullable(transportStarted);
	}

	void setTransportStarted(@Nullable Boolean transportStarted) {
		this.transportStarted = transportStarted;
	}

}
