/*
 * Copyright 2020 the original author or authors.
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

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.nosan.embedded.cassandra.commons.logging.Logger;

class RpcTransportReadiness implements Consumer<String>, Closeable {

	private static final Logger LOGGER = Logger.get(TransportReadiness.class);

	private static final Pattern RPC_TRANSPORT_BIND_PATTERN = Pattern.compile(
			"(?i).*binding\\s*thrift\\s*service\\s*to.*/(.+):(\\d+).*");

	private static final Pattern RPC_START_PATTERN = Pattern
			.compile("(?i).*listening\\s*for\\s*thrift\\s*clients.*");

	private static final Pattern RPC_TRANSPORT_NOT_START_PATTERN = Pattern.compile(
			"(?i).*not\\s*starting\\s*rpc\\s*server.*");

	private static final Pattern BIND_FAILED = Pattern.compile("(?i).*unable\\s*to\\s*create\\s*thrift\\s*socket.*");

	private final CassandraDatabase database;

	private volatile Integer port;

	private volatile InetAddress address;

	private volatile boolean disabled;

	private volatile boolean failed;

	private volatile boolean listening;

	RpcTransportReadiness(CassandraDatabase database) {
		this.database = database;
		this.disabled = database.getVersion().getMajor() >= 4;
		database.getStdOut().attach(this);
	}

	@Override
	public void accept(String line) {
		Matcher matcher = RPC_TRANSPORT_BIND_PATTERN.matcher(line);
		if (matcher.matches()) {
			this.address = getAddress(matcher.group(1));
			this.port = Integer.parseInt(matcher.group(2));
		}
		else if (RPC_TRANSPORT_NOT_START_PATTERN.matcher(line).matches()) {
			this.disabled = true;
		}
		else if (RPC_START_PATTERN.matcher(line).matches()) {
			this.listening = true;
		}
		else if (BIND_FAILED.matcher(line).matches()) {
			this.failed = true;
		}
	}

	@Override
	public void close() {
		this.database.getStdOut().detach(this);
	}

	boolean isFailed() {
		return this.failed;
	}

	boolean isDisabled() {
		return this.disabled;
	}

	boolean isStartedOrDisabled() {
		if (this.disabled) {
			return true;
		}
		return isStarted();
	}

	boolean isStarted() {
		if (this.address == null) {
			return false;
		}
		if (this.port == null) {
			return false;
		}
		return this.listening;
	}

	InetAddress getAddress() {
		return this.address;
	}

	Integer getPort() {
		return this.port;
	}

	private static InetAddress getAddress(String address) {
		try {
			return InetAddress.getByName(address);
		}
		catch (UnknownHostException ex) {
			LOGGER.error(ex, "Could not parse an address: ''{0}''", address);
			return null;
		}
	}

}
