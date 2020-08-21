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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;

/**
 * This class used to parse Cassandra's output to find a {@code port}, {@code ssl_port}, and {@code rpc_address}
 * properties and decides whether {@code native transport} is ready or not.
 *
 * @author Dmytro Nosan
 */
class NativeTransportReadinessConsumer implements ReadinessConsumer {

	private static final Logger log = LoggerFactory.getLogger(NativeTransportReadinessConsumer.class);

	private static final Pattern TRANSPORT_START_PATTERN = Pattern.compile(
			"(?i).*listening\\s*for\\s*cql\\s*clients\\s*on.*/(.+):(\\d+).*");

	private static final Pattern TRANSPORT_NOT_START_PATTERN = Pattern.compile(
			"(?i).*((not\\s*starting\\s*client\\s*transports)|(not\\s*starting\\s*native\\s*transport)).*");

	private static final String ENCRYPTED = "(encrypted)";

	private final Version version;

	private final boolean ssl;

	@Nullable
	private volatile Integer port;

	@Nullable
	private volatile Integer sslPort;

	@Nullable
	private volatile InetAddress address;

	private volatile boolean disabled;

	NativeTransportReadinessConsumer(Version version, boolean ssl) {
		this.version = version;
		this.ssl = ssl;
	}

	@Override
	public void accept(String line) {
		Matcher matcher = TRANSPORT_START_PATTERN.matcher(line);
		if (matcher.matches()) {
			this.address = getAddress(matcher.group(1));
			boolean ssl = line.toLowerCase(Locale.ENGLISH).contains(ENCRYPTED);
			if (ssl) {
				this.sslPort = Integer.parseInt(matcher.group(2));
			}
			else {
				this.port = Integer.parseInt(matcher.group(2));
			}
		}
		else if (TRANSPORT_NOT_START_PATTERN.matcher(line).matches()) {
			this.disabled = true;
		}
	}

	@Override
	public boolean isReady() {
		if (this.version.getMajor() < 2) {
			return true;
		}
		if (this.disabled) {
			return true;
		}
		if (this.ssl && this.sslPort == null) {
			return false;
		}
		return this.port != null;
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
	 * Returns the native transport port ({@code native_transport_port}) this {@code Cassandra} is listening on.
	 *
	 * @return the port
	 */
	int getPort() {
		Integer port = this.port;
		return (port != null) ? port : -1;
	}

	/**
	 * Returns the native transport SSL port ({@code native_transport_port_ssl}) this {@code Cassandra} is listening
	 * on.
	 *
	 * @return the SSL port (or -1 if none)
	 */
	int getSslPort() {
		Integer port = this.sslPort;
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
