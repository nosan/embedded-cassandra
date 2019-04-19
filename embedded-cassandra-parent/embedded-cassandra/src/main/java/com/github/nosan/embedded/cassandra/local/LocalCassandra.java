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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

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

	private final ThreadFactory threadFactory;

	private final Object monitor = new Object();

	private final CassandraDatabase database;

	private volatile State state = State.NEW;

	@Nullable
	private volatile Thread startDatabaseThread;

	LocalCassandra(long id, boolean registerShutdownHook, CassandraDatabase database) {
		this.database = database;
		this.threadFactory = new DefaultThreadFactory("ac", id);
		if (registerShutdownHook) {
			registerShutdownHook(id);
		}
	}

	@Override
	public void start() throws CassandraException {
		synchronized (this.monitor) {
			if (this.state != State.STARTED) {
				try {
					this.state = State.STARTING;
					startDatabase();
					this.state = State.STARTED;
				}
				catch (InterruptedException ex) {
					this.state = State.START_INTERRUPTED;
					interruptStartDatabase();
					stopDatabaseSafely();
					throw new CassandraInterruptedException(ex);
				}
				catch (Throwable ex) {
					this.state = State.START_FAILED;
					interruptStartDatabase();
					stopDatabaseSafely();
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
					stopDatabase();
					this.state = State.STOPPED;
				}
				catch (InterruptedException ex) {
					this.state = State.STOP_INTERRUPTED;
					throw new CassandraInterruptedException(ex);
				}
				catch (Throwable ex) {
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

	private static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	private void registerShutdownHook(long id) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			interruptStartDatabase();
			stop();
		}, String.format("ac:%d:sh", id)));
	}

	private void startDatabase() throws Throwable {
		AtomicReference<Throwable> throwableRef = new AtomicReference<>();
		Thread thread = this.threadFactory.newThread(() -> {
			try {
				this.database.start();
			}
			catch (InterruptedException | ClosedByInterruptException | FileLockInterruptionException ex) {
				selfInterrupt();
			}
			catch (Throwable ex) {
				throwableRef.set(ex);
			}
		});
		this.startDatabaseThread = thread;
		thread.start();
		thread.join();
		Throwable ex = throwableRef.get();
		if (ex != null) {
			throw ex;
		}
	}

	private void stopDatabase() throws Throwable {
		AtomicReference<Throwable> throwableRef = new AtomicReference<>();
		Thread thread = this.threadFactory.newThread(() -> {
			try {
				this.database.stop();
			}
			catch (InterruptedException | ClosedByInterruptException ex) {
				selfInterrupt();
			}
			catch (Throwable ex) {
				throwableRef.set(ex);
			}
		});
		thread.start();
		thread.join();
		Throwable ex = throwableRef.get();
		if (ex != null) {
			throw ex;
		}
	}

	private void stopDatabaseSafely() {
		try {
			stopDatabase();
		}
		catch (InterruptedException ex) {
			selfInterrupt();
		}
		catch (Throwable ex) {
			log.error(String.format("Unable to stop Apache Cassandra '%s'", getVersion()), ex);
		}
	}

	private void interruptStartDatabase() {
		Thread thread = this.startDatabaseThread;
		if (thread != null) {
			try {
				thread.interrupt();
			}
			catch (SecurityException ex) {
				//ignore
			}
			try {
				thread.join();
			}
			catch (InterruptedException ex) {
				selfInterrupt();
			}
		}
	}

}
