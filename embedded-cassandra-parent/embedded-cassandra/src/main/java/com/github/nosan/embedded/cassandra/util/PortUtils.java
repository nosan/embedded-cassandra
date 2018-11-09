/*
 * Copyright 2018-2018 the original author or authors.
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
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility methods for dealing with ports.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class PortUtils {

	private static final int MIN = 49152;

	private static final int MAX = 65535;

	private static final int RANGE = (MAX - MIN) + 1;

	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private static final InetAddress LOCALHOST = getLocalhost();


	/**
	 * Find a free {@code TCP} port.
	 *
	 * @return a port
	 * @throws IllegalStateException if port could not be found
	 */
	public static int getPort() {
		for (int i = 0; i < RANGE; i++) {
			int port = MIN + RANDOM.nextInt(RANGE);
			if (isPortAvailable(port)) {
				return port;
			}
		}
		throw new IllegalStateException(String.format("Could not find an available port in the range [%d, %d])",
				MIN, MAX));

	}

	/**
	 * Find 'N' free {@code TCP} ports.
	 *
	 * @param count the number of available ports to find
	 * @return free ports
	 * @throws IllegalStateException if ports could not be found
	 */
	@Nonnull
	public static Collection<Integer> getPorts(int count) {
		if (count <= 0) {
			return Collections.emptySet();
		}
		Set<Integer> ports = new LinkedHashSet<>();
		for (int i = 0; i < RANGE; i++) {
			ports.add(getPort());
			if (ports.size() == count) {
				return ports;
			}
		}
		throw new IllegalStateException(String.format("Could not find (%d) ports in the range [%d, %d])",
				count, MIN, MAX));
	}

	/**
	 * Test whether the {@code TCP} port is busy or not.
	 *
	 * @param address the address
	 * @param port the TCP port
	 * @return {@code true} if port is busy, otherwise {@code false}
	 * @throws IllegalArgumentException if host is unknown
	 */
	public static boolean isPortBusy(@Nullable String address, int port) {
		return isPortBusy(getInetAddress(address), port);
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
			s.connect(getInetSocketAddress(address, port));
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}


	private static boolean isPortAvailable(int port) {
		try (ServerSocket ss = new ServerSocket()) {
			ss.bind(getInetSocketAddress(LOCALHOST, port), 1);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}


	private static InetAddress getInetAddress(String address) {
		if (StringUtils.hasText(address)) {
			try {
				return InetAddress.getByName(address);
			}
			catch (UnknownHostException ex) {
				throw new IllegalArgumentException(ex);
			}
		}
		return LOCALHOST;
	}

	private static InetSocketAddress getInetSocketAddress(InetAddress address, int port) {
		if (address != null) {
			return new InetSocketAddress(address, port);
		}
		return new InetSocketAddress(LOCALHOST, port);
	}

	private static InetAddress getLocalhost() {
		try {
			return InetAddress.getByName("localhost");
		}
		catch (UnknownHostException ex) {
			return InetAddress.getLoopbackAddress();
		}
	}

}
