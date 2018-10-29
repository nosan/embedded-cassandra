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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * Utility methods for dealing with ports.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class PortUtils {

	private final static int MIN = 49152;

	private final static int MAX = 65535;

	private final static int RANGE = MAX - MIN;

	private static final Random RANDOM = new Random();

	/**
	 * Used ports.
	 */
	private static final Map<Integer, Integer> CLOSED = new ConcurrentHashMap<>();

	/**
	 * How many ports to remember.
	 */
	private static final int MAX_CLOSED = 1000;


	/**
	 * Find a free {@code TCP} port.
	 *
	 * @return a port
	 * @throws IllegalStateException if port could not be found
	 */
	public static int getPort() {
		if (CLOSED.size() > MAX_CLOSED) {
			CLOSED.clear();
		}
		for (int i = 0; i < RANGE; i++) {
			int port = MIN + RANDOM.nextInt(RANGE);
			if (!CLOSED.containsKey(port) && isFree(port) && CLOSED.putIfAbsent(port, port) == null) {
				return port;
			}
		}
		throw new IllegalStateException(String.format("Could not find an available port in the range [%d, %d])",
				MIN, MAX));

	}


	/**
	 * Test whether the {@code TCP} port is busy or not.
	 *
	 * @param address the address (if not specified, than {@link InetAddress#getLoopbackAddress()} will be used)
	 * @param port the TCP port
	 * @return {@code true} if port is busy, otherwise {@code false}
	 */
	public static boolean isPortBusy(@Nullable String address, int port) {
		try {
			new Socket(getInetAddress(address), port).close();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}


	private static boolean isFree(int port) {
		try (ServerSocket ss = new ServerSocket()) {
			ss.setReuseAddress(true);
			ss.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1);
		}
		catch (Exception ex) {
			return false;
		}
		return !isPortBusy(null, port);
	}


	private static InetAddress getInetAddress(String address) {
		try {
			return StringUtils.hasText(address) ? InetAddress.getByName(address) :
					InetAddress.getLoopbackAddress();
		}
		catch (UnknownHostException ex) {
			return InetAddress.getLoopbackAddress();
		}
	}

}
