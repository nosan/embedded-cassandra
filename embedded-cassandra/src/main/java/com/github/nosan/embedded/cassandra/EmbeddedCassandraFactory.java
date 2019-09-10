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

package com.github.nosan.embedded.cassandra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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
 * <p><strong>Exposed properties:</strong>
 * The following properties will be exposed as {@code System Properties} after {@link Cassandra} has started:
 * <pre>
 *     - embedded.cassandra.version
 *     - embedded.cassandra.address
 *     - embedded.cassandra.port
 *     - embedded.cassandra.ssl-port
 *     - embedded.cassandra.rpc-port
 * </pre>
 * <p>{@link #setExposeProperties(boolean)} can be used to disable properties exposing.
 *
 * @author Dmytro Nosan
 * @see Cassandra
 * @see CassandraFactory
 * @since 3.0.0
 */
public final class EmbeddedCassandraFactory implements CassandraFactory {

	private static final AtomicLong number = new AtomicLong();

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>(
			Collections.singletonMap("JAVA_HOME", javaHome()));

	private final List<String> jvmOptions = new ArrayList<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> configProperties = new LinkedHashMap<>();

	private boolean rootAllowed = true;

	private boolean daemon = true;

	private Logger logger = LoggerFactory.getLogger(Cassandra.class);

	private boolean registerShutdownHook = true;

	private boolean exposeProperties = true;

	private Duration timeout = Duration.ofSeconds(90);

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
		Object javaHome = getEnvironmentVariables().get("JAVA_HOME");
		return (javaHome != null) ? Paths.get(javaHome.toString()) : null;
	}

	/**
	 * Sets the path to java home.
	 * <p>
	 * Alias for {@link #getEnvironmentVariables()}{@code .put("JAVA_HOME", javaHome)}
	 *
	 * @param javaHome path to the java home
	 */
	public void setJavaHome(@Nullable Path javaHome) {
		getEnvironmentVariables().put("JAVA_HOME", javaHome);
	}

	/**
	 * Returns {@link Logger} for {@code Cassandra's} output.
	 *
	 * @return the logger
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Sets {@link Logger} for {@code Cassandra's} output. Defaults to {@code LoggerFactory.getLogger(Cassandra.class)}.
	 *
	 * @param logger the logger
	 */
	public void setLogger(Logger logger) {
		this.logger = Objects.requireNonNull(logger, "'logger' must not be null");
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
	public Duration getTimeout() {
		return this.timeout;
	}

	/**
	 * Sets Cassandra's startup timeout.
	 *
	 * @param timeout startup timeout (must be positive)
	 */
	public void setTimeout(Duration timeout) {
		Objects.requireNonNull(timeout, "'timeout' must not be null");
		if (timeout.isNegative() || timeout.isZero()) {
			throw new IllegalArgumentException("'" + timeout + "' must be positive");
		}
		this.timeout = timeout;
	}

	/**
	 * Returns the native transport port ({@code cassandra.native_transport_port}).
	 *
	 * @return the port
	 */
	@Nullable
	public Integer getPort() {
		Object port = getSystemProperties().get("cassandra.native_transport_port");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the native transport port ({@code cassandra.native_transport_port}).
	 * <p>
	 * Alias for {@link #getSystemProperties()}{@code .put("cassandra.native_transport_port", port)}
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setPort(@Nullable Integer port) {
		getSystemProperties().put("cassandra.native_transport_port", port);
	}

	/**
	 * Returns the native transport SSL port ({@code native_transport_port_ssl}).
	 *
	 * @return the SSL port (or null if none)
	 */
	@Nullable
	public Integer getSslPort() {
		Object port = getConfigProperties().get("native_transport_port_ssl");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the native transport SSL port ({@code native_transport_port_ssl}).
	 * <p>
	 * Alias for {@link #getConfigProperties()}{@code .put("native_transport_port_ssl", port)}
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslPort(@Nullable Integer port) {
		getConfigProperties().put("native_transport_port_ssl", port);
	}

	/**
	 * Returns the RPC transport port ({@code cassandra.rpc_port}).
	 *
	 * @return the RPC port (or null if none)
	 */
	@Nullable
	public Integer getRpcPort() {
		Object port = getSystemProperties().get("cassandra.rpc_port");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the RPC transport port ({@code cassandra.rpc_port}).
	 * <p>
	 * Alias for {@link #getSystemProperties()}{@code .put("cassandra.rpc_port", port)}
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setRpcPort(@Nullable Integer port) {
		getSystemProperties().put("cassandra.rpc_port", port);
	}

	/**
	 * Returns the storage port ({@code cassandra.storage_port}).
	 *
	 * @return the storage port (or null if none)
	 */
	@Nullable
	public Integer getStoragePort() {
		Object port = getSystemProperties().get("cassandra.storage_port");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the storage port ({@code cassandra.storage_port}).
	 * <p>
	 * Alias for {@link #getSystemProperties()}{@code .put("cassandra.storage_port", port)}
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setStoragePort(@Nullable Integer port) {
		getSystemProperties().put("cassandra.storage_port", port);
	}

	/**
	 * Returns the storage SSL port ({@code cassandra.ssl_storage_port}).
	 *
	 * @return the storage SSL port (or null if none)
	 */
	@Nullable
	public Integer getSslStoragePort() {
		Object port = getSystemProperties().get("cassandra.ssl_storage_port");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the storage SSL port ({@code cassandra.ssl_storage_port}).
	 * <p>
	 * Alias for {@link #getSystemProperties()}{@code .put("cassandra.ssl_storage_port", port)}
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslStoragePort(@Nullable Integer port) {
		getSystemProperties().put("cassandra.ssl_storage_port", port);
	}

	/**
	 * Returns the JMX local port ({@code cassandra.jmx.local.port}).
	 *
	 * @return the jmx local port (or null if none)
	 */
	@Nullable
	public Integer getJmxLocalPort() {
		Object port = getSystemProperties().get("cassandra.jmx.local.port");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the JMX local port ({@code cassandra.jmx.local.port}).
	 * <p>
	 * Alias for {@link #getSystemProperties()}{@code .put("cassandra.jmx.local.port", port)}
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setJmxLocalPort(@Nullable Integer port) {
		getSystemProperties().put("cassandra.jmx.local.port", port);
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

	/**
	 * Whether {@link Cassandra} properties such as {@code embedded.cassandra.port} should be exposed as {@code System
	 * Properties} or not.
	 *
	 * @return {@code true} if properties should be exposed
	 */
	public boolean isExposeProperties() {
		return this.exposeProperties;
	}

	/**
	 * Sets if the created Cassandra should expose its properties as System Properties. Defaults to {@code true}.
	 *
	 * @param exposeProperties if the properties should be exposed
	 */
	public void setExposeProperties(boolean exposeProperties) {
		this.exposeProperties = exposeProperties;
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
			name = "cassandra-" + number.incrementAndGet();
		}
		Artifact artifact = getArtifact();
		if (artifact == null) {
			artifact = Artifact.ofVersion("3.11.4");
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
		Node node = createNode(version, workingDirectory);
		Database database = new CassandraDatabase(name, version, directory, workingDirectory, isDaemon(),
				getLogger(), getTimeout(), getConfig(), getRackConfig(), getTopologyConfig(), node);
		EmbeddedCassandra cassandra = new EmbeddedCassandra(name, version, isExposeProperties(), database);
		if (isRegisterShutdownHook()) {
			Runtime.getRuntime().addShutdownHook(new Thread(cassandra::stop, name + "-sh"));
		}
		return cassandra;
	}

	private Node createNode(Version version, Path workingDirectory) {
		Map<String, Object> systemProperties = new LinkedHashMap<>(getSystemProperties());
		systemProperties.entrySet().removeIf(
				entry -> Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue()));
		Map<String, Object> environmentVariables = new LinkedHashMap<>(getEnvironmentVariables());
		environmentVariables.entrySet().removeIf(
				entry -> Objects.isNull(entry.getKey()) || Objects.isNull(entry.getValue()));
		List<String> jvmOptions = new ArrayList<>(getJvmOptions());
		jvmOptions.removeIf(Objects::isNull);
		LinkedHashMap<String, Object> configProperties = new LinkedHashMap<>(getConfigProperties());
		configProperties.keySet().removeIf(Objects::isNull);

		if (isWindows()) {
			return new WindowsNode(version, workingDirectory, jvmOptions, systemProperties, environmentVariables,
					configProperties);
		}
		return new UnixNode(version, workingDirectory, jvmOptions, systemProperties, environmentVariables,
				configProperties, isRootAllowed());
	}

	@Nullable
	private static Path javaHome() {
		return Optional.ofNullable(System.getProperty("java.home")).map(Paths::get).orElse(null);
	}

	private static boolean isWindows() {
		String name = System.getProperty("os.name");
		if (name == null) {
			throw new IllegalStateException("System Property 'os.name' is not defined");
		}
		return name.toLowerCase(Locale.ENGLISH).contains("windows");
	}

}
