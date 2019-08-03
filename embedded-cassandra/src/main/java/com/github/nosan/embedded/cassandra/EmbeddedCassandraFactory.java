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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.github.nosan.embedded.cassandra.commons.DefaultPathSupplier;
import com.github.nosan.embedded.cassandra.commons.EnvironmentPathSupplier;
import com.github.nosan.embedded.cassandra.commons.PathSupplier;
import com.github.nosan.embedded.cassandra.commons.SystemPathSupplier;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;
import com.github.nosan.embedded.cassandra.commons.util.SystemUtils;

/**
 * {@link CassandraFactory} that can be used to create and configure an {@code EmbeddedCassandra}. {@code
 * EmbeddedCassandra} runs Apache Cassandra as a separate {@link Process}. Working directory will be initialized using
 * the specified {@link Artifact}.
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
 * as {@link #setPort(int)}.
 * <p> Use {@code '0'} for a random port.
 * <p><strong>Exposed properties:</strong>
 * The following properties will be exposed after {@link Cassandra} has started:
 * <pre>
 *     - embedded.cassandra.version
 *     - embedded.cassandra.address
 *     - embedded.cassandra.port
 *     - embedded.cassandra.ssl-port
 *     - embedded.cassandra.rpc-port
 * </pre>
 * TIP: {@link #setExposedPropertiesPrefix(String)} can be used to add a prefix before property names.
 *
 * @author Dmytro Nosan
 * @see Cassandra
 * @see CassandraFactory
 * @since 3.0.0
 */
public class EmbeddedCassandraFactory implements CassandraFactory {

	private static final AtomicLong number = new AtomicLong();

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private final List<String> jvmOptions = new ArrayList<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> properties = new LinkedHashMap<>();

	private boolean rootAllowed = true;

	private boolean daemon = true;

	private PathSupplier javaHome = new SystemPathSupplier("java.home");

	private Logger logger = LoggerFactory.getLogger(Cassandra.class);

	private boolean registerShutdownHook = true;

	private Duration timeout = Duration.ofSeconds(90);

	@Nullable
	private String name;

	@Nullable
	private Artifact artifact;

	@Nullable
	private Path workingDirectory;

