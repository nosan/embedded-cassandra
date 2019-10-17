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

import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.artifact.ArchiveArtifact;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.artifact.DefaultArtifact;
import com.github.nosan.embedded.cassandra.artifact.RemoteArtifact;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * Builder that can be used to create and configure an {@code EmbeddedCassandra}. Uses the {@link
 * EmbeddedCassandraFactory} to create and configure Cassandra instances.
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandraFactory
 * @since 3.0.0
 */
public final class EmbeddedCassandraBuilder implements CassandraFactory {

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> configProperties = new LinkedHashMap<>();

	private final List<String> jvmOptions = new ArrayList<>();

	@Nullable
	private InetAddress address;

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
	private Boolean rootAllowed;

	@Nullable
	private Boolean daemon;

	@Nullable
	private Boolean registerShutdownHook;

	@Nullable
	private Logger logger;

	@Nullable
	private Duration timeout;

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
	private Path javaHome;

	/**
	 * Sets the RPC address ({@code rpc_address}). The address to bind the Thrift RPC service and native transport.
	 *
	 * @param address the address
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withAddress(@Nullable InetAddress address) {
		this.address = address;
		return this;
	}

	/**
	 * Sets the native transport port ({@code cassandra.native_transport_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withPort(@Nullable Integer port) {
		this.port = port;
		return this;
	}

	/**
	 * Sets the native transport SSL port ({@code native_transport_port_ssl}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withSslPort(@Nullable Integer port) {
		this.sslPort = port;
		return this;
	}

	/**
	 * Sets the storage port ({@code cassandra.storage_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withStoragePort(@Nullable Integer port) {
		this.storagePort = port;
		return this;
	}

	/**
	 * Sets the storage SSL port ({@code cassandra.ssl_storage_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withSslStoragePort(@Nullable Integer port) {
		this.sslStoragePort = port;
		return this;
	}

	/**
	 * Sets the RPC transport port ({@code cassandra.rpc_port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withRpcPort(@Nullable Integer port) {
		this.rpcPort = port;
		return this;
	}

	/**
	 * Sets the JMX local port ({@code cassandra.jmx.local.port}).
	 *
	 * @param port the port number, or 0 to use a port number that is automatically allocated
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withJmxLocalPort(@Nullable Integer port) {
		this.jmxLocalPort = port;
		return this;
	}

	/**
	 * Sets if the {@link Cassandra} can be run under the root user. Defaults to  {@code true}.
	 *
	 * @param rootAllowed if the root user can run {@link Cassandra}
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withRootAllowed(@Nullable Boolean rootAllowed) {
		this.rootAllowed = rootAllowed;
		return this;
	}

	/**
	 * Sets if the thread which reads Cassandra's output should be a daemon or not. Defaults to {@code true}.
	 *
	 * @param daemon if {@code Cassandra's} thread should be a daemon
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withDaemon(@Nullable Boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	/**
	 * Sets if the created {@link Cassandra} should have a shutdown hook registered. Defaults to {@code true} to ensure
	 * that {@link Cassandra} will be stopped.
	 *
	 * @param registerShutdownHook if the shutdown hook should be registered
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withRegisterShutdownHook(@Nullable Boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
		return this;
	}

	/**
	 * Sets {@link Logger} for {@code Cassandra's} output. Defaults to {@code LoggerFactory.getLogger(Cassandra.class)}.
	 *
	 * @param logger the logger
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withLogger(@Nullable Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * Sets Cassandra's startup timeout.
	 *
	 * @param timeout startup timeout (must be positive)
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withTimeout(@Nullable Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Sets Cassandra's name.
	 *
	 * @param name name of Cassandra's instance
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withName(@Nullable String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets {@link Artifact} that provides a path to Cassandra's directory and Cassandra's version.
	 *
	 * @param artifact an artifact
	 * @return this builder
	 * @see Artifact#ofVersion(String)
	 * @see Artifact#ofVersion(Version)
	 * @see RemoteArtifact
	 * @see ArchiveArtifact
	 * @see DefaultArtifact
	 */
	public EmbeddedCassandraBuilder withArtifact(@Nullable Artifact artifact) {
		this.artifact = artifact;
		return this;
	}

	/**
	 * Sets the Cassandra's config. Replaces {@code conf/cassandra.yaml} in the working directory.
	 *
	 * @param config the config
	 * @return this builder
	 * @see Resource
	 */
	public EmbeddedCassandraBuilder withConfig(@Nullable Resource config) {
		this.config = config;
		return this;
	}

	/**
	 * Sets the Cassandra's rack config. Replaces {@code conf/cassandra-rackdc.properties} in the working directory.
	 *
	 * @param rackConfig the rack config
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withRackConfig(@Nullable Resource rackConfig) {
		this.rackConfig = rackConfig;
		return this;
	}

	/**
	 * Sets the Cassandra's topology config. Replaces {@code conf/cassandra-topology.properties} in the working
	 * directory.
	 *
	 * @param topologyConfig the topology config
	 * @return this builder
	 * @see Resource
	 */
	public EmbeddedCassandraBuilder withTopologyConfig(@Nullable Resource topologyConfig) {
		this.topologyConfig = topologyConfig;
		return this;
	}

