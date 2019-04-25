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
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Random port {@link Supplier}.
 *
 * @author Dmytro Nosan
 * @since 2.0.1
 */
class PortSupplier implements Supplier<Integer>, AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(PortSupplier.class);

	private static final int MIN = 49152;

	private static final int MAX = 65535;

	private final List<ServerSocket> sockets = new ArrayList<>();

	@Nullable
	private final InetAddress inetAddress;

	PortSupplier(@Nullable InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	@Override
	public Integer get() {
		ServerSocket serverSocket = createServerSocket();
		this.sockets.add(serverSocket);
		return serverSocket.getLocalPort();
	}

	@Override
	public void close() {
		this.sockets.forEach(this::close);
	}

	private ServerSocket createServerSocket() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i <= MAX - MIN; i++) {
			int port = MIN + random.nextInt(MAX - MIN + 1);
			try {
				return new ServerSocket(port, 1, this.inetAddress);
			}
			catch (IOException ex) {
				//ignore
			}
		}
		throw new IllegalStateException(String.format("Can not find an available port in the range [%d, %d]",
				MIN, MAX));
	}

	private void close(ServerSocket ss) {
		try {
			ss.close();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(String.format("Can not close '%s'", ss), ex);
		}
	}

}
