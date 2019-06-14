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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraInterruptedException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.MDCThreadFactory;

/**
 * A simple implementation of the {@link Cassandra} which just delegates everything to the underlying
 * {@link CassandraDatabase}.
 *
 * @author Dmytro Nosan
 * @see LocalCassandraFactory
 * @since 1.0.0
 */
class LocalCassandra implements Cassandra {

	private static final AtomicLong cassandraNumber = new AtomicLong();

	private static final Logger log = LoggerFactory.getLogger(LocalCassandra.class);

	private final long id = cassandraNumber.incrementAndGet();

	private final ThreadFactory threadFactory;

	private final CassandraDatabase database;

	private final boolean registerShutdownHook;

	private volatile State state = State.NEW;

	private volatile boolean started = false;

	@Nullable
	private volatile Thread shutdownHook;

	LocalCassandra(boolean registerShutdownHook, boolean daemon, CassandraDatabase database) {
		this.registerShutdownHook = registerShutdownHook;
		this.database = database;
		this.threadFactory = new MDCThreadFactory(String.format("cassandra-%d", this.id), daemon);
	}

	@Override
	public synchronized void start() throws CassandraException {
		if (this.started) {
			return;
		}
		try {
			this.state = State.STARTING;
			doStart();
			this.state = State.STARTED;
			this.started = true;
		}
		catch (InterruptedException | FileLockInterruptionException | ClosedByInterruptException ex) {
			this.state = State.START_INTERRUPTED;
			doStopSafely();
			throw new CassandraInterruptedException(String.format("%s has been interrupted", toString()), ex);
		}
		catch (Throwable ex) {
			this.state = State.START_FAILED;
			doStopSafely();
			throw new CassandraException(String.format("Unable to start %s", toString()), ex);
		}
	}

	@Override
	public synchronized void stop() throws CassandraException {
		if (!this.started) {
			return;
		}
		try {
			this.state = State.STOPPING;
			doStop();
			this.state = State.STOPPED;
			this.started = false;
		}
		catch (InterruptedException | FileLockInterruptionException | ClosedByInterruptException ex) {
			this.state = State.STOP_INTERRUPTED;
			throw new CassandraInterruptedException(String.format("%s has been interrupted", toString()), ex);
		}
		catch (Throwable ex) {
			this.state = State.STOP_FAILED;
			throw new CassandraException(String.format("Unable to stop %s", toString()), ex);
		}
	}

	@Override
	public synchronized Settings getSettings() throws IllegalStateException {
		if (!this.started) {
			throw new IllegalStateException(String.format("%s is not running.", toString()));
		}
		return this.database.getSettings();
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
		return String.format("Local Cassandra [id=%d, version=%s, state=%s]", this.id, getVersion(), getState());
	}

	private void doStart() throws Throwable {
		execute(this.database::start, runnable -> {
			Thread thread = this.threadFactory.newThread(runnable);
			if (this.registerShutdownHook) {
				Thread shutdownHook = new Thread(() -> {
					ThreadUtils.interrupt(thread);
					stop();
				}, String.format("cassandra:%d:sh", this.id));
				Runtime.getRuntime().addShutdownHook(shutdownHook);
				this.shutdownHook = shutdownHook;
			}
			return thread;
		});
	}

	private void doStop() throws Throwable {
		execute(this.database::stop, this.threadFactory);
		Thread shutdownHook = this.shutdownHook;
		if (shutdownHook != null && shutdownHook != Thread.currentThread()) {
			this.shutdownHook = null;
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
			catch (Throwable ex) {
				//VM is already shutting down
			}
		}
	}

	private void doStopSafely() {
		try {
			doStop();
		}
		catch (InterruptedException | FileLockInterruptionException | ClosedByInterruptException ex) {
			Thread.currentThread().interrupt();
			log.error(String.format("%s can be still alive. Shutdown has been interrupted", toString()), ex);
		}
		catch (Throwable ex) {
			log.error(String.format("Unable to stop %s", toString()), ex);
		}
	}

	private void execute(Executable executable, ThreadFactory threadFactory) throws Throwable {
		RunnableExecutable runnable = new RunnableExecutable(executable);
		Thread thread = threadFactory.newThread(runnable);
		thread.start();
		try {
			thread.join();
		}
		catch (InterruptedException je) {
			ThreadUtils.interrupt(thread);
			try {
				ThreadUtils.forceJoin(thread);
			}
			catch (InterruptedException jue) {
				je.addSuppressed(jue);
			}
			Throwable ex = runnable.getThrowable();
			if (ex != null) {
				je.addSuppressed(ex);
			}
			throw je;
		}
		Throwable ex = runnable.getThrowable();
		if (ex != null) {
			throw ex;
		}

	}

	@FunctionalInterface
	private interface Executable {

		void execute() throws Throwable;

	}

	private static class RunnableExecutable implements Runnable {

		private final Executable executable;

		private final AtomicReference<Throwable> throwable = new AtomicReference<>();

		RunnableExecutable(Executable executable) {
			this.executable = executable;
		}

		@Override
		public void run() {
			try {
				this.executable.execute();
			}
			catch (Throwable ex) {
				this.throwable.set(ex);
			}
		}

		@Nullable
		Throwable getThrowable() {
			return this.throwable.get();
		}

	}

}