	/**
	 * Sets Cassandra's working directory. This directory will be initialized on start and <strong>deleted</strong> on
	 * stop. Defaults to {@code tmp dir}.
	 *
	 * @param workingDirectory the working directory
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withWorkingDirectory(@Nullable Path workingDirectory) {
		this.workingDirectory = workingDirectory;
		return this;
	}

	/**
	 * Sets the path to java home.
	 *
	 * @param javaHome path to the java home
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withJavaHome(@Nullable Path javaHome) {
		this.javaHome = javaHome;
		return this;
	}

	/**
	 * Add config property such as {@code start_rpc: true}.
	 *
	 * @param name the name of the property
	 * @param value the value of the property
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withConfigProperty(String name, @Nullable Object value) {
		Objects.requireNonNull(name, "'name' must not be null");
		this.configProperties.put(name, value);
		return this;
	}

	/**
	 * Add Cassandra's configuration properties.
	 *
	 * @param configProperties the properties
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withConfigProperties(Map<String, Object> configProperties) {
		Objects.requireNonNull(configProperties, "'configProperties' must not be null");
		this.configProperties.putAll(configProperties);
		return this;
	}

	/**
	 * Add System property that should be passed to Cassandra's process.
	 *
	 * @param name the name of the system property
	 * @param value the value of the system property
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withSystemProperty(String name, Object value) {
		Objects.requireNonNull(name, "'name' must not be null");
		Objects.requireNonNull(value, "'value' must not be null");
		this.systemProperties.put(name, value);
		return this;
	}

	/**
	 * Add System properties that should be passed to Cassandra's process.
	 *
	 * @param systemProperties system properties
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withSystemProperties(Map<String, Object> systemProperties) {
		Objects.requireNonNull(systemProperties, "'systemProperties' must not be null");
		this.systemProperties.putAll(systemProperties);
		return this;
	}

	/**
	 * Add Environment variable that should be passed to Cassandra's process.
	 *
	 * @param name the name of the env variable
	 * @param value the value of the env variable
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withEnvironmentVariable(String name, Object value) {
		Objects.requireNonNull(name, "'name' must not be null");
		Objects.requireNonNull(value, "'value' must not be null");
		this.environmentVariables.put(name, value);
		return this;
	}

	/**
	 * Add Environment variables that should be passed to Cassandra's process.
	 *
	 * @param environmentVariables environment variables
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withEnvironmentVariables(Map<String, Object> environmentVariables) {
		Objects.requireNonNull(environmentVariables, "'environmentVariables' must not be null");
		this.environmentVariables.putAll(environmentVariables);
		return this;
	}

	/**
	 * Add JVM options that should be passed to Cassandra's process.
	 *
	 * @param options JVM options
	 * @return this builder
	 */
	public EmbeddedCassandraBuilder withJvmOptions(String... options) {
		Objects.requireNonNull(options, "'options' must not be null");
		this.jvmOptions.addAll(Arrays.asList(options));
		return this;
	}

	@Override
	public Cassandra create() {
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		cassandraFactory.getConfigProperties().putAll(this.configProperties);
		cassandraFactory.getSystemProperties().putAll(this.systemProperties);
		cassandraFactory.getEnvironmentVariables().putAll(this.environmentVariables);
		cassandraFactory.getJvmOptions().addAll(this.jvmOptions);
		Optional.ofNullable(this.artifact).ifPresent(cassandraFactory::setArtifact);
		Optional.ofNullable(this.address).ifPresent(cassandraFactory::setAddress);
		Optional.ofNullable(this.rootAllowed).ifPresent(cassandraFactory::setRootAllowed);
		Optional.ofNullable(this.registerShutdownHook).ifPresent(cassandraFactory::setRegisterShutdownHook);
		Optional.ofNullable(this.daemon).ifPresent(cassandraFactory::setDaemon);
		Optional.ofNullable(this.config).ifPresent(cassandraFactory::setConfig);
		Optional.ofNullable(this.rackConfig).ifPresent(cassandraFactory::setRackConfig);
		Optional.ofNullable(this.topologyConfig).ifPresent(cassandraFactory::setTopologyConfig);
		Optional.ofNullable(this.logger).ifPresent(cassandraFactory::setLogger);
		Optional.ofNullable(this.timeout).ifPresent(cassandraFactory::setTimeout);
		Optional.ofNullable(this.name).ifPresent(cassandraFactory::setName);
		Optional.ofNullable(this.workingDirectory).ifPresent(cassandraFactory::setWorkingDirectory);
		Optional.ofNullable(this.javaHome).ifPresent(cassandraFactory::setJavaHome);
		Optional.ofNullable(this.port).ifPresent(cassandraFactory::setPort);
		Optional.ofNullable(this.sslPort).ifPresent(cassandraFactory::setSslPort);
		Optional.ofNullable(this.rpcPort).ifPresent(cassandraFactory::setRpcPort);
		Optional.ofNullable(this.storagePort).ifPresent(cassandraFactory::setStoragePort);
		Optional.ofNullable(this.sslStoragePort).ifPresent(cassandraFactory::setSslStoragePort);
		Optional.ofNullable(this.jmxLocalPort).ifPresent(cassandraFactory::setJmxLocalPort);
		return cassandraFactory.create();
	}

}
