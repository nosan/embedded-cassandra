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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Config;

/**
 * Utility class to check cassandra is ready to accept connections.
 *
 * @author Dmytro Nosan
 */
abstract class TransportUtils {

	private static final String LOCALHOST = "localhost";

	private static final Logger log = LoggerFactory.getLogger(TransportUtils.class);


	/**
	 * Check whether cassandra transport is ready or not.
	 *
	 * @param config Cassandra's config.
	 * @param attempts how many times to try to connect.
	 * @param wait how long to wait between connections.
	 * @throws IOException Cassandra transport has not been started.
	 */
	static void check(Config config, int attempts, Duration wait) throws IOException {
		if (isEnabled(config) && !isConnected(config, attempts, wait)) {
			throw new IOException("Cassandra process transport has not been started correctly.");
		}
	}

	private static boolean isEnabled(Config config) {
		return config.isStartNativeTransport() || config.isStartRpc();
	}

	private static boolean isConnected(Config config, int maxAttempts, Duration sleep) {
		for (int i = 0; i < maxAttempts; i++) {
			log.debug("Trying to connect to cassandra... Attempt:" + (i + 1));
			boolean connected = tryConnect(config);
			if (connected) {
				log.info("Connection to Cassandra has been established successfully.");
				return true;
			}
			try {
				TimeUnit.MILLISECONDS.sleep(sleep.toMillis());
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		log.error("Connection to Cassandra has not been established...");
		return false;
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
