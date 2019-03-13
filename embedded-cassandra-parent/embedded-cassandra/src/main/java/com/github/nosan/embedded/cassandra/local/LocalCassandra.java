/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * This {@link Cassandra} implementation just a wrapper on {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @see LocalCassandraFactory
 * @since 1.0.0
 */
class LocalCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(LocalCassandra.class);

	private final boolean registerShutdownHook;

	private final int jmxPort;

	private final boolean allowRoot;

	private final Object lock = new Object();

	private final Version version;

	private final ArtifactFactory artifactFactory;

	private final Path workingDirectory;

	private final Path artifactDirectory;

	private final Duration startupTimeout;

	@Nullable
	private final URL configurationFile;

	@Nullable
	private final URL logbackFile;

	@Nullable
	private final URL rackFile;

	@Nullable
	private final URL topologyFile;

	@Nullable
	private final URL commitLogArchivingFile;

	private final List<String> jvmOptions;

	@Nullable
	private final Path javaHome;

	private volatile State state = State.NEW;

	@Nullable
	private volatile Thread currentThread;

	@Nullable
	private CassandraNode node;

	@Nullable
	private Settings settings;

	/**
	 * Creates a new {@link LocalCassandra}.
	 *
	 * @param version a version
	 * @param artifactFactory a factory to create {@link Artifact}
	 * @param workingDirectory a directory to keep data/logs/etc... (must be writable)
	 * @param artifactDirectory a directory to extract an {@link Artifact} (must be writable)
	 * @param startupTimeout a startup timeout
	 * @param configurationFile URL to {@code cassandra.yaml}
	 * @param logbackFile URL to {@code logback.xml}
	 * @param rackFile URL to {@code cassandra-rackdc.properties}
	 * @param topologyFile URL to {@code cassandra-topology.properties}
	 * @param commitLogArchivingFile URL to {@code commitlog_archiving.properties}
	 * @param jvmOptions additional {@code JVM} options
	 * @param javaHome java home directory
	 * @param jmxPort JMX port
	 * @param allowRoot allow running as a root
	 * @param registerShutdownHook whether shutdown hook should be registered or not
	 */
	LocalCassandra(Version version, ArtifactFactory artifactFactory,
			Path workingDirectory, Path artifactDirectory,
			Duration startupTimeout, @Nullable URL configurationFile,
			@Nullable URL logbackFile, @Nullable URL rackFile, @Nullable URL topologyFile,
			@Nullable URL commitLogArchivingFile, List<String> jvmOptions, @Nullable Path javaHome,
			int jmxPort, boolean allowRoot, boolean registerShutdownHook) {
		this.artifactFactory = artifactFactory;
		this.workingDirectory = workingDirectory;
		this.artifactDirectory = artifactDirectory;
		this.startupTimeout = startupTimeout;
		this.configurationFile = configurationFile;
		this.logbackFile = logbackFile;
		this.rackFile = rackFile;
		this.topologyFile = topologyFile;
		this.commitLogArchivingFile = commitLogArchivingFile;
		this.jvmOptions = jvmOptions;
		this.javaHome = javaHome;
		this.jmxPort = jmxPort;
		this.allowRoot = allowRoot;
		this.version = version;
		this.registerShutdownHook = registerShutdownHook;
	}

	@Override
	public void start() throws CassandraException {
		synchronized (this.lock) {
			if (this.state != State.STARTED) {
				try {
					this.currentThread = Thread.currentThread();
					try {
						registerShutdownHook();
					}
					catch (Throwable ex) {
						throw new CassandraException("Unable to register a shutdown hook for Cassandra", ex);
					}
					try {
						this.state = State.STARTING;
						initialize();
						start0();
						this.state = State.STARTED;
					}
					catch (InterruptedException ex) {
						stopSilently();
						this.state = State.START_INTERRUPTED;
						Thread.currentThread().interrupt();
					}
					catch (Throwable ex) {
						stopSilently();
						this.state = State.START_FAILED;
						throw new CassandraException("Unable to start Cassandra", ex);
					}
				}
				finally {
					this.currentThread = null;
				}
			}
		}
	}

	@Override
	public void stop() throws CassandraException {
		synchronized (this.lock) {
			if (this.state != State.STOPPED) {
				try {
					this.currentThread = Thread.currentThread();
					try {
						this.state = State.STOPPING;
						stop0();
						this.state = State.STOPPED;
					}
					catch (InterruptedException ex) {
						this.state = State.STOP_INTERRUPTED;
						Thread.currentThread().interrupt();
					}
					catch (Throwable ex) {
						this.state = State.STOP_FAILED;
						throw new CassandraException("Unable to stop Cassandra", ex);
					}
				}
				finally {
					this.currentThread = null;
				}
			}
		}
	}

	@Override
	public Settings getSettings() throws CassandraException {
		synchronized (this.lock) {
			if (this.state == State.STARTED) {
				Settings settings = this.settings;
				if (settings != null) {
					return settings;
				}
				throw new IllegalStateException("Settings cannot be null if Cassandra is running.");
			}
			throw new CassandraException("Cassandra is not started. Please start it before calling this method.");
		}
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", getClass().getSimpleName(), this.version);
	}

	private void initialize() throws IOException, InterruptedException {
		try {
			Version version = this.version;
			log.info("Initialize Apache Cassandra '{}'. It takes a while...", version);
			long start = System.currentTimeMillis();
			List<Initializer> initializers = new ArrayList<>();
			initializers.add(new WorkingDirectoryInitializer(this.artifactFactory, this.artifactDirectory));
			initializers.add(new ConfigurationFileInitializer(this.configurationFile));
			initializers.add(new LogbackFileInitializer(this.logbackFile));
			initializers.add(new RackFileInitializer(this.rackFile));
			initializers.add(new TopologyFileInitializer(this.topologyFile));
			initializers.add(new CommitLogFileInitializer(this.commitLogArchivingFile));
			initializers.add(new ConfigurationFileRandomPortInitializer());
			if (!isWindows()) {
				initializers.add(new CassandraFileExecutableInitializer());
			}
			for (Initializer initializer : initializers) {
				initializer.initialize(this.workingDirectory, version);
			}
			long elapsed = System.currentTimeMillis() - start;
			log.info("Apache Cassandra '{}' has been initialized ({} ms)", version, elapsed);
		}
		catch (IOException ex) {
			if (isClosedByInterruptException(ex)) {
				throw new InterruptedException();
			}
			throw ex;
		}
	}

	private void start0() throws IOException, InterruptedException {
		Version version = this.version;
		log.info("Starts Apache Cassandra '{}'", version);
		long start = System.currentTimeMillis();
		CassandraNode node = createNode();
		this.node = node;
		this.settings = node.start();
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra '{}' has been started ({} ms)", version, elapsed);
	}

	private void stop0() throws IOException, InterruptedException {
		Version version = this.version;
		CassandraNode node = this.node;
		if (node != null) {
			long start = System.currentTimeMillis();
			log.info("Stops Apache Cassandra '{}'", version);
			node.stop();
			this.node = null;
			long elapsed = System.currentTimeMillis() - start;
			log.info("Apache Cassandra '{}' has been stopped ({} ms)", version, elapsed);
		}
	}

	private CassandraNode createNode() {
		if (isWindows()) {
			return new WindowsCassandraNode(this.workingDirectory, this.version, this.startupTimeout, this.jvmOptions,
					this.javaHome, this.jmxPort);
		}
		return new UnixCassandraNode(this.workingDirectory, this.version, this.startupTimeout, this.jvmOptions,
				this.javaHome, this.jmxPort, this.allowRoot);
	}

	private void registerShutdownHook() {
		if (this.registerShutdownHook && this.state == State.NEW) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				Thread currentThread = this.currentThread;
				if (currentThread != null) {
					currentThread.interrupt();
				}
				stopSilently();
			}, "Cassandra Shutdown Hook"));
		}
	}

	private void stopSilently() {
		try {
			stop();
		}
		catch (Throwable ex) {
			if (log.isDebugEnabled()) {
				log.error("Unable to stop Cassandra", ex);
			}
		}
	}

	private static boolean isClosedByInterruptException(@Nullable Throwable ex) {
		if (ex instanceof ClosedByInterruptException) {
			return true;
		}
		return ex != null && isClosedByInterruptException(ex.getCause());
	}

	private static boolean isWindows() {
		return File.separatorChar == '\\';
	}

}
