/*
 * Copyright 2020 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.commons.StringUtils;
import com.github.nosan.embedded.cassandra.commons.function.IOSupplier;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;

/**
 * An immutable builder that can be used to configure and create {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see #build()
 * @see CassandraBuilderConfigurator
 * @since 4.0.0
 */
public final class CassandraBuilder {

	private static final AtomicInteger INSTANCES = new AtomicInteger();

	private final String name;

	private final Version version;

	private final boolean registerShutdownHook;

	private final Duration startupTimeout;

	private final Logger logger;

	private final IOSupplier<? extends Path> workingDirectorySupplier;

	private final WorkingDirectoryDestroyer workingDirectoryDestroyer;

	private final WorkingDirectoryInitializer workingDirectoryInitializer;

	private final Map<String, Object> environmentVariables;

	private final Map<String, Object> configProperties;

	private final Map<String, Object> systemProperties;

	private final Set<String> jvmOptions;

	private final Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers;

	/**
	 * Creates new instance of {@link CassandraBuilder}.
	 */
	public CassandraBuilder() {
		this.name = null;
		this.version = null;
		this.workingDirectorySupplier = null;
		this.startupTimeout = null;
		this.workingDirectoryDestroyer = null;
		this.workingDirectoryInitializer = null;
		this.logger = null;
		this.registerShutdownHook = true;
		this.environmentVariables = Collections.emptyMap();
		this.systemProperties = Collections.emptyMap();
		this.jvmOptions = Collections.emptySet();
		this.configProperties = Collections.emptyMap();
		this.workingDirectoryCustomizers = Collections.emptySet();
	}

	private CassandraBuilder(String name, Version version, IOSupplier<? extends Path> workingDirectorySupplier,
			Map<String, Object> environmentVariables, Map<String, Object> systemProperties, Set<String> jvmOptions,
			boolean registerShutdownHook, Map<String, Object> configProperties,
			Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers,
			WorkingDirectoryDestroyer workingDirectoryDestroyer,
			WorkingDirectoryInitializer workingDirectoryInitializer, Duration startupTimeout, Logger logger) {
		this.name = name;
		this.version = version;
		this.workingDirectorySupplier = workingDirectorySupplier;
		this.environmentVariables = environmentVariables;
		this.systemProperties = systemProperties;
		this.jvmOptions = jvmOptions;
		this.registerShutdownHook = registerShutdownHook;
		this.configProperties = configProperties;
		this.workingDirectoryCustomizers = workingDirectoryCustomizers;
		this.workingDirectoryDestroyer = workingDirectoryDestroyer;
		this.workingDirectoryInitializer = workingDirectoryInitializer;
		this.startupTimeout = startupTimeout;
		this.logger = logger;
	}

	/**
	 * Build a new {@link Cassandra} instance.
	 *
	 * @return a {@link Cassandra} instance.
	 */
	public Cassandra build() {
		String name = (this.name != null) ? this.name : "cassandra-" + INSTANCES.getAndIncrement();
		Version version = (this.version != null) ? this.version : Version.parse("4.0-beta2");
		IOSupplier<? extends Path> workingDirectorySupplier = this.workingDirectorySupplier;
		if (workingDirectorySupplier == null) {
			workingDirectorySupplier = () -> Files.createTempDirectory("apache-cassandra-" + version + "-");
		}
		WorkingDirectoryInitializer workingDirectoryInitializer = this.workingDirectoryInitializer;
		if (workingDirectoryInitializer == null) {
			workingDirectoryInitializer = new DefaultWorkingDirectoryInitializer(new WebCassandraDirectoryProvider());
		}
		WorkingDirectoryDestroyer workingDirectoryDestroyer = this.workingDirectoryDestroyer;
		if (workingDirectoryDestroyer == null) {
			workingDirectoryDestroyer = WorkingDirectoryDestroyer.deleteOnly("bin", "pylib", "lib", "tools",
					"doc", "javadoc", "interface");
		}
		Logger logger = this.logger;
		if (logger == null) {
			logger = Logger.get("Cassandra");
		}
		Duration startupTimeout = this.startupTimeout;
		if (startupTimeout == null) {
			startupTimeout = Duration.ofMinutes(2);
		}
		return new DefaultCassandra(name, version, this.environmentVariables, this.systemProperties,
				this.registerShutdownHook, this.jvmOptions, workingDirectorySupplier, this.configProperties,
				workingDirectoryInitializer, workingDirectoryDestroyer, startupTimeout,
				this.workingDirectoryCustomizers, logger);
	}

