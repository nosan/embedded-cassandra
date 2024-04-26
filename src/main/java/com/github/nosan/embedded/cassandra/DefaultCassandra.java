/*
 * Copyright 2020-2024 the original author or authors.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(DefaultCassandra.class);

	private final String name;

	private final Version version;

	private final boolean registerShutdownHook;

	private final Path workingDirectory;

	private final Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers;

	private final WorkingDirectoryInitializer workingDirectoryInitializer;

	private final WorkingDirectoryDestroyer workingDirectoryDestroyer;

	private final Duration startupTimeout;

	private final Object lock = new Object();

	private final CassandraDatabaseFactory databaseFactory;

	private final Logger logger;

	private volatile boolean started = false;

	private volatile boolean running = false;

	private volatile Thread shutdownHookThread;

	private volatile CassandraDatabase database;

	private volatile Settings settings;

	DefaultCassandra(String name, Version version, Path workingDirectory, boolean registerShutdownHook,
			WorkingDirectoryInitializer workingDirectoryInitializer,
			WorkingDirectoryDestroyer workingDirectoryDestroyer, Duration startupTimeout,
			Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers,
			CassandraDatabaseFactory databaseFactory, Logger logger) {
		this.name = name;
		this.version = version;
		this.startupTimeout = startupTimeout;
		this.workingDirectoryInitializer = workingDirectoryInitializer;
		this.registerShutdownHook = registerShutdownHook;
		this.workingDirectory = workingDirectory;
		this.workingDirectoryDestroyer = workingDirectoryDestroyer;
		this.databaseFactory = databaseFactory;
		this.workingDirectoryCustomizers = Collections.unmodifiableSet(workingDirectoryCustomizers);
		this.logger = logger;
	}

	@Override
	public synchronized void start() throws CassandraException {
		if (this.started) {
			return;
		}
		this.settings = null;
		this.running = false;
		this.database = null;
		init();
		doStart();
		await();
		//if a database was stopped outside this class.
		this.database.onExit().thenRun(this::doStop);
	}

	@Override
	public synchronized void stop() throws CassandraException {
		if (!this.started) {
			return;
		}
		doStop();
	}

	@Override
	public synchronized Settings getSettings() {
		Settings settings = this.settings;
		if (settings == null) {
			throw new IllegalStateException("The getSettings() method was called but start() had not been called");
		}
		return settings;
	}

	@Override
	public boolean isRunning() {
		CassandraDatabase database = this.database;
		return this.running && (database != null && database.isAlive());
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	@Override
	public String toString() {
		return "DefaultCassandra{" + "name='" + this.name + "', version='" + this.version + "'}";
	}

	private void init() {
		Version version = this.version;
		try {
			Files.createDirectories(this.workingDirectory);
			this.workingDirectoryInitializer.init(this.workingDirectory, version);
			for (WorkingDirectoryCustomizer workingDirectoryCustomizer : this.workingDirectoryCustomizers) {
				workingDirectoryCustomizer.customize(this.workingDirectory, version);
			}
			this.database = this.databaseFactory.create(this.workingDirectory);
		}
		catch (Exception ex) {
			destroyWorkingDirectory();
			throw new CassandraException(
					String.format("Unable to initialize %s. Caused by: %s", this, ex), ex);
		}
	}

	private void doStart() {
		synchronized (this.lock) {
			try {
				this.started = true;
				addShutdownHook();
				this.database.start();
			}
			catch (Exception ex) {
				try {
					doStop();
				}
				catch (Exception suppressed) {
					ex.addSuppressed(suppressed);
				}
				throw new CassandraException(
						String.format("Unable to start %s. Caused by: %s", this, ex), ex);
			}
		}
	}

	private void doStop() {
		synchronized (this.lock) {
			if (!this.started) {
				return;
			}
			CassandraDatabase database = this.database;
			if (database != null) {
				try {
					database.stop();
				}
				catch (Exception ex) {
					throw new CassandraException(
							String.format("Unable to stop %s. Caused by: %s", this, ex), ex);
				}
			}
			destroyWorkingDirectory();
			removeShutdownHook();
			this.started = false;
			this.running = false;
			this.database = null;
		}
	}

	private void await() {
		CassandraDatabase database = this.database;
		Duration timeout = this.startupTimeout;
		database.getStdOut().attach(this.logger::info);
		database.getStdErr().attach(this.logger::error);
		try (OutputCollector outputCollector = new OutputCollector(database);
				NativeTransportParser nativeTransport = new NativeTransportParser(database);
				ErrorCollector errorCollector = new ErrorCollector(database);
				StartupParser startup = new StartupParser(database)) {
			long start = System.nanoTime();
			long rem = timeout.toNanos();
			while (rem > 0 && database.isAlive() && !(nativeTransport.isComplete() && startup.isComplete())) {
				Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
				rem = timeout.toNanos() - (System.nanoTime() - start);
			}
			if (!database.isAlive() || nativeTransport.isFailed()) {
				StringBuilder message = new StringBuilder(String.format("'%s' is not alive.", database))
						.append(" Please see logs for more details.");
				List<String> errors = errorCollector.getErrors();
				if (!errors.isEmpty()) {
					message.append(String.format("%nErrors:%n%s", String.join(System.lineSeparator(), errors)));
				}
				message.append(String.format("%nOutput:%n%s",
						String.join(System.lineSeparator(), outputCollector.getOutput())));
				throw new IOException(message.toString());
			}
			if (rem <= 0) {
				throw new IllegalStateException(String.format("%s couldn't be started within %sms",
						database, this.startupTimeout.toMillis()));
			}
			this.settings = new DefaultSettings(database.getName(), database.getVersion(), nativeTransport.getAddress(),
					nativeTransport.isStarted(), nativeTransport.getPort(), nativeTransport.getSslPort(),
					database.getConfigurationFile(), database.getWorkingDirectory(), database.getJvmOptions(),
					database.getSystemProperties(), database.getEnvironmentVariables(), database.getConfigProperties());
			this.running = true;
		}
		catch (Exception ex) {
			try {
				doStop();
			}
			catch (Exception suppressed) {
				ex.addSuppressed(suppressed);
			}
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new CassandraException(
					String.format("Unable to await %s. Caused by: %s", this, ex), ex);
		}
	}

	private void addShutdownHook() {
		if (this.registerShutdownHook && this.shutdownHookThread == null) {
			Thread thread = new Thread(this::doStop, this.name + "-sh");
			Runtime.getRuntime().addShutdownHook(thread);
			this.shutdownHookThread = thread;
		}
	}

	private void removeShutdownHook() {
		Thread shutdownHookThread = this.shutdownHookThread;
		if (shutdownHookThread != null && shutdownHookThread != Thread.currentThread()) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
				this.shutdownHookThread = null;
			}
			catch (Exception ex) {
				// If the virtual machine is already in the process of shutting down
			}
		}
	}

	private void destroyWorkingDirectory() {
		try {
			this.workingDirectoryDestroyer.destroy(this.workingDirectory, this.version);
		}
		catch (Exception ex) {
			log.error("Working directory: ''{}'' could not be destroyed", this.workingDirectory, ex);
		}
	}

}
