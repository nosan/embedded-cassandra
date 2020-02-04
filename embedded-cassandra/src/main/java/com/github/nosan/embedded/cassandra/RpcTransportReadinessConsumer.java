/*
 * Copyright 2018-2020 the original author or authors.
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
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;

/**
 * This class used to parse Cassandra's output to find a {@code rpc_port}, {@code rpc_address} and decides whether
 * {@code rpc transport} is ready or not.
 *
 * @author Dmytro Nosan
 */
class RpcTransportReadinessConsumer implements ReadinessConsumer {

	private static final Logger log = LoggerFactory.getLogger(RpcTransportReadinessConsumer.class);

	private static final Pattern RPC_TRANSPORT_START_PATTERN = Pattern.compile(
			"(?i).*binding\\s*thrift\\s*service\\s*to.*/(.+):(\\d+).*");

	private static final Pattern RPC_TRANSPORT_NOT_START_PATTERN = Pattern.compile(
			"(?i).*not\\s*starting\\s*rpc\\s*server.*");

	private final Version version;

	@Nullable
	private volatile Integer rpcPort;

	@Nullable
	private volatile InetAddress address;

	@Nullable
	private volatile Boolean started;

	RpcTransportReadinessConsumer(Version version) {
		this.version = version;
	}

	@Override
	public void accept(String line) {
		Matcher matcher = RPC_TRANSPORT_START_PATTERN.matcher(line);
		if (matcher.matches()) {
			this.address = getAddress(matcher.group(1));
			this.rpcPort = Integer.parseInt(matcher.group(2));
			this.started = true;
		}
		else if (RPC_TRANSPORT_NOT_START_PATTERN.matcher(line).matches()) {
			this.started = false;
		}
	}

	@Override
	public boolean isReady() {
		return this.version.getMajor() >= 4 || this.started != null;
	}

	/**
	 * Returns the native transport address ({@code rpc_address}) this {@code Cassandra} is listening on.
	 *
	 * @return the address (or null if none)
	 */
	@Nullable
	InetAddress getAddress() {
		return this.address;
	}

	/**
	 * Returns the RPC transport port ({@code rpc_port}) this {@code Cassandra} is listening on.
	 *
	 * @return the RPC port (or -1 if none)
	 */
	int getRpcPort() {
		Integer port = this.rpcPort;
		return (port != null) ? port : -1;
	}

	@Nullable
	private static InetAddress getAddress(String address) {
		try {
			return InetAddress.getByName(address);
		}
		catch (UnknownHostException ex) {
			log.error(String.format("Address '%s' cannot be parsed", address), ex);
			return null;
		}
	}

}