	/**
	 * Sets the Cassandra instance name.
	 * <p>Defaults to cassandra-${counter}
	 *
	 * @param name the Cassandra name
	 * @return a new builder instance.
	 */
	public CassandraBuilder name(String name) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		return new CassandraBuilder(name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets the Cassandra version.
	 * <p>Defaults to <b>4.0-beta2</b>
	 *
	 * @param version the Cassandra version
	 * @return a new builder instance.
	 */
	public CassandraBuilder version(String version) {
		Objects.requireNonNull(version, "Version must not be null");
		return new CassandraBuilder(this.name, Version.parse(version), this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets the Cassandra logger. This logger will consume Cassandra stdout and stderr outputs.
	 * <p>Defaults to {@code Logger.get("Cassandra")}
	 *
	 * @param logger the Cassandra logger
	 * @return a new builder instance.
	 */
	public CassandraBuilder logger(Logger logger) {
		Objects.requireNonNull(logger, "Logger must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, logger);
	}

	/**
	 * Sets the working directory supplier. The supplied directory will be initialized by {@link
	 * WorkingDirectoryInitializer} and used as Cassandra home directory.
	 * <p>
	 * In the end, provided working directory could be deleted or partly deleted by {@link WorkingDirectoryDestroyer}.
	 * <p>Defaults to {@code Files.createTempDirectory("apache-cassandra-" + version + "-")}
	 *
	 * @param workingDirectorySupplier the working directory supplier
	 * @return a new builder instance.
	 * @see IOSupplier#wrap(Supplier)
	 */
	public CassandraBuilder workingDirectory(IOSupplier<? extends Path> workingDirectorySupplier) {
		Objects.requireNonNull(workingDirectorySupplier, "Working Directory Supplier must not be null");
		return new CassandraBuilder(this.name, this.version, workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets if the created {@link Cassandra} should have a shutdown hook registered.
	 * <p>Defaults to {@code true}.
	 *
	 * @param registerShutdownHook {@code true} if shutdown hook should be registered, otherwise {@code false}
	 * @return a new builder instance.
	 */
	public CassandraBuilder registerShutdownHook(boolean registerShutdownHook) {
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets Cassandra environment variable, e.g. {@code JAVA_HOME=System.getEnv("JAVA_HOME")}.
	 *
	 * @param name Cassandra environment variable name
	 * @param value Cassandra environment variable value
	 * @return a new builder instance.
	 * @see #environmentVariables(Map)
	 * @see #clearEnvironmentVariables()
	 */
	public CassandraBuilder environmentVariable(String name, Object value) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		return environmentVariables(Collections.singletonMap(name, convertValue(value)));
	}

	/**
	 * Sets Cassandra environment variables. There are some environment variables:
	 * <pre>
	 * {@code
	 *  JAVA_HOME=System.getEnv("JAVA_HOME") | System.getProperty("java.home")
	 *  EXTRA_CLASSPATH=new ClassPathResource("lib.jar")
	 * }</pre>
	 *
	 * @param environmentVariables Cassandra environment variables
	 * @return a new builder instance.
	 * @see #clearEnvironmentVariables()
	 * @see #environmentVariable(String, Object)
	 */
	@SuppressWarnings("unchecked")
	public CassandraBuilder environmentVariables(Map<String, Object> environmentVariables) {
		Objects.requireNonNull(environmentVariables, "Environment Variables must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				putAll(this.environmentVariables, (Map<String, Object>) convertValue(environmentVariables)),
				this.systemProperties, this.jvmOptions, this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Clears any previously configured environment variables.
	 *
	 * @return a new builder instance.
	 */
	public CassandraBuilder clearEnvironmentVariables() {
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				Collections.emptyMap(), this.systemProperties, this.jvmOptions, this.registerShutdownHook,
				this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) system parameter, For instance:
	 * <pre>{@code cassandra.config=new ClassPathResource("cassandra.yaml")}.
	 * </pre>
	 *
	 * @param name Cassandra system parameter name
	 * @param value Cassandra system parameter value
	 * @return a new builder instance.
	 * @see #systemProperties(Map)
	 * @see #clearSystemProperties()
	 */
	public CassandraBuilder systemProperty(String name, Object value) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		return systemProperties(Collections.singletonMap(name, convertValue(value)));
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) system parameters. There are some system properties:
	 * <pre>
	 * {@code
	 *  cassandra.config=new ClassPathResource("cassandra.yaml")
	 *  cassandra.native_transport_port=9042
	 *  cassandra.jmx.local.port=7199
	 * }</pre>
	 *
	 * @param systemProperties Cassandra system parameters
	 * @return a new builder instance.
	 * @see #clearSystemProperties()
	 * @see #systemProperty(String, Object)
	 */
	@SuppressWarnings("unchecked")
	public CassandraBuilder systemProperties(Map<String, Object> systemProperties) {
		Objects.requireNonNull(systemProperties, "System Properties must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables,
				putAll(this.systemProperties, (Map<String, Object>) convertValue(systemProperties)),
				this.jvmOptions, this.registerShutdownHook,
				this.configProperties, this.workingDirectoryCustomizers, this.workingDirectoryDestroyer,
				this.workingDirectoryInitializer, this.startupTimeout, this.logger);
	}

	/**
	 * Clears any previously configured system properties.
	 *
	 * @return a new builder instance.
	 */
	public CassandraBuilder clearSystemProperties() {
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, Collections.emptyMap(),
				this.jvmOptions, this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) Options. Setting this value will replace any previously
	 * configured options.
	 *
	 * @param jvmOptions the JVM options to set
	 * @return a new builder instance
	 */
	public CassandraBuilder jvmOptions(String... jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		return jvmOptions(Arrays.asList(jvmOptions));
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) Options. Setting this value will replace any previously
	 * configured options.
	 *
	 * @param jvmOptions the JVM options to set
	 * @return a new builder instance
	 */
	public CassandraBuilder jvmOptions(Collection<String> jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties,
				addAll(Collections.emptySet(), jvmOptions), this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Adds Cassandra native Java Virtual Machine (JVM) Options.
	 *
	 * @param jvmOptions the JVM options to add
	 * @return a new builder instance
	 */
	public CassandraBuilder addJvmOptions(String... jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		return addJvmOptions(Arrays.asList(jvmOptions));
	}

	/**
	 * Adds Cassandra native Java Virtual Machine (JVM) Options.
	 *
	 * @param jvmOptions the JVM options to add
	 * @return a new builder instance
	 */
	public CassandraBuilder addJvmOptions(Collection<String> jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties,
				addAll(this.jvmOptions, jvmOptions), this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets Cassandra config property which should be merged with a property from cassandra.yaml.
	 * <pre>
	 * {@code
	 * builder.configProperty("client_encryption_options.enabled",true)
	 *        .configProperty("cluster_name","MyCluster")}
	 * </pre>
	 * <p>Output Yaml:
	 * <pre>
	 * cluster_name: "MyCluster"
	 * client_encryption_options:
	 *    enabled: true
	 * </pre>
	 *
	 * @param name config property name  (e.g. native_transport_port, client_encryption_options.enabled)
	 * @param value config property value
	 * @return a new builder instance
	 * @see #clearConfigProperties()
	 * @see #configProperties(Map)
	 */
	public CassandraBuilder configProperty(String name, Object value) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		return configProperties(Collections.singletonMap(name, convertValue(value)));
	}

	/**
	 * Sets Cassandra config properties which should be merged with properties from cassandra.yaml.
	 * <pre>
	 * {@code
	 * Map<String,Object> properties = new LinkedHashMap<>();
	 * properties.put("client_encryption_options.enabled", true);
	 * properties.put("cluster_name", "MyCluster");
	 * builder.configProperties(properties);
	 * }
	 * </pre>
	 * Output Yaml:
	 * <pre>
	 * cluster_name: "MyCluster"
	 * client_encryption_options:
	 *    enabled: true
	 * </pre>
	 *
	 * @param configProperties Cassandra config properties
	 * @return a new builder instance
	 * @see #clearConfigProperties()
	 * @see #configProperty(String, Object)
	 */
	@SuppressWarnings("unchecked")
	public CassandraBuilder configProperties(Map<String, Object> configProperties) {
		Objects.requireNonNull(configProperties, "Config Properties must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties,
				this.jvmOptions, this.registerShutdownHook,
				putAll(this.configProperties, (Map<String, Object>) convertValue(configProperties)),
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Clears any previously configured config properties.
	 *
	 * @return a new builder instance.
	 */
	public CassandraBuilder clearConfigProperties() {
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, Collections.emptyMap(),
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets the {@link WorkingDirectoryInitializer}.
	 * <p>Defaults to {@link DefaultWorkingDirectoryInitializer} with the
	 * underlying {@link WebCassandraDirectoryProvider}.
	 *
	 * @param workingDirectoryInitializer the working directory initializer
	 * @return a new builder instance.
	 */
	public CassandraBuilder workingDirectoryInitializer(WorkingDirectoryInitializer workingDirectoryInitializer) {
		Objects.requireNonNull(workingDirectoryInitializer, "Working Directory Initializer must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets the {@link WorkingDirectoryDestroyer}.
	 * <p>
	 * Defaults to {@code WorkingDirectoryDestroyer.deleteOnly("bin", "pylib", "lib", "tools", "doc", "javadoc",
	 * "interface")}
	 *
	 * @param workingDirectoryDestroyer the working directory destroyer
	 * @return a new builder instance.
	 */
	public CassandraBuilder workingDirectoryDestroyer(WorkingDirectoryDestroyer workingDirectoryDestroyer) {
		Objects.requireNonNull(workingDirectoryDestroyer, "Working Directory Destroyer must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, workingDirectoryDestroyer, this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Sets the startup timeout.
	 * <p>Defaults to 2 minutes.
	 *
	 * @param startupTimeout the startup timeout
	 * @return a new builder instance.
	 */
	public CassandraBuilder startupTimeout(Duration startupTimeout) {
		Objects.requireNonNull(startupTimeout, "Startup Timeout must not be null");
		if (startupTimeout.isZero() || startupTimeout.isNegative()) {
			throw new IllegalArgumentException("Startup Timeout must be positive");
		}
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties, this.jvmOptions,
				this.registerShutdownHook, this.configProperties,
				this.workingDirectoryCustomizers, this.workingDirectoryDestroyer, this.workingDirectoryInitializer,
				startupTimeout, this.logger);
	}

	/**
	 * Sets the {@link WorkingDirectoryCustomizer}. Customizers are applied in the order that they were added after
	 * builder configuration has been applied. Setting this value will replace any previously configured customizers.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to set
	 * @return a new builder instance
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #addWorkingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder workingDirectoryCustomizers(WorkingDirectoryCustomizer... workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		return workingDirectoryCustomizers(Arrays.asList(workingDirectoryCustomizers));
	}

	/**
	 * Sets the {@link WorkingDirectoryCustomizer}. Customizers are applied in the order that they were added after
	 * builder configuration has been applied. Setting this value will replace any previously configured customizers.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to set
	 * @return a new builder instance
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #addWorkingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder workingDirectoryCustomizers(
			Collection<? extends WorkingDirectoryCustomizer> workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties,
				this.jvmOptions, this.registerShutdownHook, this.configProperties,
				addAll(Collections.emptySet(), workingDirectoryCustomizers), this.workingDirectoryDestroyer,
				this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Adds the {@link WorkingDirectoryCustomizer}. Customizers are applied in the order that they were added after
	 * builder configuration has been applied.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to add
	 * @return a new builder instance
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #workingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #workingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer... workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		return addWorkingDirectoryCustomizers(Arrays.asList(workingDirectoryCustomizers));
	}

	/**
	 * Adds the {@link WorkingDirectoryCustomizer}. Customizers are applied in the order that they were added after
	 * builder configuration has been applied.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to add
	 * @return a new builder instance
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #workingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #workingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder addWorkingDirectoryCustomizers(
			Collection<? extends WorkingDirectoryCustomizer> workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties,
				this.jvmOptions, this.registerShutdownHook, this.configProperties,
				addAll(this.workingDirectoryCustomizers, workingDirectoryCustomizers), this.workingDirectoryDestroyer,
				this.workingDirectoryInitializer,
				this.startupTimeout, this.logger);
	}

	/**
	 * Applies the given configurator to this builder and returns the new one.
	 *
	 * @param configurator configurator to use
	 * @return a new builder instance
	 * @see SimpleSeedProviderConfigurator
	 */
	public CassandraBuilder configure(CassandraBuilderConfigurator configurator) {
		Objects.requireNonNull(configurator, "Cassandra Builder Configurator must not be null");
		return configurator.configure(this);
	}

	private static <K, V> Map<K, V> putAll(Map<K, V> to, Map<K, V> from) {
		Map<K, V> result = new LinkedHashMap<>(to);
		for (Map.Entry<K, V> entry : from.entrySet()) {
			K key = entry.getKey();
			Objects.requireNonNull(key, "Name must not be null");
			V value = entry.getValue();
			result.put(key, value);
		}
		return Collections.unmodifiableMap(result);
	}

	private static <T> Set<T> addAll(Set<T> to, Collection<? extends T> from) {
		Set<T> result = new LinkedHashSet<>(to);
		for (T value : from) {
			if (value != null) {
				Objects.requireNonNull(value, "Value must not be null");
				result.add(value);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	private static Object convertValue(Object object) {
		if (object instanceof Path) {
			return object.toString();
		}
		if (object instanceof File) {
			return object.toString();
		}
		if (object instanceof Resource) {
			try {
				return Paths.get(((Resource) object).toURI()).toString();
			}
			catch (Exception ex) {
				//ignore
			}
			try {
				return ((Resource) object).toURI().toString();
			}
			catch (IOException ex) {
				//ignore
			}
			try {
				return ((Resource) object).toURL().toString();
			}
			catch (IOException ex) {
				//ignore
			}
		}
		if (object instanceof Map<?, ?>) {
			Map<Object, Object> result = new LinkedHashMap<>();
			((Map<?, ?>) object).forEach((name, value) -> result.put(name, convertValue(value)));
			return result;
		}
		if (object instanceof Collection<?>) {
			List<Object> result = new ArrayList<>();
			((Collection<?>) object).forEach(each -> result.add(convertValue(each)));
			return result;
		}
		return object;
	}

}