	@Nullable
	private String exposedPropertiesPrefix;

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
	public void setName(String name) {
		this.name = Objects.requireNonNull(name, "'name' must not be null");
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
	 * Sets Cassandra's working directory. This directory will be initialized on start and deleted on stop. Defaults to
	 * {@code temporary dir}.
	 *
	 * @param workingDirectory the working directory
	 */
	public void setWorkingDirectory(Path workingDirectory) {
		this.workingDirectory = Objects.requireNonNull(workingDirectory, "'workingDirectory' must not be null");
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
	public void setArtifact(Artifact artifact) {
		this.artifact = Objects.requireNonNull(artifact, "'artifact' must not be null");
	}

	/**
	 * Returns the java's directory provider.
	 *
	 * @return the java provider
	 */
	public PathSupplier getJavaHome() {
		return this.javaHome;
	}

	/**
	 * Sets the java home path. Defaults to {@link SystemPathSupplier}.
	 *
	 * @param javaHome the java home supplier
	 * @see DefaultPathSupplier
	 * @see EnvironmentPathSupplier
	 * @see SystemPathSupplier
	 */
	public void setJavaHome(PathSupplier javaHome) {
		this.javaHome = Objects.requireNonNull(javaHome, "'javaHome' must not be null");
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
		Object port = getSystemProperties().get("cassandra.native_transport_port");
		if (port == null) {
			port = getProperties().get("native_transport_port");
		}
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the native transport port ({@code native_transport_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setPort(int port) {
		getSystemProperties().put("cassandra.native_transport_port", port);
	}

	/**
	 * Returns the native transport SSL port ({@code native_transport_port_ssl}).
	 *
	 * @return the SSL port (or null if none)
	 */
	@Nullable
	public Integer getSslPort() {
		Object port = getProperties().get("native_transport_port_ssl");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the native transport SSL port ({@code native_transport_port_ssl}).
	 *
	 * @param sslPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setSslPort(int sslPort) {
		getProperties().put("native_transport_port_ssl", sslPort);
	}

	/**
	 * Returns the RPC transport port ({@code rpc_port}).
	 *
	 * @return the RPC port (or null if none)
	 */
	@Nullable
	public Integer getRpcPort() {
		Object port = getSystemProperties().get("cassandra.rpc_port");
		if (port == null) {
			port = getProperties().get("rpc_port");
		}
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the RPC transport port ({@code rpc_port}).
	 *
	 * @param rpcPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setRpcPort(int rpcPort) {
		getSystemProperties().put("cassandra.rpc_port", rpcPort);
	}

	/**
	 * Returns the Cassandra's configuration file ({@code cassandra.yaml}).
	 *
	 * @return the configuration file (or null if none)
	 */
	@Nullable
	public URL getConfigurationFile() {
		Object url = getSystemProperties().get("cassandra.config");
		if (url == null) {
			return null;
		}
		try {
			return new URL(url.toString());
		}
		catch (MalformedURLException ex) {
			try {
				return URI.create(url.toString()).toURL();
			}
			catch (Exception swallow) {
				ex.addSuppressed(swallow);
				throw new IllegalArgumentException(ex);
			}
		}
	}

	/**
	 * Sets the Cassandra's configuration file.
	 *
	 * @param configurationFile the configuration file ({@code cassandra.yaml})
	 */
	public void setConfigurationFile(URL configurationFile) {
		Objects.requireNonNull(configurationFile, "'configurationFile' must not be null");
		getSystemProperties().put("cassandra.config", configurationFile.toString());
	}

	/**
	 * Sets the Cassandra's configuration file.
	 *
	 * @param configurationFile the configuration file ({@code cassandra.yaml})
	 */
	public void setConfigurationFile(URI configurationFile) {
		Objects.requireNonNull(configurationFile, "'configurationFile' must not be null");
		try {
			setConfigurationFile(configurationFile.toURL());
		}
		catch (MalformedURLException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Returns the storage port ({@code storage_port}).
	 *
	 * @return the storage port (or null if none)
	 */
	@Nullable
	public Integer getStoragePort() {
		Object port = getSystemProperties().get("cassandra.storage_port");
		if (port == null) {
			port = getProperties().get("storage_port");
		}
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the storage port ({@code storage_port}).
	 *
	 * @param storagePort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setStoragePort(int storagePort) {
		getSystemProperties().put("cassandra.storage_port", storagePort);
	}

	/**
	 * Returns the storage SSL port ({@code storage_ssl_port}).
	 *
	 * @return the storage SSL port (or null if none)
	 */
	@Nullable
	public Integer getStorageSslPort() {
		Object port = getSystemProperties().get("cassandra.ssl_storage_port");
		if (port == null) {
			port = getProperties().get("ssl_storage_port");
		}
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the storage SSL port ({@code ssl_storage_port}).
	 *
	 * @param storageSslPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setStorageSslPort(int storageSslPort) {
		getSystemProperties().put("cassandra.ssl_storage_port", storageSslPort);
	}

	/**
	 * Returns the JMX local port ({@code -Dcassandra.jmx.local.port}).
	 *
	 * @return the jmx local port (or null if none)
	 */
	@Nullable
	public Integer getJmxLocalPort() {
		Object port = getSystemProperties().get("cassandra.jmx.local.port");
		return (port != null) ? Integer.parseInt(port.toString()) : null;
	}

	/**
	 * Sets the JMX local port ({@code -Dcassandra.jmx.local.port}).
	 *
	 * @param jmxLocalPort the port number, or 0 to use a port number that is automatically allocated
	 */
	public void setJmxLocalPort(int jmxLocalPort) {
		getSystemProperties().put("cassandra.jmx.local.port", jmxLocalPort);
	}

	/**
	 * Returns whether shutdown hook should be registered or not.
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
	 * Returns the prefix which will be used for exposed properties, such as {@code embedded.cassandra.port}, if it is
	 * not null or not empty.
	 *
	 * @return the prefix to use
	 */
	@Nullable
	public String getExposedPropertiesPrefix() {
		return this.exposedPropertiesPrefix;
	}

	/**
	 * Sets the prefix used for exposed properties, such as {@code embedded.cassandra.port}. If the prefix is not null
	 * and not empty it will be added before the property name.
	 * <p> Example:
	 * <pre>{@code
	 * exposedPropertiesPrefix=<null> | embedded.cassandra.port
	 * exposedPropertiesPrefix=<empty> | embedded.cassandra.port
	 * exposedPropertiesPrefix='spring' | spring.embedded.cassandra.port
	 * }</pre>
	 *
	 * @param exposedPropertiesPrefix the prefix to use
	 */
	public void setExposedPropertiesPrefix(@Nullable String exposedPropertiesPrefix) {
		this.exposedPropertiesPrefix = exposedPropertiesPrefix;
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
		Artifact.Resource resource = artifact.getResource();
		Version version = resource.getVersion();
		Path workingDirectory = getWorkingDirectory();
		if (workingDirectory == null) {
			workingDirectory = Files.createTempDirectory("apache-cassandra-" + version + "-");
		}
		if (Files.exists(workingDirectory) && !Files.isDirectory(workingDirectory)) {
			throw new IllegalArgumentException(workingDirectory + " is not a directory");
		}
		Path artifactDirectory = resource.getDirectory();
		if (!Files.exists(artifactDirectory)) {
			throw new IllegalStateException(artifactDirectory + " does not exist");
		}
		if (!Files.isDirectory(artifactDirectory)) {
			throw new IllegalStateException(artifactDirectory + " is not a directory");
		}
		Node node = createNode(version, workingDirectory);
		Database database = new EmbeddedDatabase(name, version, isDaemon(), getLogger(), getTimeout(), node);
		String exposedPropertiesPrefix = Objects.toString(getExposedPropertiesPrefix(), "").trim();
		EmbeddedCassandra cassandra = new EmbeddedCassandra(name, exposedPropertiesPrefix, artifactDirectory,
				workingDirectory, version, database);
		if (isRegisterShutdownHook()) {
			Runtime.getRuntime().addShutdownHook(new Thread(cassandra::stop, name + "-sh"));
		}
		return cassandra;
	}

	private Node createNode(Version version, Path workingDirectory) throws Exception {
		Path javaHome = getJavaHome().get();
		if (SystemUtils.isWindows()) {
			return new WindowsNode(version, workingDirectory, javaHome, getJvmOptions(), getSystemProperties(),
					getEnvironmentVariables(), getProperties());
		}
		return new UnixNode(version, workingDirectory, javaHome, getJvmOptions(), getSystemProperties(),
				getEnvironmentVariables(), getProperties(), isRootAllowed());
	}

}
