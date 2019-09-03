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

package com.github.nosan.embedded.cassandra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.CacheConsumer;
import com.github.nosan.embedded.cassandra.commons.CompositeConsumer;
import com.github.nosan.embedded.cassandra.commons.MDCThreadFactory;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * Embedded {@link Database}.
 *
 * @author Dmytro Nosan
 */
class DefaultDatabase implements Database {

	private static final Logger log = LoggerFactory.getLogger(DefaultDatabase.class);

	private static final MDCThreadFactory threadFactory = new MDCThreadFactory();

	private final String name;

	private final Version version;

	private final boolean daemon;

	private final Logger logger;

	private final Duration timeout;

	private final Node node;

	@Nullable
	private volatile NodeProcess process;

	@Nullable
	private volatile InetAddress address;

	private volatile int port = -1;

	private volatile int sslPort = -1;

	private volatile int rpcPort = -1;

	DefaultDatabase(String name, Version version, boolean daemon, Logger logger, Duration timeout, Node node) {
		this.name = name;
		this.version = version;
		this.daemon = daemon;
		this.logger = logger;
		this.timeout = timeout;
		this.node = node;
	}

	@Override
	public void start() throws InterruptedException, IOException {
		log.info("Starts {}", toString());
		NodeProcess process = this.node.start();
		this.process = process;
		log.info("{} has been started", toString());
		NativeTransportReadinessConsumer nativeTransportReadiness = new NativeTransportReadinessConsumer(this.version);
		RpcTransportReadinessConsumer rpcTransportReadiness = new RpcTransportReadinessConsumer(this.version);
		await(process, nativeTransportReadiness, rpcTransportReadiness);
		log.info("{} is running and ready for connections", toString());
		int sslPort = nativeTransportReadiness.getSslPort();
		int port = nativeTransportReadiness.getPort();
		this.port = (port != -1) ? port : sslPort;
		this.sslPort = sslPort;
		this.rpcPort = rpcTransportReadiness.getRpcPort();
		InetAddress address = nativeTransportReadiness.getAddress();
		this.address = (address != null) ? address : rpcTransportReadiness.getAddress();
	}

	@Override
	public void stop() throws InterruptedException, IOException {
		NodeProcess process = this.process;
		if (process != null && process.isAlive()) {
			log.info("Stops {}", toString());
			process.stop();
			log.info("{} has been stopped", toString());
		}
		this.process = null;
		this.port = -1;
		this.sslPort = -1;
		this.rpcPort = -1;
		this.address = null;
	}

	@Override
	@Nullable
	public InetAddress getAddress() {
		return this.address;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public int getSslPort() {
		return this.sslPort;
	}

	@Override
	public int getRpcPort() {
		return this.rpcPort;
	}

	@Override
	public String toString() {
		return String.format("Cassandra Database (name='%s' version='%s' process='%s')", this.name, this.version,
				this.process);
	}

	private void await(NodeProcess process, ReadinessConsumer... readinessConsumers)
			throws IOException, InterruptedException {
		CompositeConsumer<String> compositeConsumer = new CompositeConsumer<>();
		CacheConsumer<String> cacheConsumer = new CacheConsumer<>(30);
		compositeConsumer.add(this.logger::info);
		compositeConsumer.add(cacheConsumer);
		for (ReadinessConsumer readinessConsumer : readinessConsumers) {
			compositeConsumer.add(readinessConsumer);
		}
		Thread thread = threadFactory.newThread(() -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getProcess().getInputStream(), StandardCharsets.UTF_8))) {
				try {
					reader.lines().filter(StringUtils::hasText).forEach(compositeConsumer);
				}
				catch (UncheckedIOException ex) {
					if (!ex.getMessage().contains("Stream closed")) {
						throw ex;
					}
				}
			}
			catch (IOException ex) {
				throw new UncheckedIOException("Stream cannot be closed", ex);
			}
		});
		thread.setName(this.name);
		thread.setDaemon(this.daemon);
		thread.setUncaughtExceptionHandler((t, ex) -> log.error("Exception in thread " + t, ex));
		thread.start();

		long start = System.nanoTime();
		long rem = this.timeout.toNanos();
		while (rem > 0 && process.isAlive() && !isReady(readinessConsumers)) {
			Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
			rem = this.timeout.toNanos() - (System.nanoTime() - start);
		}
		if (!process.isAlive()) {
			thread.join(100);
			List<String> lines = new ArrayList<>(cacheConsumer.get());
			Collections.reverse(lines);
			throw new IOException(String.format("'%s' is not alive. Please see logs for more details%n\t%s", process,
					String.join(String.format("%n\t"), lines)));
		}
		if (rem <= 0) {
			throw new IllegalStateException(
					toString() + " couldn't be started within " + this.timeout.toMillis() + "ms");
		}
		for (ReadinessConsumer readinessConsumer : readinessConsumers) {
			compositeConsumer.remove(readinessConsumer);
		}
		compositeConsumer.remove(cacheConsumer);
	}

	private static boolean isReady(Readiness... readinesses) {
		for (Readiness readiness : readinesses) {
			if (!readiness.isReady()) {
				return false;
			}
		}
		return true;
	}

}
