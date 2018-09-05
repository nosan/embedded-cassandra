/*
 * Copyright 2018-2018 the original author or authors.
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

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
 * This class is just a wrapper on {@link LocalProcess}. It main goals are initialize/destroy all resources
 * before/after calling {@link LocalProcess}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class LocalCassandra implements Cassandra {

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

	@Nonnull
	private final Version version;

	@Nonnull
	private final ArtifactFactory artifactFactory;

	@Nonnull
	private final Directory directory;

	@Nonnull
	private final List<? extends DirectoryInitializer> initializers;

	@Nonnull
	private final LocalProcess localProcess;

	@Nullable
	private volatile Settings settings;

	private volatile boolean initialized = false;


	/**
	 * Creates {@link LocalCassandra}.
	 *
	 * @param version a version
	 * @param artifactFactory a factory to create {@link Artifact}
	 * @param workingDirectory a directory to keep data/logs/etc... (must be writable)
	 * @param startupTimeout a startup timeout
	 * @param configurationFile URL to {@code cassandra.yaml}
	 * @param logbackFile URL to {@code logback.xml}
	 * @param rackFile URL to {@code cassandra-rackdc.properties}
	 * @param topologyFile URL to {@code cassandra-topology.properties}
	 * @param jvmOptions additional {@code JVM} options
	 */
	LocalCassandra(@Nonnull Version version, @Nonnull ArtifactFactory artifactFactory,
			@Nonnull Path workingDirectory, @Nonnull Duration startupTimeout, @Nullable URL configurationFile,
			@Nullable URL logbackFile, @Nullable URL rackFile, @Nullable URL topologyFile,
			@Nonnull List<String> jvmOptions) {
		this.artifactFactory = Objects.requireNonNull(artifactFactory, "Artifact Factory must not be null");
		this.version = Objects.requireNonNull(version, "Version must not be null");
		this.directory = new BaseDirectory(Objects.requireNonNull(workingDirectory,
				"Working Directory must not be null"));
		List<DirectoryInitializer> initializers = new ArrayList<>();
		initializers.add(new LogbackFileInitializer(logbackFile));
		initializers.add(new ConfigurationFileInitializer(configurationFile));
		initializers.add(new RackFileInitializer(rackFile));
		initializers.add(new TopologyFileInitializer(topologyFile));
		initializers.add(new PortReplacerInitializer());
		this.initializers = Collections.unmodifiableList(initializers);
		this.localProcess = new LocalProcess(this.directory,
				Objects.requireNonNull(startupTimeout, "Startup timeout must not be null"),
				Objects.requireNonNull(jvmOptions, "JVM Options must not be null"));

		try {
			Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
		}
		catch (Throwable ex) {
			log.error(String.format("Shutdown hook is not registered for (%s)", getClass()), ex);
		}
	}


	@Override
	public void start() throws CassandraException {
		if (!this.initialized) {
			synchronized (this) {
				try {
					try {
						if (!this.initialized) {
							long start = System.currentTimeMillis();
							log.info("Starts Apache Cassandra");
							initializeWorkingDirectory();
							this.localProcess.start();
							this.settings = this.localProcess.getSettings();
							long end = System.currentTimeMillis();
							log.info("Apache Cassandra has been started ({} ms) ", end - start);
						}
					}
					finally {
						this.initialized = true;
					}
				}
				catch (Throwable ex) {
					try {
						stop();
					}
					catch (Throwable suppress) {
						ex.addSuppressed(suppress);
					}
					throw new CassandraException("Unable to start Cassandra", ex);
				}
			}
		}
	}


	@Override
	public void stop() throws CassandraException {
		if (this.initialized) {
			synchronized (this) {
				if (this.initialized) {
					long start = System.currentTimeMillis();
					log.info("Stops Apache Cassandra");
					try {
						this.localProcess.stop();
					}
					catch (Throwable ex) {
						throw new CassandraException("Unable to stop Cassandra", ex);
					}
					finally {
						try {
							this.directory.destroy();
						}
						catch (Throwable ex) {
							log.error("({}) has not been deleted", this.directory);
						}
						this.settings = null;
						this.initialized = false;
					}
					long end = System.currentTimeMillis();
					log.info("Apache Cassandra has been stopped ({} ms) ", end - start);
				}
			}
		}
	}

	@Nonnull
	@Override
	public Settings getSettings() throws CassandraException {
		Settings settings = this.settings;
		if (settings == null) {
			throw new CassandraException("Cassandra is not initialized. Please start it before calling this method.");
		}
		return settings;
	}

	private void initializeWorkingDirectory() throws Exception {
		Artifact artifact = this.artifactFactory.create(this.version);
		Objects.requireNonNull(artifact, "Artifact must not be null");
		this.directory.initialize(artifact);
		Path directory = this.directory.get();
		for (DirectoryInitializer initializer : this.initializers) {
			initializer.initialize(directory, this.version);
		}
	}


}
