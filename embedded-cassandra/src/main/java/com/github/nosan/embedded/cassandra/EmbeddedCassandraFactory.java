/*
 * Copyright 2018-2020 the original author or authors.
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

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraCreationException;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.artifact.ArchiveArtifact;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.artifact.DefaultArtifact;
import com.github.nosan.embedded.cassandra.artifact.RemoteArtifact;
import com.github.nosan.embedded.cassandra.commons.io.Resource;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * {@link CassandraFactory} that can be used to create and configure an {@code EmbeddedCassandra}. {@code
 * EmbeddedCassandra} runs Apache Cassandra as a separate {@link Process}.
 * <p><strong>Example:</strong>
 * <pre>
 * class Scratch {
 *
 *   public static void main(String[] args) {
 *     EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
 *     Cassandra cassandra = cassandraFactory.create();
 *     cassandra.start();
 *     try {
 *       // ...
 *     }
 *     finally {
 *       cassandra.stop();
 *     }
 *   }
 *
 * }
 * </pre>
 * <p><strong>Configuration:</strong>
 * <p>
 * <em>{@link #getSystemProperties()}</em> used to set system properties that should be associated with {@code
 * Cassandra}, e.g. {@code 'cassandra.start_rpc=true'}.
 * <p>
 * <em>{@link #getEnvironmentVariables()}</em> used to set environment variables that should be associated with
 * {@code Cassandra}, e.g. {@code 'JAVA_HOME=<PATH>'}.
 * <p>
 * <em>{@link #getJvmOptions()}</em> used to set jvm options that should be
 * associated with {@code Cassandra}, e.g. {@code '-Xmx128m -Xms64m'}.
 * <p>
 * <em>{@link #getConfigProperties()}</em>used to set {@code
 * cassandra} properties (cassandra.yaml) that should be associated with {@code Cassandra}, e.g. {@code
 * 'start_rpc=true'}.
 * <p><strong>Ports:</strong>
 * {@code EmbeddedCassandra} is running on default ports. There are several methods that can be used to set ports, such
 * as {@link #setPort(Integer)}.
 * <p> Use {@code '0'} for a random port.
 *
 * @author Dmytro Nosan
 * @see Cassandra
 * @see CassandraFactory
 * @see EmbeddedCassandraBuilder
 * @since 3.0.0
 */
public final class EmbeddedCassandraFactory implements CassandraFactory {

	private static final AtomicLong NUMBER = new AtomicLong();

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private final List<String> jvmOptions = new ArrayList<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> configProperties = new LinkedHashMap<>();

	private boolean rootAllowed = true;

	private boolean daemon = true;

	private boolean registerShutdownHook = true;

	@Nullable
	private Logger logger;

	@Nullable
	private Duration timeout;

	@Nullable
	private Path javaHome;

	@Nullable
	private String name;

	@Nullable
	private Artifact artifact;

	@Nullable
	private Resource config;

	@Nullable
	private Resource rackConfig;

	@Nullable
	private Resource topologyConfig;

	@Nullable
	private Path workingDirectory;

	@Nullable
	private Integer port;

	@Nullable
	private Integer sslPort;

	@Nullable
	private Integer storagePort;

	@Nullable
	private Integer sslStoragePort;

	@Nullable
	private Integer rpcPort;

	@Nullable
	private Integer jmxLocalPort;

	@Nullable
	private InetAddress address;

	/**
	 * Returns Cassandra's name. Defaults to {@code 'cassandra'}.
	 *
	 * @return name of Cassandra's instance
	 */
	@Nullable
	public String getName() {
		return this.name;
	}

	/**
	 * Sets Cassandra's name.
	 *
	 * @param name name of Cassandra's instance
	 */
	public void setName(@Nullable String name) {
		this.name = name;
	}

