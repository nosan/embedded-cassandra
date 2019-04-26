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
import java.net.UnknownHostException;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility methods for dealing with a network.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class NetworkUtils {

	private static final String LOCALHOST = "localhost";

	/**
	 * Test whether the {@code TCP} port is busy or not.
	 *
	 * @param address the address
	 * @param port the TCP port
	 * @return {@code true} if port is busy, otherwise {@code false}
	 */
	static boolean isListen(@Nullable InetAddress address, int port) {
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(address, port), 1000);
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	static InetAddress getLocalhost() {
		try {
			return InetAddress.getByName(LOCALHOST);
		}
		catch (UnknownHostException ex) {
			return InetAddress.getLoopbackAddress();
		}
	}

}
