/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.process;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;

import com.github.nosan.embedded.cassandra.Config;

/**
 * Utility class to await when cassandra is ready to accept connections.
 *
 * @author Dmytro Nosan
 */
abstract class TransportUtils {

	private static final String LOCALHOST = "localhost";

	/**
	 * Waits when Cassandra is ready to accept connections.
	 *
	 * @param config Cassandra's config.
	 * @param timeout how long to wait between connections.
	 * @return whether cassandra is ready or not.
	 */
	static boolean await(Config config, Duration timeout) {
		if (!isEnabled(config)) {
			return true;
		}
		long start = System.nanoTime();
		long rem = timeout.toNanos();
		do {
			if (tryConnect(config)) {
				return true;
			}
			if (rem > 0) {
				try {
					Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			rem = timeout.toNanos() - (System.nanoTime() - start);
		}
		while (rem > 0);

		return false;
	}

	private static boolean isEnabled(Config config) {
		return config.isStartNativeTransport() || config.isStartRpc();
	}

	private static boolean tryConnect(Config config) {
		if (config.isStartNativeTransport()) {
			return tryConnect(
					ObjectUtils.defaultIfNull(config.getRpcAddress(), LOCALHOST),
					config.getNativeTransportPort());
		}
		else if (config.isStartRpc()) {
			return tryConnect(
					ObjectUtils.defaultIfNull(config.getRpcAddress(), LOCALHOST),
					config.getRpcPort());
		}
		return false;
	}

	private static boolean tryConnect(String host, int port) {
		try (Socket ignored = new Socket(host, port)) {
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

}
