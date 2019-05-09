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

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.util.ClassUtils;
import com.github.nosan.embedded.cassandra.util.SystemUtils;

/**
 * {@link CassandraFactory} to create a local {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class LocalCassandraFactory implements CassandraFactory {

	private static final String SNAKEYAML_YAML_CLASS = "org.yaml.snakeyaml.Yaml";

	private static final RandomPortSupplier PORT_SUPPLIER = new RandomPortSupplier(NetworkUtils::getLocalhost);

	private final List<String> jvmOptions = new ArrayList<>();

	private final List<WorkingDirectoryCustomizer> workingDirectoryCustomizers = new ArrayList<>();

	@Nullable
	private ArtifactFactory artifactFactory;

	@Nullable
	private Version version;

	@Nullable
	private Integer port;

	@Nullable
	private Integer rpcPort;

	@Nullable
	private Integer storagePort;

	@Nullable
	private Integer sslStoragePort;

	@Nullable
	private Integer jmxLocalPort;

	@Nullable
	private Path artifactDirectory;

	@Nullable
	private Path workingDirectory;

	@Nullable
	private Path javaHome;

	@Nullable
	private URL loggingFile;

	@Nullable
	private URL rackFile;

	@Nullable
	private URL topologyFile;

	@Nullable
	private URL configurationFile;

	private boolean allowRoot = true;

	private boolean registerShutdownHook = true;

	private boolean deleteWorkingDirectory = true;

	/**
	 * Allow running Cassandra under {@code root} user.
	 * <p>
	 * This property will be added as {@code -R} to the command line
	 *
	 * @return The value of the {@code allowRoot} attribute
	 * @since 1.2.1
	 */
	public boolean isAllowRoot() {
		return this.allowRoot;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#isAllowRoot()} attribute.
	 *
	 * @param allowRoot The value for allowRoot
	 * @since 1.2.1
	 */
	public void setAllowRoot(boolean allowRoot) {
		this.allowRoot = allowRoot;
	}

	/**
	 * Java home directory.
	 * <p>
	 * This value will be added as {@code $JAVA_HOME} environment variable.
	 *
	 * @return The value of the {@code javaHome} attribute
	 * @since 1.0.9
	 */
	@Nullable
	public Path getJavaHome() {
		return this.javaHome;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getJavaHome()} attribute.
	 *
	 * @param javaHome The value for javaHome
	 * @since 1.0.9
	 */
	public void setJavaHome(@Nullable Path javaHome) {
		this.javaHome = javaHome;
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
	 * Initializes the value for the {@link LocalCassandraFactory#getRackFile()} attribute.
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
	 * Initializes the value for the {@link LocalCassandraFactory#getTopologyFile()} attribute.
	 *
	 * @param topologyFile The value for topologyFile
	 */
	public void setTopologyFile(@Nullable URL topologyFile) {
		this.topologyFile = topologyFile;
	}

	/**
	 * {@link Version} of the Cassandra.
	 *
	 * @return The value of the {@code version} attribute
	 */
	@Nullable
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getVersion()} attribute.
	 *
	 * @param version The value for version
	 */
	public void setVersion(@Nullable Version version) {
		this.version = version;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getVersion()} attribute.
	 *
	 * @param version The value for version
	 * @since 2.0.1
	 */
	public void setVersion(@Nullable String version) {
		setVersion((version != null) ? Version.parse(version) : null);
	}

	/**
	 * {@link ArtifactFactory} that creates a {@link Artifact}.
	 *
	 * @return The value of the {@code artifactFactory} attribute
	 */
	@Nullable
	public ArtifactFactory getArtifactFactory() {
		return this.artifactFactory;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getArtifactFactory()} attribute.
	 *
	 * @param artifactFactory The value for artifactFactory
	 * @see RemoteArtifactFactory
	 */
	public void setArtifactFactory(@Nullable ArtifactFactory artifactFactory) {
		this.artifactFactory = artifactFactory;
	}

	/**
	 * Cassandra directory. This directory keeps data/logs and other files. Default value is {@link
	 * SystemUtils#getTmpDirectory() java.io.tmpdir}{@code /embedded-cassandra/{version}/{UUID}}.
	 *
	 * @return The value of the {@code workingDirectory} attribute
	 */
	@Nullable
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getWorkingDirectory()} attribute.
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
	 * Initializes the value for the {@link LocalCassandraFactory#getConfigurationFile()} attribute.
	 *
	 * @param configurationFile The value for configurationFile
	 */
	public void setConfigurationFile(@Nullable URL configurationFile) {
		this.configurationFile = configurationFile;
	}

	/**
	 * Cassandra logging file.
	 *
	 * @return The value of the {@code logging file} attribute
	 */
	@Nullable
	public URL getLoggingFile() {
		return this.loggingFile;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getLoggingFile()} attribute.
	 *
	 * @param loggingFile The value for logging file
	 */
	public void setLoggingFile(@Nullable URL loggingFile) {
		this.loggingFile = loggingFile;
	}

	/**
	 * Register a shutdown hook with the JVM runtime, stops this {@code cassandra} on JVM shutdown unless it has already
	 * been stopped at that time.
	 *
	 * @return The value of the {@code registerShutdownHook} attribute
	 * @since 1.2.3
	 */
	public boolean isRegisterShutdownHook() {
		return this.registerShutdownHook;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#isRegisterShutdownHook()}
	 * attribute.
	 *
	 * @param registerShutdownHook The value for registerShutdownHook
	 * @since 1.2.3
	 */
	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	/**
	 * Directory to extract an {@link Artifact} (must be writable). Default value is {@link
	 * SystemUtils#getTmpDirectory() java.io.tmpdir}{@code /embedded-cassandra/{version}/apache-cassandra-{version}}.
	 *
	 * @return The value of the {@code artifactDirectory} attribute
	 * @since 1.3.0
	 */
	@Nullable
	public Path getArtifactDirectory() {
		return this.artifactDirectory;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getArtifactDirectory()} attribute.
	 *
	 * @param artifactDirectory The value for artifactDirectory
	 * @since 1.3.0
	 */
	public void setArtifactDirectory(@Nullable Path artifactDirectory) {
		this.artifactDirectory = artifactDirectory;
	}

	/**
	 * Delete the working directory after the successful {@code Cassandra} stop.
	 *
	 * @return The value of the {@code deleteWorkingDirectory} attribute
	 * @since 2.0.0
	 */
	public boolean isDeleteWorkingDirectory() {
		return this.deleteWorkingDirectory;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#isDeleteWorkingDirectory()} attribute.
	 *
	 * @param deleteWorkingDirectory The value for deleteWorkingDirectory
	 * @since 2.0.0
	 */
	public void setDeleteWorkingDirectory(boolean deleteWorkingDirectory) {
		this.deleteWorkingDirectory = deleteWorkingDirectory;
	}

	/**
	 * The native transport port to listen for the clients on.
	 * This value will be added as {@code -Dcassandra.native_transport_port} system property.
	 *
	 * @return native transport port
	 * @since 2.0.0
	 */
	@Nullable
	public Integer getPort() {
		return this.port;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getPort()} attribute.
	 *
	 * @param port The value for port
	 * @since 2.0.0
	 */
	public void setPort(@Nullable Integer port) {
		this.port = port;
	}

	/**
	 * Thrift port for client connections.
	 * This value will be added as {@code -Dcassandra.rpc_port} system property.
	 *
	 * @return the thrift port
	 * @since 2.0.0
	 */
	@Nullable
	public Integer getRpcPort() {
		return this.rpcPort;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getRpcPort()} attribute.
	 *
	 * @param rpcPort The value for rpcPort
	 * @since 2.0.0
	 */
	public void setRpcPort(@Nullable Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	/**
	 * The port for inter-node communication.
	 * This value will be added as {@code -Dcassandra.storage_port} system property.
	 *
	 * @return storage port
	 * @since 2.0.0
	 */
	@Nullable
	public Integer getStoragePort() {
		return this.storagePort;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getStoragePort()} attribute.
	 *
	 * @param storagePort The value for storagePort
	 * @since 2.0.0
	 */
	public void setStoragePort(@Nullable Integer storagePort) {
		this.storagePort = storagePort;
	}

	/**
	 * The ssl port for inter-node communication.
	 * <p>
	 * This value will be added as {@code -Dcassandra.ssl_storage_port} system property.
	 *
	 * @return storage ssl port
	 * @since 2.0.0
	 */
	@Nullable
	public Integer getSslStoragePort() {
		return this.sslStoragePort;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getSslStoragePort()} attribute.
	 *
	 * @param sslStoragePort The value for sslStoragePort
	 * @since 2.0.0
	 */
	public void setSslStoragePort(@Nullable Integer sslStoragePort) {
		this.sslStoragePort = sslStoragePort;
	}

	/**
	 * JMX port to listen on.
	 * <p>
	 * This value will be added as {@code -Dcassandra.jmx.local.port} system property.
	 *
	 * @return The value of the {@code jmxPort} attribute
	 * @since 1.1.1
	 */
	@Nullable
	public Integer getJmxLocalPort() {
		return this.jmxLocalPort;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getJmxLocalPort()} attribute.
	 *
	 * @param jmxLocalPort The value for jmxLocalPort
	 * @since 1.1.1
	 */
	public void setJmxLocalPort(@Nullable Integer jmxLocalPort) {
		this.jmxLocalPort = jmxLocalPort;
	}

	/**
	 * JVM options that should be associated with Cassandra.
	 * <p>
	 * These values will be added as {@code $JVM_EXTRA_OPTS} environment variable.
	 *
	 * @return The value of the {@code jvmOptions} attribute
	 */
	public List<String> getJvmOptions() {
		return this.jvmOptions;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getJvmOptions()} attribute.
	 *
	 * @param jvmOptions the jvm options
	 * @since 2.0.0
	 */
	public void setJvmOptions(String... jvmOptions) {
		this.jvmOptions.clear();
		this.jvmOptions.addAll(Arrays.asList(jvmOptions));
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getJvmOptions()} attribute.
	 *
	 * @param jvmOptions the jvm options
	 * @since 2.0.0
	 */
	public void setJvmOptions(Iterable<String> jvmOptions) {
		this.jvmOptions.clear();
		for (String jvmOption : jvmOptions) {
			this.jvmOptions.add(jvmOption);
		}
	}

	/**
	 * Customizers that should be applied during working directory initialization.
	 *
	 * @return the customizers
	 * @since 2.0.0
	 */
	public List<WorkingDirectoryCustomizer> getWorkingDirectoryCustomizers() {
		return this.workingDirectoryCustomizers;
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getWorkingDirectoryCustomizers()} attribute.
	 *
	 * @param workingDirectoryCustomizers the customizers
	 * @since 2.0.0
	 */
	public void setWorkingDirectoryCustomizers(WorkingDirectoryCustomizer... workingDirectoryCustomizers) {
		this.workingDirectoryCustomizers.clear();
		this.workingDirectoryCustomizers.addAll(Arrays.asList(workingDirectoryCustomizers));
	}

	/**
	 * Initializes the value for the {@link LocalCassandraFactory#getWorkingDirectoryCustomizers()} attribute.
	 *
	 * @param customizers the customizers
	 * @since 2.0.0
	 */
	public void setWorkingDirectoryCustomizers(Iterable<? extends WorkingDirectoryCustomizer> customizers) {
		this.workingDirectoryCustomizers.clear();
		for (WorkingDirectoryCustomizer customizer : customizers) {
			this.workingDirectoryCustomizers.add(customizer);
		}
	}

	@Override
	public Cassandra create() {
		ArtifactFactory artifactFactory = getArtifactFactory();
		if (artifactFactory == null) {
			artifactFactory = new RemoteArtifactFactory();
		}
		Version version = getVersion();
		if (version == null) {
			version = Version.parse("3.11.4");
		}
		Path workingDirectory = getWorkingDirectory();
		if (workingDirectory == null) {
			workingDirectory = getTempDir()
					.resolve(String.format("embedded-cassandra/%s/%s", version, UUID.randomUUID()));
		}
		Path artifactDirectory = getArtifactDirectory();
		if (artifactDirectory == null) {
			artifactDirectory = getTempDir()
					.resolve(String.format("embedded-cassandra/%1$s/apache-cassandra-%1$s", version));
		}
		CassandraNode node = createCassandraNode(workingDirectory, version);
		CassandraDatabase cassandraDatabase = new LocalCassandraDatabase(node, workingDirectory, artifactDirectory,
				artifactFactory, isDeleteWorkingDirectory(), getMergedWorkingDirectoryCustomizers());
		return new LocalCassandra(isRegisterShutdownHook(), cassandraDatabase);
	}

	private Path getTempDir() {
		return SystemUtils.getTmpDirectory()
				.orElseThrow(() -> new IllegalStateException("java.io.tmpdir is not defined."
						+ " Please set java.io.tmpdir system property."));
	}

	private List<WorkingDirectoryCustomizer> getMergedWorkingDirectoryCustomizers() {
		List<WorkingDirectoryCustomizer> customizers = new ArrayList<>();
		Optional.ofNullable(getConfigurationFile()).map(ConfigurationFileCustomizer::new).ifPresent(customizers::add);
		Optional.ofNullable(getRackFile()).map(RackFileCustomizer::new).ifPresent(customizers::add);
		Optional.ofNullable(getTopologyFile()).map(TopologyFileCustomizer::new).ifPresent(customizers::add);
		Optional.ofNullable(getLoggingFile()).map(LoggingFileCustomizer::new).ifPresent(customizers::add);
		if (ClassUtils.isPresent(SNAKEYAML_YAML_CLASS, getClass().getClassLoader())) {
			customizers.add(new ConfigurationFileRandomPortCustomizer(PORT_SUPPLIER));
		}
		if (!SystemUtils.isWindows()) {
			customizers.add(new CassandraFileExecutableCustomizer());
		}
		customizers.addAll(getWorkingDirectoryCustomizers());
		return customizers;
	}

	private CassandraNode createCassandraNode(Path workingDirectory, Version version) {
		Ports ports = new Ports(getPort(), getRpcPort(), getStoragePort(), getSslStoragePort(), getJmxLocalPort());
		JvmOptions jvmOptions = new JvmOptions(getJvmOptions(), ports, PORT_SUPPLIER);
		if (SystemUtils.isWindows()) {
			return new WindowsCassandraNode(version, workingDirectory, getJavaHome(), jvmOptions);
		}
		return new UnixCassandraNode(version, workingDirectory, getJavaHome(), jvmOptions, isAllowRoot());
	}

}
