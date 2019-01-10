/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Utility methods for dealing with ports.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.INTERNAL)
public abstract class PortUtils {

	/**
	 * The default minimum value for port ranges used when finding an available
	 * socket port.
	 */
	private static final int MIN = 1024;

	/**
	 * The default maximum value for port ranges used when finding an available
	 * socket port.
	 */
	private static final int MAX = 65535;

	/**
	 * Find a free {@code TCP} port.
	 *
	 * @return a port
	 * @throws IllegalStateException if port could not be found
	 */
	public static int getPort() {
		return getPort(NetworkUtils.getLocalhost());
	}

	/**
	 * Find a free {@code TCP} port.
	 *
	 * @param address the address
	 * @return a port
	 * @throws IllegalStateException if port could not be found
	 * @since 1.1.0
	 */
	public static int getPort(@Nullable InetAddress address) {
		for (int i = 0; i <= MAX - MIN; i++) {
			int port = MIN + ThreadLocalRandom.current().nextInt(MAX - MIN + 1);
			if (isPortAvailable(address, port)) {
				return port;
			}
		}
		throw new IllegalStateException(String.format("Could not find an available port in the range [%d, %d]",
				MIN, MAX));
	}

	/**
	 * Test whether the {@code TCP} port is busy or not.
	 *
	 * @param address the address
	 * @param port the TCP port
	 * @return {@code true} if port is busy, otherwise {@code false}
	 */
	public static boolean isPortBusy(@Nullable InetAddress address, int port) {
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(address, port), 10);
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	private static boolean isPortAvailable(InetAddress address, int port) {
		try (ServerSocket ss = new ServerSocket()) {
			ss.bind(new InetSocketAddress(address, port), 1);
		}
		catch (IOException ex) {
			return false;
		}
		return !isPortBusy(address, port);
	}

}
