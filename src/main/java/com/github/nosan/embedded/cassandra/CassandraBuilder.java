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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * An immutable builder that can be used to configure and create a {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see #build()
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
	 * Build a new {@link Cassandra} instance and configure it using this builder.
	 *
	 * @return a configured {@link Cassandra} instance.
	 */
	public Cassandra build() {
		String name = (this.name != null) ? this.name : "cassandra-" + INSTANCES.getAndIncrement();
		Version version = (this.version != null) ? this.version : Version.parse("4.0-beta2");
		IOSupplier<? extends Path> workingDirectorySupplier = this.workingDirectorySupplier;
		if (workingDirectorySupplier == null) {
			workingDirectorySupplier = () -> Files.createTempDirectory(name + "-" + version + "-");
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
			startupTimeout = Duration.ofSeconds(90);
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
	 * Sets the Cassandra logger.
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
	 * <p>Defaults to {@code Files.createTempDirectory(name + "-" + version + "-")}
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
		return environmentVariables(Collections.singletonMap(name, value));
	}

	/**
	 * Sets Cassandra environment variables. There are some environment variables:
	 * <p>
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
	public CassandraBuilder environmentVariables(Map<String, Object> environmentVariables) {
		Objects.requireNonNull(environmentVariables, "Environment Variables must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				putAll(this.environmentVariables, environmentVariables), this.systemProperties,
				this.jvmOptions, this.registerShutdownHook, this.configProperties,
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
	 * Sets Cassandra native Java Virtual Machine (JVM) system parameter, e.g. {@code cassandra.config=new
	 * ClassPathResource("cassandra.yaml")}.
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
		return systemProperties(Collections.singletonMap(name, value));
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) system parameters. There are some system properties:
	 * <p>
	 * <pre>
	 * {@code
	 *  cassandra.config=new ClassPathResource("cassandra.yaml") //override default cassandra.yaml
	 *  cassandra.native_transport_port=9042 //set native transport port
	 *  cassandra.jmx.local.port=7199 //set jmx local port
	 * }</pre>
	 *
	 * @param systemProperties Cassandra system parameters
	 * @return a new builder instance.
	 * @see #clearSystemProperties()
	 * @see #systemProperty(String, Object)
	 */
	public CassandraBuilder systemProperties(Map<String, Object> systemProperties) {
		Objects.requireNonNull(systemProperties, "System Properties must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, putAll(this.systemProperties, systemProperties),
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
	 * Sets Cassandra config property.
	 * <p>This property has a higher precedence than the cassandra.yaml file. The property
	 * from cassandra.yaml will be overridden by the given one.
	 * <p>
	 * The property name could contain a dot, for instance: {@code client_encryption_options.enabled} which means that
	 * value is corresponding to {@code enabled} which is a child of {@code client_encryption_options}.
	 * <p>Source YAML:
	 * <pre>
	 *        cluster_name: "Test"
	 *        client_encryption_options:
	 *          enabled: false
	 * </pre>
	 * <p>
	 * <pre>
	 * {@code
	 * builder.configProperty("client_encryption_options.enabled",true)
	 *        .configProperty("cluster_name","MyCluster")}
	 * </pre>
	 * <p>Output Yaml:
	 * <pre>
	 *        cluster_name: "MyCluster"
	 *        client_encryption_options:
	 *           enabled: true
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
		return configProperties(Collections.singletonMap(name, value));
	}

	/**
	 * Sets Cassandra config properties. These properties have higher precedence than the cassandra.yaml file.
	 * Properties from cassandra.yaml will be overridden by the these properties.
	 * <p>
	 * The property name could contain a dot, for instance: {@code client_encryption_options.enabled} which means that
	 * value is corresponding to {@code enabled} which is a child of {@code client_encryption_options}.
	 * <p>Source YAML:
	 * <pre>
	 *        cluster_name: "Test"
	 *        client_encryption_options:
	 *           enabled: false
	 * </pre>
	 * <p>
	 * <pre>
	 * {@code
	 * Map<String,Object> properties = new LinkedHashMap<>();
	 * properties.put("client_encryption_options.enabled", true);
	 * properties.put("cluster_name", "MyCluster");
	 * builder.configProperties(properties);}
	 * </pre>
	 * <p>Output Yaml:
	 * <pre>
	 *        cluster_name: "MyCluster"
	 *        client_encryption_options:
	 *          enabled: true
	 * </pre>
	 *
	 * @param configProperties Cassandra config properties
	 * @return a new builder instance
	 * @see #clearConfigProperties()
	 * @see #configProperty(String, Object)
	 */
	public CassandraBuilder configProperties(Map<String, Object> configProperties) {
		Objects.requireNonNull(configProperties, "Config Properties must not be null");
		return new CassandraBuilder(this.name, this.version, this.workingDirectorySupplier,
				this.environmentVariables, this.systemProperties,
				this.jvmOptions, this.registerShutdownHook,
				putAll(this.configProperties, configProperties),
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
	 * <p>Defaults to 90 seconds.
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

}
