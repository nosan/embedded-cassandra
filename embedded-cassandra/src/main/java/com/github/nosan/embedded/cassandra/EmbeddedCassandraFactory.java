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
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import com.github.nosan.embedded.cassandra.commons.util.FileUtils;
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
 *     cassandraFactory.setArtifact(Artifact.of("3.11.4"));
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
 * <p><strong>Properties:</strong>
 * This factory provides a way to configure {@code systemProperties}, {@code environmentVariables}, {@code jvmOptions}
 * and {@code properties}.
 * <p>
 * <em>{@code -systemProperties:}</em> used to set system properties that should be associated with {@code
 * Cassandra}, e.g. {@code 'cassandra.start_rpc=true'}.
 * <p>
 * <em>{@code -environmentVariables:}</em> used to set environment variables that should be associated with
 * {@code Cassandra}, e.g. {@code 'JAVA_HOME=<PATH>'}.
 * <p>
 * <em>{@code -jvmOptions:}</em>used to set jvm options that should be
 * associated with {@code Cassandra}, e.g. {@code '-Xmx128m -Xms64m'}.
 * <p>
 * <em>{@code -properties:}</em>used to set {@code
 * cassandra} properties (cassandra.yaml) that should be associated with {@code Cassandra}, e.g. {@code
 * 'start_rpc=true'}.
 * <p><strong>Ports:</strong>
 * {@code EmbeddedCassandra} is running on default ports. There are several methods that can be used to set ports, such
 * as {@link #setPort(Integer)}.
 * <p> Use {@code '0'} for a random port.
 * instance with preconfigured random ports.
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

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private final List<String> jvmOptions = new ArrayList<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> properties = new LinkedHashMap<>();

	private boolean rootAllowed = true;

	private boolean daemon = true;

	private Logger logger = LoggerFactory.getLogger(Cassandra.class);

	private boolean registerShutdownHook = true;

	private boolean exposeProperties = true;

	private Duration timeout = Duration.ofSeconds(90);

	@Nullable
	private Path javaHome = FileUtils.getJavaHome();

	@Nullable
	private String name;

	@Nullable
	private Artifact artifact;

	@Nullable
	private Resource config;

	@Nullable
	private Path workingDirectory;

	@Nullable
	private Integer port;

	@Nullable
	private Integer sslPort;

	@Nullable
	private Integer rpcPort;

	@Nullable
	private Integer storagePort;

	@Nullable
	private Integer sslStoragePort;

	@Nullable
	private Integer jmxLocalPort;

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
	 * @see Artifact#of(String)
	 * @see Artifact#of(Version)
	 * @see RemoteArtifact
	 * @see ArchiveArtifact
	 * @see DefaultArtifact
	 */
	public void setArtifact(@Nullable Artifact artifact) {
		this.artifact = artifact;
	}

	/**
	 * Returns the path to java directory. Defaults to {@code java.home}.
	 *
	 * @return the path to java directory
	 */
	@Nullable
	public Path getJavaHome() {
		return this.javaHome;
	}

	/**
	 * Sets the path to java directory.
	 *
	 * @param javaHome the path to java directory
	 */
	public void setJavaHome(@Nullable Path javaHome) {
		this.javaHome = javaHome;
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
	 * Returns Cassandra's properties ({@code cassandra.yaml}). These properties replace properties in ({@code
	 * cassandra.yaml}}.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return this.properties;
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
	 * Returns the native transport port ({@code native_transport_port}).
	 *
	 * @return the port
	 */
	@Nullable
	public Integer getPort() {
		return this.port;
	}

	/**
	 * Sets the native transport port ({@code native_transport_port}).
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
	 * @param sslPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslPort(@Nullable Integer sslPort) {
		this.sslPort = sslPort;
	}

	/**
	 * Returns the RPC transport port ({@code rpc_port}).
	 *
	 * @return the RPC port (or null if none)
	 */
	@Nullable
	public Integer getRpcPort() {
		return this.rpcPort;
	}

	/**
	 * Sets the RPC transport port ({@code rpc_port}).
	 *
	 * @param rpcPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setRpcPort(@Nullable Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	/**
	 * Returns the storage port ({@code storage_port}).
	 *
	 * @return the storage port (or null if none)
	 */
	@Nullable
	public Integer getStoragePort() {
		return this.storagePort;
	}

	/**
	 * Sets the storage port ({@code storage_port}).
	 *
	 * @param storagePort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setStoragePort(@Nullable Integer storagePort) {
		this.storagePort = storagePort;
	}

	/**
	 * Returns the storage SSL port ({@code storage_ssl_port}).
	 *
	 * @return the storage SSL port (or null if none)
	 */
	@Nullable
	public Integer getSslStoragePort() {
		return this.sslStoragePort;
	}

	/**
	 * Sets the storage SSL port ({@code ssl_storage_port}).
	 *
	 * @param sslStoragePort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslStoragePort(@Nullable Integer sslStoragePort) {
		this.sslStoragePort = sslStoragePort;
	}

	/**
	 * Returns the JMX local port ({@code -Dcassandra.jmx.local.port}).
	 *
	 * @return the jmx local port (or null if none)
	 */
	@Nullable
	public Integer getJmxLocalPort() {
		return this.jmxLocalPort;
	}

	/**
	 * Sets the JMX local port ({@code -Dcassandra.jmx.local.port}).
	 *
	 * @param jmxLocalPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setJmxLocalPort(@Nullable Integer jmxLocalPort) {
		this.jmxLocalPort = jmxLocalPort;
	}

	/**
	 * Returns the Cassandra's configuration file ({@code cassandra.yaml}).
	 *
	 * @return the configuration file (or null if none)
	 */
	@Nullable
	public Resource getConfig() {
		return this.config;
	}

	/**
	 * Sets the Cassandra's configuration file.
	 *
	 * @param config the configuration file ({@code cassandra.yaml})
	 * @see Resource
	 */
	public void setConfig(@Nullable Resource config) {
		this.config = config;
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
			artifact = Artifact.of("3.11.4");
		}
		Artifact.Descriptor descriptor = artifact.getDescriptor();
		Version version = descriptor.getVersion();
		Path workingDirectory = getWorkingDirectory();
		if (workingDirectory == null) {
			workingDirectory = Files.createTempDirectory("apache-cassandra-" + version + "-");
		}
		if (Files.exists(workingDirectory) && !Files.isDirectory(workingDirectory)) {
			throw new IllegalArgumentException(workingDirectory + " is not a directory");
		}
		Path artifactDirectory = descriptor.getDirectory();
		if (!Files.exists(artifactDirectory)) {
			throw new IllegalStateException(artifactDirectory + " does not exist");
		}
		if (!Files.isDirectory(artifactDirectory)) {
			throw new IllegalStateException(artifactDirectory + " is not a directory");
		}
		Node node = createNode(version, workingDirectory);
		Database database = new DefaultDatabase(name, version, isDaemon(), getLogger(), getTimeout(), node);
		EmbeddedCassandra cassandra = new EmbeddedCassandra(name, isExposeProperties(), artifactDirectory,
				workingDirectory, version, database);
		if (isRegisterShutdownHook()) {
			Runtime.getRuntime().addShutdownHook(new Thread(cassandra::stop, name + "-sh"));
		}
		return cassandra;
	}

	private Node createNode(Version version, Path workingDirectory) {
		Map<String, Object> systemProperties = new LinkedHashMap<>(getSystemProperties());
		Integer port = getPort();
		if (port != null) {
			systemProperties.put("cassandra.native_transport_port", port);
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
		Map<String, Object> properties = new LinkedHashMap<>(getProperties());
		Integer sslPort = getSslPort();
		if (sslPort != null) {
			properties.put("native_transport_port_ssl", sslPort);
		}
		Integer jmxLocalPort = getJmxLocalPort();
		if (jmxLocalPort != null) {
			systemProperties.put("cassandra.jmx.local.port", jmxLocalPort);
		}
		Map<String, Object> environmentVariables = new LinkedHashMap<>(getEnvironmentVariables());
		Path javaHome = getJavaHome();
		if (javaHome != null) {
			environmentVariables.put("JAVA_HOME", javaHome);
		}
		if (isWindows()) {
			return new WindowsNode(version, workingDirectory, getConfig(), getJvmOptions(), systemProperties,
					environmentVariables, properties);
		}
		return new UnixNode(version, workingDirectory, getConfig(), getJvmOptions(), systemProperties,
				environmentVariables, properties, isRootAllowed());
	}

	private static boolean isWindows() {
		String name = System.getProperty("os.name");
		if (name == null) {
			throw new IllegalStateException("System Property 'os.name' is not defined");
		}
		return name.toLowerCase(Locale.ENGLISH).contains("windows");
	}

}
