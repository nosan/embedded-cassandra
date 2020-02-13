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
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.nosan.embedded.cassandra.commons.logging.Logger;

class TransportReadiness implements Consumer<String>, Closeable {

	private static final Logger LOGGER = Logger.get(TransportReadiness.class);

	private static final Pattern TRANSPORT_START_PATTERN = Pattern.compile(
			"(?i).*listening\\s*for\\s*cql\\s*clients\\s*on.*/(.+):(\\d+).*");

	private static final Pattern TRANSPORT_NOT_START_PATTERN = Pattern.compile(
			"(?i).*((not\\s*starting\\s*client\\s*transports)|(not\\s*starting\\s*native\\s*transport)).*");

	private static final Pattern BIND_FAILED = Pattern.compile("(?i).*failed\\s*to\\s*bind\\s*port.*");

	private static final String ENCRYPTED = "(encrypted)";

	private final CassandraDatabase database;

	private volatile InetAddress address;

	private volatile Integer sslPort;

	private volatile Integer port;

	private volatile boolean disabled;

	private volatile boolean failed;

	TransportReadiness(CassandraDatabase database) {
		this.database = database;
		this.disabled = database.getVersion().getMajor() < 2;
		database.getStdOut().attach(this);
	}

	@Override
	public void accept(String line) {
		Matcher matcher = TRANSPORT_START_PATTERN.matcher(line);
		if (matcher.matches()) {
			this.address = getAddress(matcher.group(1));
			boolean encrypted = line.toLowerCase(Locale.ENGLISH).contains(ENCRYPTED);
			if (encrypted && getSslPort(this.database) != null) {
				this.sslPort = Integer.parseInt(matcher.group(2));
			}
			else {
				this.port = Integer.parseInt(matcher.group(2));
			}
		}
		else if (TRANSPORT_NOT_START_PATTERN.matcher(line).matches()) {
			this.disabled = true;
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
		if (getSslPort(this.database) != null && this.sslPort == null) {
			return false;
		}
		return this.port != null;
	}

	InetAddress getAddress() {
		return this.address;
	}

	Integer getPort() {
		return this.port;
	}

	Integer getSslPort() {
		return this.sslPort;
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

	private static Integer getSslPort(CassandraDatabase database) {
		Object value = database.getConfigProperties().get("native_transport_port_ssl");
		return (value != null) ? Integer.parseInt(value.toString()) : null;
	}

}
