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
import java.net.ServerSocket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Synchronized {@link Supplier} to generate random ports.
 *
 * @author Dmytro Nosan
 * @since 2.0.1
 */
class RandomPortSupplier implements Supplier<Integer> {

	static final Supplier<Integer> INSTANCE = new RandomPortSupplier(SocketUtils::getLocalhost);

	private static final int ATTEMPTS = 1024;

	private static final int SIZE = 100;

	private static final int MIN = 49152;

	private static final int MAX = 65535;

	private final Deque<Integer> ports = new ArrayDeque<>(SIZE);

	private final Supplier<InetAddress> addressSupplier;

	RandomPortSupplier(Supplier<InetAddress> addressSupplier) {
		this.addressSupplier = addressSupplier;
	}

	@Override
	public synchronized Integer get() {
		return getPort(this.addressSupplier.get());
	}

	private int getPort(InetAddress address) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if (this.ports.size() == SIZE) {
			this.ports.removeFirst();
		}
		for (int i = 0; i < ATTEMPTS; i++) {
			int port = MIN + random.nextInt(MAX - MIN + 1);
			if (!this.ports.contains(port)) {
				try (ServerSocket ignore = new ServerSocket(port, 1, address)) {
					this.ports.addLast(port);
					return port;
				}
				catch (IOException ex) {
					//ignore
				}
			}
		}
		throw new IllegalStateException(String.format("Can not find an available port in the range [%d, %d]",
				MIN, MAX));
	}

}
