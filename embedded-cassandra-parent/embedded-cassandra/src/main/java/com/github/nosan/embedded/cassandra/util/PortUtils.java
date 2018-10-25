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

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
	 * Remembered ports.
	 */
	private static final Map<Integer, Integer> REMEMBER = new ConcurrentHashMap<>();

	/**
	 * How many ports to remember.
	 */
	private static final int MAX_REMEMBER = 1000;


	/**
	 * Find a free {@code TCP} port.
	 *
	 * @return a port
	 * @throws IllegalStateException if port could not be found
	 */
	public static int getPort() {
		if (REMEMBER.size() > MAX_REMEMBER) {
			REMEMBER.clear();
		}
		for (int i = 0; i < RANGE; i++) {
			int port = MIN + RANDOM.nextInt(RANGE);
			if (isFree(port)) {
				if (REMEMBER.putIfAbsent(port, port) == null) {
					return port;
				}
			}
		}
		throw new IllegalStateException(String.format("Could not find an available port in the range [%d, %d])",
				MIN, MAX));

	}


	private static boolean isFree(int port) {
		try (ServerSocket ss = new ServerSocket()) {
			ss.setReuseAddress(true);
			ss.bind(new InetSocketAddress(InetAddress.getLocalHost(), port), 1);
		}
		catch (Exception ex) {
			return false;
		}
		try {
			new Socket(InetAddress.getLoopbackAddress(), port).close();
			return false;
		}
		catch (ConnectException ex) {
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}


}
