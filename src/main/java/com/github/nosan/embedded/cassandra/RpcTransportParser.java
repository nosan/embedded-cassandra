/*
 * Copyright 2020-2021 the original author or authors.
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

class RpcTransportParser implements Consumer<String>, Closeable {

	private static final Pattern TRANSPORTS_NOT_START = Pattern
			.compile("Not starting client transports", Pattern.CASE_INSENSITIVE);

	private static final Pattern TRANSPORT_NOT_START = Pattern
			.compile("Not starting RPC server as requested", Pattern.CASE_INSENSITIVE);

	private static final Pattern TRANSPORT_START_PATTERN = Pattern.compile(
			"Binding thrift service to .*/(.+):(\\d+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern START_LISTENING_PATTERN = Pattern
			.compile("Listening for thrift clients", Pattern.CASE_INSENSITIVE);

	private static final Pattern FAILED_TO_BIND = Pattern
			.compile("Unable to create thrift socket to", Pattern.CASE_INSENSITIVE);

	private final CassandraDatabase database;

	private volatile Integer port;

	private volatile InetAddress address;

	private volatile boolean disabled;

	private volatile boolean failed;

	private volatile boolean listening;

	RpcTransportParser(CassandraDatabase database) {
		this.database = database;
		this.disabled = database.getVersion().getMajor() >= 4;
		database.getStdOut().attach(this);
		database.getStdErr().attach(this);
	}

	@Override
	public void accept(String line) {
		Matcher matcher;
		if ((matcher = TRANSPORT_START_PATTERN.matcher(line)).find()) {
			this.address = getAddress(matcher.group(1));
			this.port = Integer.parseInt(matcher.group(2));
		}
		else if (TRANSPORT_NOT_START.matcher(line).find()) {
			this.disabled = true;
		}
		else if (TRANSPORTS_NOT_START.matcher(line).find()) {
			this.disabled = true;
		}
		else if (START_LISTENING_PATTERN.matcher(line).find()) {
			this.listening = true;
		}
		else if (FAILED_TO_BIND.matcher(line).find()) {
			this.failed = true;
		}
	}

	@Override
	public void close() {
		this.database.getStdOut().detach(this);
		this.database.getStdErr().detach(this);
	}

	boolean isFailed() {
		return this.failed;
	}

	boolean isParsed() {
		if (this.disabled) {
			return true;
		}
		return isStarted();
	}

	boolean isStarted() {
		if (this.address == null) {
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
			throw new IllegalStateException(String.format("Could not parse an address: '%s'", address), ex);
		}
	}

}
