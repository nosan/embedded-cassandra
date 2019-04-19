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

import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileLockInterruptionException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraInterruptedException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * A simple implementation of the {@link Cassandra} which just delegates everything to the underlying
 * {@link CassandraDatabase}.
 *
 * @author Dmytro Nosan
 * @see LocalCassandraFactory
 * @since 1.0.0
 */
class LocalCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(LocalCassandra.class);

	private final Object monitor = new Object();

	private final CassandraDatabase database;

	private volatile State state = State.NEW;

	@Nullable
	private volatile Thread currentThread;

	LocalCassandra(long id, boolean registerShutdownHook, CassandraDatabase database) {
		this.database = database;
		if (registerShutdownHook) {
			registerShutdownHook(id);
		}
	}

	@Override
	public void start() throws CassandraException {
		synchronized (this.monitor) {
			if (this.state != State.STARTED) {
				try {
					this.currentThread = Thread.currentThread();
					this.state = State.STARTING;
					this.database.start();
					this.state = State.STARTED;
					this.currentThread = null;
				}
				catch (InterruptedException | ClosedByInterruptException | FileLockInterruptionException ex) {
					this.currentThread = null;
					this.state = State.START_INTERRUPTED;
					stopDatabase();
					throw new CassandraInterruptedException(ex);
				}
				catch (Exception ex) {
					this.currentThread = null;
					this.state = State.START_FAILED;
					stopDatabase();
					throw new CassandraException(String.format("Unable to start Apache Cassandra '%s'", getVersion()),
							ex);
				}
			}
		}
	}

	@Override
	public void stop() throws CassandraException {
		synchronized (this.monitor) {
			if (this.state != State.STOPPED && this.state != State.NEW) {
				try {
					this.state = State.STOPPING;
					this.database.stop();
					this.state = State.STOPPED;
				}
				catch (InterruptedException | ClosedByInterruptException ex) {
					this.state = State.STOP_INTERRUPTED;
					throw new CassandraInterruptedException(ex);
				}
				catch (Exception ex) {
					this.state = State.STOP_FAILED;
					throw new CassandraException(String.format("Unable to stop Apache Cassandra '%s'", getVersion()),
							ex);
				}
			}
		}
	}

	@Override
	public Settings getSettings() throws IllegalStateException {
		synchronized (this.monitor) {
			if (getState() != State.STARTED) {
				throw new IllegalStateException(String.format("Apache Cassandra '%s' is not running.", getVersion()));
			}
			return this.database.getSettings();
		}
	}

	@Override
	public Version getVersion() {
		return this.database.getVersion();
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public String toString() {
		return String.format("Apache Cassandra '%s'", getVersion());
	}

	private void registerShutdownHook(long id) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Optional.ofNullable(this.currentThread).ifPresent(Thread::interrupt);
			stop();
		}, String.format("cassandra:%d:hook", id)));
	}

	private void stopDatabase() {
		try {
			this.database.stop();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		catch (Exception ex) {
			log.error(String.format("Unable to stop Apache Cassandra '%s'", getVersion()), ex);
		}
	}

}
