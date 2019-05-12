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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility methods for dealing with sockets.
 *
 * @author Dmytro Nosan
 * @since 2.0.1
 */
abstract class SocketUtils {

	private static final String LOCALHOST = "localhost";

	/**
	 * Test whether is is possible to connect to the given address and port.
	 *
	 * @param address the address
	 * @param port the TCP port
	 * @return {@code true} if it possible to connect otherwise {@code false}
	 */
	static boolean connect(@Nullable InetAddress address, int port) {
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(address, port), 1000);
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	/**
	 * Determines the IP address of a localhost.
	 *
	 * @return the localhost
	 */
	static InetAddress getLocalhost() {
		try {
			return getAddress(LOCALHOST);
		}
		catch (IllegalArgumentException ex) {
			return InetAddress.getLoopbackAddress();
		}
	}

	/**
	 * Determines the IP address of a host, given the host's name.
	 *
	 * @param host the specified host
	 * @return an IP address for the given host name
	 */
	static InetAddress getAddress(@Nullable String host) {
		try {
			return InetAddress.getByName(host);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(String.format("Can not parse an address '%s'", host), ex);
		}
	}

	/**
	 * Parses the port.
	 *
	 * @param port the specified port
	 * @return a port
	 */
	static int getPort(String port) {
		try {
			return Integer.parseInt(port);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(String.format("Can not parse a port '%s'", port), ex);
		}
	}

}
