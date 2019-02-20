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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;

/**
 * This {@link Cassandra} implementation just a wrapper on {@link CassandraProcess}.
 *
 * @author Dmytro Nosan
 * @see LocalCassandraFactory
 * @since 1.0.0
 */
class LocalCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(LocalCassandra.class);

	private final boolean registerShutdownHook;

	@Nonnull
	private final Object lock = new Object();

	@Nonnull
	private final Version version;

	@Nonnull
	private final Initializer initializer;

	@Nonnull
	private final CassandraProcess process;

	@Nullable
	private volatile Settings settings;

	@Nonnull
	private volatile State state = State.NEW;

	private volatile boolean shutdownHookRegistered;

	private volatile boolean started;

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
	LocalCassandra(@Nonnull Version version, @Nonnull ArtifactFactory artifactFactory,
			@Nonnull Path workingDirectory, @Nonnull Path artifactDirectory,
			@Nonnull Duration startupTimeout, @Nullable URL configurationFile,
			@Nullable URL logbackFile, @Nullable URL rackFile, @Nullable URL topologyFile,
			@Nullable URL commitLogArchivingFile, @Nonnull List<String> jvmOptions, @Nullable Path javaHome,
			int jmxPort, boolean allowRoot, boolean registerShutdownHook) {
		Objects.requireNonNull(artifactFactory, "Artifact Factory must not be null");
		Objects.requireNonNull(version, "Version must not be null");
		Objects.requireNonNull(startupTimeout, "Startup timeout must not be null");
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		Objects.requireNonNull(workingDirectory, "Working Directory must not be null");
		this.version = version;
		this.registerShutdownHook = registerShutdownHook;
		this.initializer = new DefaultInitializer(workingDirectory, version,
				artifactFactory, artifactDirectory, configurationFile, logbackFile, rackFile,
				topologyFile, commitLogArchivingFile);
		this.process = new DefaultCassandraProcess(workingDirectory, version, startupTimeout, jvmOptions, javaHome,
				jmxPort, allowRoot);
	}

	@Override
	public void start() throws CassandraException {
		synchronized (this.lock) {
			if (this.started) {
				return;
			}
			try {
				this.state = State.INITIALIZING;
				initialize();
				this.state = State.INITIALIZED;
			}
			catch (Throwable ex) {
				this.state = State.FAILED;
				throw new CassandraException("Unable to initialize Cassandra", ex);
			}
			try {
				this.state = State.STARTING;
				start0();
				this.state = State.STARTED;
			}
			catch (InterruptedException ex) {
				if (log.isDebugEnabled()) {
					log.debug("Cassandra launch was interrupted");
				}
				stopSilently();
				this.state = State.INTERRUPTED;
				Thread.currentThread().interrupt();
			}
			catch (Throwable ex) {
				stopSilently();
				this.state = State.FAILED;
				throw new CassandraException("Unable to start Cassandra", ex);
			}
		}

	}

	@Override
	public void stop() throws CassandraException {
		synchronized (this.lock) {
			if (!this.started) {
				return;
			}
			try {
				this.state = State.STOPPING;
				stop0();
				this.state = State.STOPPED;
			}
			catch (Throwable ex) {
				this.state = State.FAILED;
				throw new CassandraException("Unable to stop Cassandra", ex);
			}
		}
	}

	@Nonnull
	@Override
	public Settings getSettings() throws CassandraException {
		synchronized (this.lock) {
			Settings settings = this.settings;
			return Optional.ofNullable(settings)
					.orElseThrow(() -> new CassandraException(
							"Cassandra is not initialized. Please start it before calling this method."));
		}
	}

	@Nonnull
	@Override
	public State getState() {
		return this.state;
	}

	@Override
	@Nonnull
	public String toString() {
		return String.format("%s [%s]", getClass().getSimpleName(), this.version);
	}

	private void initialize() throws IOException {
		log.info("Initialize Apache Cassandra ({}). It takes a while...", this.version);
		long start = System.currentTimeMillis();
		this.initializer.initialize();
		long elapsed = System.currentTimeMillis() - start;
		if (this.registerShutdownHook && !this.shutdownHookRegistered) {
			String className = getClass().getSimpleName();
			String hexString = Integer.toHexString(hashCode());
			String name = String.format("Hook:%s:%s", className, hexString);
			Runtime.getRuntime().addShutdownHook(new Thread(this::stopSilently, name));
			this.shutdownHookRegistered = true;
		}
		log.info("Apache Cassandra ({}) has been initialized ({} ms)", this.version, elapsed);
	}

	private void start0() throws IOException, InterruptedException {
		log.info("Starts Apache Cassandra ({}) ", this.version);
		long start = System.currentTimeMillis();
		this.started = true;
		this.settings = this.process.start();
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra ({}) has been started ({} ms)", this.version, elapsed);
	}

	private void stop0() throws IOException {
		log.info("Stops Apache Cassandra ({}) ", this.version);
		long start = System.currentTimeMillis();
		this.process.stop();
		this.settings = null;
		this.started = false;
		long elapsed = System.currentTimeMillis() - start;
		log.info("Apache Cassandra ({}) has been stopped ({} ms)", this.version, elapsed);
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
}
