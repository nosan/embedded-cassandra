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
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.util.FileUtils;

/**
 * {@link CassandraFactory} to create a local {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see LocalCassandraFactoryBuilder
 * @since 1.0.0
 */
public final class LocalCassandraFactory implements CassandraFactory {

	@Nonnull
	private final List<String> jvmOptions = new ArrayList<>();

	@Nullable
	private Version version;

	@Nullable
	private ArtifactFactory artifactFactory;

	@Nullable
	private Duration startupTimeout;

	@Nullable
	private Path workingDirectory;

	@Nullable
	private URL configurationFile;

	@Nullable
	private URL logbackFile;

	@Nullable
	private URL rackFile;

	@Nullable
	private URL topologyFile;

	@Nullable
	private Path javaHome;

	private int jmxPort = 7199;

	/**
	 * JMX port. This property will be passed to the Cassandra as {@code -Dcassandra.jmx.local.port={jmxPort}}
	 *
	 * @return The value of the {@code jmxPort} attribute
	 * @since 1.1.1
	 */
	public int getJmxPort() {
		return this.jmxPort;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getJmxPort} attribute.
	 *
	 * @param jmxPort The value for jmxPort
	 * @since 1.1.1
	 */
	public void setJmxPort(int jmxPort) {
		this.jmxPort = jmxPort;
	}

	/**
	 * Java home directory.
	 *
	 * @return The value of the {@code javaHome} attribute
	 * @since 1.0.9
	 */
	@Nullable
	public Path getJavaHome() {
		return this.javaHome;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getJavaHome} attribute.
	 *
	 * @param javaHome The value for javaHome
	 * @since 1.0.9
	 */
	public void setJavaHome(@Nullable Path javaHome) {
		this.javaHome = javaHome;
	}

	/**
	 * Startup timeout.
	 *
	 * @return The value of the {@code startupTimeout} attribute
	 */
	@Nullable
	public Duration getStartupTimeout() {
		return this.startupTimeout;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getStartupTimeout} attribute.
	 *
	 * @param startupTimeout The value for startupTimeout
	 */
	public void setStartupTimeout(@Nullable Duration startupTimeout) {
		this.startupTimeout = startupTimeout;
	}

	/**
	 * JVM options that should be associated with Cassandra.
	 *
	 * @return The value of the {@code jvmOptions} attribute
	 */
	@Nonnull
	public List<String> getJvmOptions() {
		return this.jvmOptions;
	}

	/**
	 * The GossipingPropertyFileSnitch, Ec2Snitch, and Ec2MultiRegionSnitch use the cassandra-rackdc.properties
	 * configuration file to determine which datacenters and racks nodes belong to.
	 *
	 * @return the value of {@code rackFile} attribute
	 */
	@Nullable
	public URL getRackFile() {
		return this.rackFile;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getRackFile} attribute.
	 *
	 * @param rackFile The value for rackFile
	 */
	public void setRackFile(@Nullable URL rackFile) {
		this.rackFile = rackFile;
	}

	/**
	 * The PropertyFileSnitch uses the cassandra-topology.properties for datacenters and rack names and to determine
	 * network topology so that requests are routed efficiently and allows the database to distribute replicas evenly.
	 *
	 * @return the value of {@code topologyFile} attribute
	 */
	@Nullable
	public URL getTopologyFile() {
		return this.topologyFile;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getTopologyFile} attribute.
	 *
	 * @param topologyFile The value for topologyFile
	 */
	public void setTopologyFile(@Nullable URL topologyFile) {
		this.topologyFile = topologyFile;
	}

	/**
	 * Version of the Cassandra.
	 *
	 * @return The value of the {@code version} attribute
	 */
	@Nullable
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getVersion() version} attribute.
	 *
	 * @param version The value for version
	 */
	public void setVersion(@Nullable Version version) {
		this.version = version;
	}

	/**
	 * {@link ArtifactFactory}  that creates a {@link Artifact}.
	 *
	 * @return The value of the {@code artifactFactory} attribute
	 */
	@Nullable
	public ArtifactFactory getArtifactFactory() {
		return this.artifactFactory;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getArtifactFactory() artifactFactory} attribute.
	 *
	 * @param artifactFactory The value for artifactFactory
	 * @see RemoteArtifactFactory
	 */
	public void setArtifactFactory(@Nullable ArtifactFactory artifactFactory) {
		this.artifactFactory = artifactFactory;
	}

	/**
	 * Cassandra directory. This directory keeps data/logs, and other Cassandra's files. <p>
	 * By default {@link FileUtils#getTmpDirectory() tmp.dir} is used. <p>Note! Temporary directory will be deleted
	 * at the end. If you want to keep the data/logs between launches, you should specify the {@code real} directory.
	 *
	 * @return The value of the {@code workingDirectory} attribute
	 */
	@Nullable
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getWorkingDirectory() workingDirectory} attribute.
	 *
	 * @param workingDirectory The value for workingDirectory
	 */
	public void setWorkingDirectory(@Nullable Path workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Cassandra configuration file ({@code cassandra.yaml}).
	 *
	 * @return The value of the {@code configurationFile} attribute
	 */
	@Nullable
	public URL getConfigurationFile() {
		return this.configurationFile;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getConfigurationFile() configurationFile} attribute.
	 *
	 * @param configurationFile The value for configurationFile
	 */
	public void setConfigurationFile(@Nullable URL configurationFile) {
		this.configurationFile = configurationFile;
	}

	/**
	 * Cassandra logging file ({@code logback.xml}).
	 *
	 * @return The value of the {@code logbackFile} attribute
	 */
	@Nullable
	public URL getLogbackFile() {
		return this.logbackFile;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getLogbackFile() logbackFile} attribute.
	 *
	 * @param logbackFile The value for logbackFile
	 */
	public void setLogbackFile(@Nullable URL logbackFile) {
		this.logbackFile = logbackFile;
	}


	@Nonnull
	@Override
	public Cassandra create() {
		ArtifactFactory artifactFactory = getArtifactFactory();
		if (artifactFactory == null) {
			artifactFactory = new RemoteArtifactFactory();
		}
		Version version = getVersion();
		if (version == null) {
			version = new Version(3, 11, 3);
		}
		Duration startupTimeout = getStartupTimeout();
		if (startupTimeout == null || startupTimeout.toMillis() <= 0) {
			startupTimeout = Duration.ofMinutes(1);
		}
		Path workingDirectory = getWorkingDirectory();
		if (workingDirectory == null) {
			workingDirectory = FileUtils.getTmpDirectory().
					resolve(String.format("embedded-cassandra-%s", UUID.randomUUID()));
		}
		return new LocalCassandra(version, artifactFactory, workingDirectory, startupTimeout, getConfigurationFile(),
				getLogbackFile(), getRackFile(), getTopologyFile(), getJvmOptions(), getJavaHome(), getJmxPort());
	}


}
