/*
 * Copyright 2020 the original author or authors.
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;

class DefaultCassandra implements Cassandra {

	private static final Logger LOGGER = Logger.get(DefaultCassandra.class);

	private final String name;

	private final Version version;

	private final Map<String, Object> environmentVariables;

	private final Map<String, Object> systemProperties;

	private final boolean registerShutdownHook;

	private final Set<String> jvmOptions;

	private final IOSupplier<? extends Path> workingDirectorySupplier;

	private final Map<String, Object> configProperties;

	private final Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers;

	private final WorkingDirectoryInitializer workingDirectoryInitializer;

	private final WorkingDirectoryDestroyer workingDirectoryDestroyer;

	private final Duration startupTimeout;

	private final Logger logger;

	private final Object lock = new Object();

	private volatile boolean started = false;

	private volatile boolean running = false;

	private volatile Thread shutdownHookThread;

	private volatile CassandraDatabase database;

	private volatile Settings settings;

	DefaultCassandra(String name, Version version, Map<String, Object> environmentVariables,
			Map<String, Object> systemProperties, boolean registerShutdownHook, Set<String> jvmOptions,
			IOSupplier<? extends Path> workingDirectorySupplier, Map<String, Object> configProperties,
			WorkingDirectoryInitializer workingDirectoryInitializer,
			WorkingDirectoryDestroyer workingDirectoryDestroyer, Duration startupTimeout,
			Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers, Logger logger) {
		this.name = name;
		this.version = version;
		this.startupTimeout = startupTimeout;
		this.workingDirectoryInitializer = workingDirectoryInitializer;
		this.registerShutdownHook = registerShutdownHook;
		this.workingDirectorySupplier = workingDirectorySupplier;
		this.workingDirectoryDestroyer = workingDirectoryDestroyer;
		this.environmentVariables = environmentVariables;
		this.systemProperties = systemProperties;
		this.jvmOptions = jvmOptions;
		this.configProperties = configProperties;
		this.workingDirectoryCustomizers = workingDirectoryCustomizers;
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
		createDatabase();
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
	public String toString() {
		return "DefaultCassandra{" + "name='" + this.name + "', version='" + this.version + "'}";
	}

	private void createDatabase() {
		Version version = this.version;
		Path workingDirectory;
		try {
			workingDirectory = Files.createDirectories(this.workingDirectorySupplier.get());
		}
		catch (Exception ex) {
			throw new CassandraException("Unable to create Working Directory", ex);
		}

		try {
			this.workingDirectoryInitializer.init(workingDirectory, version);
			for (WorkingDirectoryCustomizer workingDirectoryCustomizer : this.workingDirectoryCustomizers) {
				workingDirectoryCustomizer.customize(workingDirectory, version);
			}
		}
		catch (Exception ex) {
			destroyWorkingDirectory(workingDirectory, version);
			throw new CassandraException(String.format("Unable to initialize Working Directory: '%s'",
					workingDirectory), ex);
		}

		try {
			CassandraDatabaseFactory cassandraDatabaseFactory = new DefaultCassandraDatabaseFactory(this.name,
					version, this.environmentVariables, this.configProperties, this.systemProperties,
					this.jvmOptions);
			this.database = cassandraDatabaseFactory.create(workingDirectory);
		}
		catch (Exception ex) {
			destroyWorkingDirectory(workingDirectory, version);
			throw new CassandraException("Unable to create Cassandra Database", ex);
		}
	}

	private void doStart() {
		synchronized (this.lock) {
			try {
				this.started = true;
				this.database.start();
				this.database.getStdOut().attach(this.logger::info);
				this.database.getStdErr().attach(this.logger::error);
				addShutdownHook();
			}
			catch (Exception ex) {
				try {
					doStop();
				}
				catch (Exception suppressed) {
					ex.addSuppressed(suppressed);
				}
				throw new CassandraException("Unable to start " + toString(), ex);
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
					throw new CassandraException("Unable to stop " + toString(), ex);
				}
				destroyWorkingDirectory(database.getWorkingDirectory(), database.getVersion());
			}
			removeShutdownHook();
			this.started = false;
			this.running = false;
			this.database = null;
		}
	}

	private void await() {
		CassandraDatabase database = this.database;
		Duration timeout = this.startupTimeout;
		try (TransportReadiness transportReadiness = new TransportReadiness(database);
				RpcTransportReadiness rpcTransportReadiness = new RpcTransportReadiness(database);
				OutputCollector outputCollector = new OutputCollector(database);
				ErrorCollector errorCollector = new ErrorCollector(database)) {
			long start = System.nanoTime();
			long rem = timeout.toNanos();
			while (rem > 0 && database.isAlive()
					&& !(transportReadiness.isStartedOrDisabled() && rpcTransportReadiness.isStartedOrDisabled())) {
				Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
				rem = timeout.toNanos() - (System.nanoTime() - start);
			}
			if (rem > 0 && database.isAlive()
					&& (!transportReadiness.isDisabled() || !rpcTransportReadiness.isDisabled())) {
				//We actually don't know whether transport has been started. Stdout
				//does not contain such information. So, here we sleep for additional 2 seconds
				//and if the transport has been failed to start,
				//the following conditional will catch this.
				//We can't use connect(address,port) because we could connect to another running Cassandra.
				Thread.sleep(2000);
			}
			if (!database.isAlive() || transportReadiness.isFailed() || rpcTransportReadiness.isFailed()) {
				StringBuilder message = new StringBuilder(String.format("'%s' is not alive", database))
						.append(" or transport has been failed to start.")
						.append(" Please see logs for more details.");
				List<String> errors = errorCollector.getErrors();
				if (!errors.isEmpty()) {
					message.append(String.format("%nErrors:%n%s", String.join(System.lineSeparator(), errors)));
				}
				Deque<String> output = outputCollector.getOutput();
				if (!output.isEmpty()) {
					message.append(String.format("%nOutput:%n%s", String.join(System.lineSeparator(), output)));
				}
				throw new IOException(message.toString());
			}
			if (rem <= 0
					&& !(transportReadiness.isStartedOrDisabled() && rpcTransportReadiness.isStartedOrDisabled())) {
				throw new IllegalStateException(String.format("%s couldn't be started within %sms",
						database, this.startupTimeout.toMillis()));
			}
			InetAddress address = Optional.ofNullable(Optional.ofNullable(transportReadiness.getAddress())
					.orElse(rpcTransportReadiness.getAddress())).orElseGet(() -> getAddress(database));
			int port = Optional.ofNullable(transportReadiness.getPort()).orElseGet(() -> getPort(database));
			Integer sslPort = Optional.ofNullable(transportReadiness.getSslPort())
					.orElseGet(() -> getSslPort(database));
			int rpcPort = Optional.ofNullable(rpcTransportReadiness.getPort()).orElseGet(() -> getRpcPort(database));
			this.settings = new DefaultSettings(database.getName(), database.getVersion(), address, port, rpcPort,
					sslPort, database.getWorkingDirectory(), database.getJvmOptions(), database.getSystemProperties(),
					database.getEnvironmentVariables(), database.getConfigProperties());
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
			throw new CassandraException("Unable to start " + toString(), ex);
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

	private void destroyWorkingDirectory(Path workingDirectory, Version version) {
		try {
			this.workingDirectoryDestroyer.destroy(workingDirectory, version);
		}
		catch (Exception ex) {
			LOGGER.error(ex, "Working directory: ''{0}'' could not be destroyed", workingDirectory);
		}
	}

	private static int getRpcPort(CassandraDatabase database) {
		Object value = database.getSystemProperties().get("cassandra.rpc_port");
		if (value == null) {
			value = database.getConfigProperties().get("rpc_port");
		}
		return (value != null) ? Integer.parseInt(value.toString()) : 9160;
	}

	private static Integer getSslPort(CassandraDatabase database) {
		Object value = database.getConfigProperties().get("native_transport_port_ssl");
		return (value != null) ? Integer.parseInt(value.toString()) : null;
	}

	private static int getPort(CassandraDatabase database) {
		Object value = database.getSystemProperties().get("cassandra.native_transport_port");
		if (value == null) {
			value = database.getConfigProperties().get("native_transport_port");
		}
		return (value != null) ? Integer.parseInt(value.toString()) : 9042;
	}

	private static InetAddress getAddress(CassandraDatabase database) {
		Object value = database.getConfigProperties().get("rpc_address");
		if (value != null) {
			try {
				return InetAddress.getByName(value.toString());
			}
			catch (UnknownHostException ex) {
				LOGGER.error(ex, "Could not parse an address: ''{0}''.", value);
			}
		}
		try {
			LOGGER.warn("Cassandra Address is undefined. LocalHost Address will be used.");
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException ex) {
			LOGGER.warn("LocalHost address is undefined. Loopback Address will be used.");
			return InetAddress.getLoopbackAddress();
		}
	}

}