	/**
	 * Returns Cassandra's working directory.
	 *
	 * @return the working directory
	 */
	@Nullable
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	/**
	 * Sets Cassandra's working directory. This directory will be initialized on start and <strong>deleted</strong> on
	 * stop. Defaults to {@code tmp dir}.
	 *
	 * @param workingDirectory the working directory
	 */
	public void setWorkingDirectory(@Nullable Path workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Returns an artifact that used to provide a path to Cassandra's directory and version.
	 *
	 * @return an artifact
	 */
	@Nullable
	public Artifact getArtifact() {
		return this.artifact;
	}

	/**
	 * Sets {@link Artifact} that provides a path to Cassandra's directory and Cassandra's version.
	 *
	 * @param artifact an artifact
	 * @see Artifact#ofVersion(String)
	 * @see Artifact#ofVersion(Version)
	 * @see RemoteArtifact
	 * @see ArchiveArtifact
	 * @see DefaultArtifact
	 */
	public void setArtifact(@Nullable Artifact artifact) {
		this.artifact = artifact;
	}

	/**
	 * Returns the path to java home.
	 *
	 * @return the path to java home.
	 */
	@Nullable
	public Path getJavaHome() {
		return this.javaHome;
	}

	/**
	 * Sets the path to java home.
	 *
	 * @param javaHome path to the java home
	 */
	public void setJavaHome(@Nullable Path javaHome) {
		this.javaHome = javaHome;
	}

	/**
	 * Returns {@link Logger} for {@code Cassandra's} output.
	 *
	 * @return the logger
	 */
	@Nullable
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Sets {@link Logger} for {@code Cassandra's} output. Defaults to {@code LoggerFactory.getLogger(Cassandra.class)}.
	 *
	 * @param logger the logger
	 */
	public void setLogger(@Nullable Logger logger) {
		this.logger = logger;
	}

	/**
	 * Whether the thread which reads Cassandra's output should be a daemon or not.
	 *
	 * @return the daemon or not
	 */
	public boolean isDaemon() {
		return this.daemon;
	}

	/**
	 * Sets if the thread which reads Cassandra's output should be a daemon or not. Defaults to {@code true}.
	 *
	 * @param daemon if {@code Cassandra's} thread should be a daemon
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * Whether the {@code root} user is able to start Cassandra or not.
	 *
	 * @return the root allowed or not
	 */
	public boolean isRootAllowed() {
		return this.rootAllowed;
	}

	/**
	 * Sets if the {@link Cassandra} can be run under the root user. Defaults to  {@code true}.
	 *
	 * @param rootAllowed if the root user can run {@link Cassandra}
	 */
	public void setRootAllowed(boolean rootAllowed) {
		this.rootAllowed = rootAllowed;
	}

	/**
	 * JVM options that should be passed to Cassandra's process.
	 *
	 * @return the jvm options
	 */
	public List<String> getJvmOptions() {
		return this.jvmOptions;
	}

	/**
	 * System properties that should be passed to Cassandra's process.
	 *
	 * @return the system properties
	 */
	public Map<String, Object> getSystemProperties() {
		return this.systemProperties;
	}

	/**
	 * Environment variables that should be passed to Cassandra's process.
	 *
	 * @return the environment variables
	 */
	public Map<String, Object> getEnvironmentVariables() {
		return this.environmentVariables;
	}

	/**
	 * These properties replace any properties in ({@code cassandra.yaml}}.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getConfigProperties() {
		return this.configProperties;
	}

	/**
	 * Returns Cassandra's startup timeout. Defaults to {@code 90 seconds}.
	 *
	 * @return the timeout (always positive)
	 */
	@Nullable
	public Duration getTimeout() {
		return this.timeout;
	}

	/**
	 * Sets Cassandra's startup timeout.
	 *
	 * @param timeout startup timeout (must be positive)
	 */
	public void setTimeout(@Nullable Duration timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the native transport port ({@code cassandra.native_transport_port}).
	 *
	 * @return the port
	 */
	@Nullable
	public Integer getPort() {
		return this.port;
	}

	/**
	 * Sets the native transport port ({@code cassandra.native_transport_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setPort(@Nullable Integer port) {
		this.port = port;
	}

	/**
	 * Returns the native transport SSL port ({@code native_transport_port_ssl}).
	 *
	 * @return the SSL port (or null if none)
	 */
	@Nullable
	public Integer getSslPort() {
		return this.sslPort;
	}

	/**
	 * Sets the native transport SSL port ({@code native_transport_port_ssl}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslPort(@Nullable Integer port) {
		this.sslPort = port;
	}

	/**
	 * Returns the RPC transport port ({@code cassandra.rpc_port}).
	 *
	 * @return the RPC port (or null if none)
	 */
	@Nullable
	public Integer getRpcPort() {
		return this.rpcPort;
	}

	/**
	 * Sets the RPC transport port ({@code cassandra.rpc_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setRpcPort(@Nullable Integer port) {
		this.rpcPort = port;
	}

	/**
	 * Returns the storage port ({@code cassandra.storage_port}).
	 *
	 * @return the storage port (or null if none)
	 */
	@Nullable
	public Integer getStoragePort() {
		return this.storagePort;
	}

	/**
	 * Sets the storage port ({@code cassandra.storage_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setStoragePort(@Nullable Integer port) {
		this.storagePort = port;
	}

	/**
	 * Returns the storage SSL port ({@code cassandra.ssl_storage_port}).
	 *
	 * @return the storage SSL port (or null if none)
	 */
	@Nullable
	public Integer getSslStoragePort() {
		return this.sslStoragePort;
	}

	/**
	 * Sets the storage SSL port ({@code cassandra.ssl_storage_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslStoragePort(@Nullable Integer port) {
		this.sslStoragePort = port;
	}

	/**
	 * Returns the JMX local port ({@code cassandra.jmx.local.port}).
	 *
	 * @return the jmx local port (or null if none)
	 */
	@Nullable
	public Integer getJmxLocalPort() {
		return this.jmxLocalPort;
	}

	/**
	 * Sets the JMX local port ({@code cassandra.jmx.local.port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setJmxLocalPort(@Nullable Integer port) {
		this.jmxLocalPort = port;
	}

	/**
	 * Returns the RPC address ({@code rpc_address}).
	 *
	 * @return the address
	 */
	@Nullable
	public InetAddress getAddress() {
		return this.address;
	}

	/**
	 * Sets the RPC address ({@code rpc_address}). The address to bind the Thrift RPC service and native transport.
	 *
	 * @param address the address
	 */
	public void setAddress(@Nullable InetAddress address) {
		this.address = address;
	}

	/**
	 * Returns the Cassandra's config ({@code conf/cassandra.yaml}).
	 *
	 * @return the config
	 */
	@Nullable
	public Resource getConfig() {
		return this.config;
	}

	/**
	 * Sets the Cassandra's config. Replaces {@code conf/cassandra.yaml} in the working directory.
	 *
	 * @param config the config
	 * @see Resource
	 */
	public void setConfig(@Nullable Resource config) {
		this.config = config;
	}

	/**
	 * Returns the Cassandra's rack config ({@code conf/cassandra-rackdc.properties}).
	 *
	 * @return the rack config (or null if none)
	 */
	@Nullable
	public Resource getRackConfig() {
		return this.rackConfig;
	}

	/**
	 * Sets the Cassandra's rack config. Replaces {@code conf/cassandra-rackdc.properties} in the working directory.
	 *
	 * @param rackConfig the rack config
	 */
	public void setRackConfig(@Nullable Resource rackConfig) {
		this.rackConfig = rackConfig;
	}

	/**
	 * Returns the Cassandra's topology config ({@code conf/cassandra-topology.properties}).
	 *
	 * @return the topology config (or null if none)
	 */
	@Nullable
	public Resource getTopologyConfig() {
		return this.topologyConfig;
	}

	/**
	 * Sets the Cassandra's topology config. Replaces {@code conf/cassandra-topology.properties} in the working
	 * directory.
	 *
	 * @param topologyConfig the topology config
	 * @see Resource
	 */
	public void setTopologyConfig(@Nullable Resource topologyConfig) {
		this.topologyConfig = topologyConfig;
	}

	/**
	 * Whether shutdown hook should be registered or not.
	 *
	 * @return {@code true} if shutdown hook should be registered
	 */
	public boolean isRegisterShutdownHook() {
		return this.registerShutdownHook;
	}

	/**
	 * Sets if the created {@link Cassandra} should have a shutdown hook registered. Defaults to {@code true} to ensure
	 * that {@link Cassandra} will be stopped.
	 *
	 * @param registerShutdownHook if the shutdown hook should be registered
	 */
	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	@Override
	public Cassandra create() throws CassandraCreationException {
		try {
			return doCreate();
		}
		catch (Exception ex) {
			throw new CassandraCreationException("Cassandra instance cannot be created", ex);
		}
	}

	private Cassandra doCreate() throws Exception {
		String name = getName();
		if (!StringUtils.hasText(name)) {
			name = "cassandra-" + NUMBER.incrementAndGet();
		}
		Artifact artifact = getArtifact();
		if (artifact == null) {
			artifact = Artifact.ofVersion("4.0-beta1");
		}
		Artifact.Distribution distribution = artifact.getDistribution();
		Version version = distribution.getVersion();
		Path workingDirectory = getWorkingDirectory();
		if (workingDirectory == null) {
			workingDirectory = Files.createTempDirectory("apache-cassandra-" + version + "-");
		}
		if (Files.exists(workingDirectory) && !Files.isDirectory(workingDirectory)) {
			throw new IllegalArgumentException(workingDirectory + " is not a directory");
		}
		Path directory = distribution.getDirectory();
		if (!Files.exists(directory)) {
			throw new IllegalStateException(directory + " does not exist");
		}
		if (!Files.isDirectory(directory)) {
			throw new IllegalStateException(directory + " is not a directory");
		}
		Logger logger = getLogger();
		if (logger == null) {
			logger = LoggerFactory.getLogger(Cassandra.class);
		}
		Duration timeout = getTimeout();
		if (timeout == null || timeout.toMillis() <= 0) {
			timeout = Duration.ofSeconds(90);
		}
		CassandraNode node = createNode(version, workingDirectory);
		CassandraDatabase database = new EmbeddedCassandraDatabase(name, version, directory, workingDirectory,
				isDaemon(), logger, timeout, getConfig(), getRackConfig(), getTopologyConfig(), node);
		EmbeddedCassandra cassandra = new EmbeddedCassandra(name, version, database);
		if (isRegisterShutdownHook()) {
			Runtime.getRuntime().addShutdownHook(new Thread(cassandra::stop, name + "-sh"));
		}
		return cassandra;
	}

	private CassandraNode createNode(Version version, Path workingDirectory) {
		Map<String, Object> systemProperties = new LinkedHashMap<>(getSystemProperties());
		systemProperties.keySet().removeIf(Objects::isNull);
		Map<String, Object> environmentVariables = new LinkedHashMap<>(getEnvironmentVariables());
		environmentVariables.keySet().removeIf(Objects::isNull);
		List<String> jvmOptions = new ArrayList<>(getJvmOptions());
		jvmOptions.removeIf(Objects::isNull);
		LinkedHashMap<String, Object> configProperties = new LinkedHashMap<>(getConfigProperties());
		configProperties.keySet().removeIf(Objects::isNull);
		Path javaHome = Optional.ofNullable(getJavaHome())
				.orElseGet(() -> Optional.ofNullable(System.getProperty("java.home")).map(Paths::get).orElse(null));
		if (javaHome != null) {
			environmentVariables.put("JAVA_HOME", javaHome);
		}
		Integer port = getPort();
		if (port != null) {
			systemProperties.put("cassandra.native_transport_port", port);
		}
		Integer sslPort = getSslPort();
		if (sslPort != null) {
			configProperties.put("native_transport_port_ssl", sslPort);
		}
		InetAddress address = getAddress();
		if (address != null) {
			configProperties.put("rpc_address", address.getHostAddress());
		}
		Integer rpcPort = getRpcPort();
		if (rpcPort != null) {
			systemProperties.put("cassandra.rpc_port", rpcPort);
		}
		Integer storagePort = getStoragePort();
		if (storagePort != null) {
			systemProperties.put("cassandra.storage_port", storagePort);
		}
		Integer sslStoragePort = getSslStoragePort();
		if (sslStoragePort != null) {
			systemProperties.put("cassandra.ssl_storage_port", sslStoragePort);
		}
		Integer jmxLocalPort = getJmxLocalPort();
		if (jmxLocalPort != null) {
			systemProperties.put("cassandra.jmx.local.port", jmxLocalPort);
		}
		if (isWindows()) {
			return new WindowsCassandraNode(version, workingDirectory, jvmOptions, systemProperties,
					environmentVariables, configProperties);
		}
		return new UnixCassandraNode(version, workingDirectory, jvmOptions, systemProperties, environmentVariables,
				configProperties, isRootAllowed());
	}

	private static boolean isWindows() {
		String name = System.getProperty("os.name");
		if (name == null) {
			throw new IllegalStateException("System Property 'os.name' is not defined");
		}
		return name.toLowerCase(Locale.ENGLISH).contains("windows");
	}

}
