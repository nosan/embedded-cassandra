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

import java.net.InetAddress;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * The node {@link Settings}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class NodeSettings implements Settings {

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private final Lock writeLock = this.readWriteLock.writeLock();

	private final Lock readLock = this.readWriteLock.readLock();

	private final Version version;

	@Nullable
	private InetAddress address;

	@Nullable
	private InetAddress rpcAddress;

	@Nullable
	private Integer port;

	@Nullable
	private Integer sslPort;

	@Nullable
	private Integer rpcPort;

	@Nullable
	private Boolean rpcTransportStarted;

	@Nullable
	private Boolean transportStarted;

	NodeSettings(Version version) {
		this.version = version;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public Optional<InetAddress> getOptionalAddress() {
		return read(() -> {
			InetAddress address = this.address;
			if (address != null) {
				return Optional.of(address);
			}
			return Optional.ofNullable(this.rpcAddress);
		});
	}

	@Override
	public Optional<Integer> getOptionalPort() {
		return read(() -> Optional.ofNullable(this.port));
	}

	@Override
	public Optional<Integer> getOptionalSslPort() {
		return read(() -> Optional.ofNullable(this.sslPort));
	}

	@Override
	public Optional<Integer> getOptionalRpcPort() {
		return read(() -> Optional.ofNullable(this.rpcPort));
	}

	@Override
	public Optional<Boolean> getOptionalRpcTransportStarted() {
		return read(() -> Optional.ofNullable(this.rpcTransportStarted));
	}

	@Override
	public Optional<Boolean> getOptionalTransportStarted() {
		return read(() -> Optional.ofNullable(this.transportStarted));
	}

	@Override
	public String toString() {
		return read(() -> new StringJoiner(", ", NodeSettings.class.getSimpleName() + " [", "]")
				.add("version=" + this.version)
				.add("address=" + Optional.ofNullable(this.address).orElse(this.rpcAddress))
				.add("port=" + this.port)
				.add("sslPort=" + this.sslPort)
				.add("rpcPort=" + this.rpcPort)
				.add("rpcTransportStarted=" + this.rpcTransportStarted)
				.add("transportStarted=" + this.transportStarted)
				.toString());
	}

	void stopRpcTransport() {
		write(() -> {
			this.rpcAddress = null;
			this.rpcPort = null;
			this.rpcTransportStarted = false;
		});
	}

	void stopTransport() {
		write(() -> {
			this.port = null;
			this.sslPort = null;
			this.address = null;
			this.transportStarted = false;
		});
	}

	void startRpcTransport(InetAddress address, int port) {
		write(() -> {
			this.rpcPort = port;
			this.rpcAddress = address;
			this.rpcTransportStarted = true;
		});
	}

	void startTransport(InetAddress address, int port, boolean ssl) {
		write(() -> {
			if (ssl) {
				this.sslPort = port;
			}
			else {
				this.port = port;
			}
			this.address = address;
			this.transportStarted = true;
		});
	}

	private <T> T read(Supplier<T> supplier) {
		Lock lock = this.readLock;
		lock.lock();
		try {
			return supplier.get();
		}
		finally {
			lock.unlock();
		}
	}

	private void write(Runnable runnable) {
		Lock lock = this.writeLock;
		lock.lock();
		try {
			runnable.run();
		}
		finally {
			lock.unlock();
		}
	}

}
