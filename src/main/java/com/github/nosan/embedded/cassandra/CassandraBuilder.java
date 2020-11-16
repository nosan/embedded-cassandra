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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.github.nosan.embedded.cassandra.commons.web.JdkHttpClient;

/**
 * A builder that can be used to configure and create {@link Cassandra}.
 * <p>
 * <b>This class is not thread safe and should not be shared across different threads!</b>
 * </p>
 *
 * @author Dmytro Nosan
 * @see #build()
 * @see CassandraBuilderConfigurator
 * @since 4.0.0
 */
public class CassandraBuilder {

	/**
	 * Default Cassandra version.
	 */
	public static final Version DEFAULT_VERSION = Version.parse("4.0-beta3");

	private static final AtomicInteger CASSANDRA_ID = new AtomicInteger();

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private final Map<String, Object> configProperties = new LinkedHashMap<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Set<String> jvmOptions = new LinkedHashSet<>();

	private final Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers = new LinkedHashSet<>();

	private boolean registerShutdownHook = true;

	private String name;

	private Version version;

	private Duration startupTimeout;

	private Logger logger;

	private IOSupplier<? extends Path> workingDirectorySupplier;

	private WorkingDirectoryDestroyer workingDirectoryDestroyer;

	private WorkingDirectoryInitializer workingDirectoryInitializer;

	/**
	 * Build a new {@link Cassandra} instance.
	 *
	 * @return a {@link Cassandra} instance.
	 */
	public Cassandra build() {
		String name = (this.name != null) ? this.name : "cassandra-" + CASSANDRA_ID.getAndIncrement();
		Version version = (this.version != null) ? this.version : DEFAULT_VERSION;
		Path workingDirectory;
		try {
			IOSupplier<? extends Path> workingDirectorySupplier = this.workingDirectorySupplier;
			if (workingDirectorySupplier != null) {
				workingDirectory = workingDirectorySupplier.get();
				Objects.requireNonNull(workingDirectory, "Working Directory must not be null");
			}
			else {
				workingDirectory = Files.createTempDirectory("apache-cassandra-" + version + "-");
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Unable to get a working directory", ex);
		}
		WorkingDirectoryInitializer workingDirectoryInitializer = this.workingDirectoryInitializer;
		if (workingDirectoryInitializer == null) {
			workingDirectoryInitializer = new DefaultWorkingDirectoryInitializer(new WebCassandraDirectoryProvider(
					new JdkHttpClient(Duration.ofSeconds(30), Duration.ofSeconds(30))));
		}
		WorkingDirectoryDestroyer workingDirectoryDestroyer = this.workingDirectoryDestroyer;
		if (workingDirectoryDestroyer == null) {
			workingDirectoryDestroyer = WorkingDirectoryDestroyer.deleteOnly("bin", "pylib", "lib", "tools",
					"doc", "javadoc", "interface");
		}
		Duration startupTimeout = this.startupTimeout;
		if (startupTimeout == null) {
			startupTimeout = Duration.ofMinutes(2);
		}
		Logger logger = this.logger;
		if (logger == null) {
			logger = Logger.get(Cassandra.class);
		}
		Map<String, Object> environmentVariables = new LinkedHashMap<>(this.environmentVariables);
		environmentVariables.values().removeIf(Objects::isNull);
		Map<String, Object> systemProperties = new LinkedHashMap<>(this.systemProperties);
		systemProperties.values().removeIf(Objects::isNull);
		Set<String> jvmOptions = new LinkedHashSet<>(this.jvmOptions);
		jvmOptions.removeIf(Objects::isNull);
		Set<WorkingDirectoryCustomizer> workingDirectoryCustomizers = new LinkedHashSet<>(
				this.workingDirectoryCustomizers);
		workingDirectoryCustomizers.removeIf(Objects::isNull);
		Map<String, Object> configProperties = new LinkedHashMap<>(this.configProperties);
		CassandraDatabaseFactory databaseFactory = new DefaultCassandraDatabaseFactory(name, version,
				environmentVariables, configProperties, systemProperties, jvmOptions);
		return new DefaultCassandra(name, version, workingDirectory.normalize().toAbsolutePath(),
				this.registerShutdownHook, workingDirectoryInitializer, workingDirectoryDestroyer, startupTimeout,
				workingDirectoryCustomizers, databaseFactory, logger);
	}

	/**
	 * Sets the Cassandra instance name.
	 * <p>Defaults to cassandra-0, cassandra-1, and so on.
	 *
	 * @param name the Cassandra name
	 * @return this builder
	 */
	public CassandraBuilder name(String name) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		this.name = name;
		return this;
	}

	/**
	 * Gets current Cassandra instance name.
	 *
	 * @return Cassandra instance name, never {@code null}
	 */
	public String getName() {
		String name = this.name;
		return (name != null) ? name : "cassandra-" + CASSANDRA_ID.get();
	}

	/**
	 * Sets the Cassandra version.
	 * <p>Defaults to {@link #DEFAULT_VERSION}
	 *
	 * @param version the Cassandra version
	 * @return this builder
	 */
	public CassandraBuilder version(String version) {
		Objects.requireNonNull(version, "Version must not be null");
		return version(Version.parse(version));
	}

	/**
	 * Sets the Cassandra version.
	 * <p>Defaults to {@link #DEFAULT_VERSION}
	 *
	 * @param version the Cassandra version
	 * @return this builder
	 */
	public CassandraBuilder version(Version version) {
		Objects.requireNonNull(version, "Version must not be null");
		this.version = version;
		return this;
	}

	/**
	 * Gets current Cassandra version.
	 *
	 * @return Cassandra version, never {@code null}
	 */
	public Version getVersion() {
		Version version = this.version;
		return (version != null) ? version : DEFAULT_VERSION;
	}

	/**
	 * Sets the Cassandra logger. This logger will consume Cassandra stdout and stderr outputs.
	 * <p>Defaults to {@code Logger.get(Cassandra.class)}
	 *
	 * @param logger the Cassandra logger
	 * @return this builder
	 */
	public CassandraBuilder logger(Logger logger) {
		Objects.requireNonNull(logger, "Logger must not be null");
		this.logger = logger;
		return this;
	}

	/**
	 * Sets if the created {@link Cassandra} should have a shutdown hook registered.
	 * <p>Defaults to {@code true}.
	 *
	 * @param registerShutdownHook {@code true} if shutdown hook should be registered, otherwise {@code false}
	 * @return this builder
	 */
	public CassandraBuilder registerShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
		return this;
	}

	/**
	 * Sets the startup timeout.
	 * <p>Defaults to 2 minutes.
	 *
	 * @param startupTimeout the startup timeout
	 * @return this builder
	 */
	public CassandraBuilder startupTimeout(Duration startupTimeout) {
		Objects.requireNonNull(startupTimeout, "Startup Timeout must not be null");
		if (startupTimeout.isZero() || startupTimeout.isNegative()) {
			throw new IllegalArgumentException("Startup Timeout must be positive");
		}
		this.startupTimeout = startupTimeout;
		return this;
	}

	/**
	 * Set the {@code Supplier} of the working directory that should be called each time when {@link #build()} is
	 * called.
	 * <p>The supplied directory will be initialized by {@link
	 * WorkingDirectoryInitializer} and used as Cassandra home directory.
	 * <p>
	 * In the end, provided working directory may be deleted or partly deleted by {@link WorkingDirectoryDestroyer}.
	 * <p>Defaults to {@code Files.createTempDirectory("apache-cassandra-" + version + "-")}
	 *
	 * @param workingDirectorySupplier the working directory supplier
	 * @return this builder
	 * @see IOSupplier#wrap(Supplier)
	 */
	public CassandraBuilder workingDirectory(IOSupplier<? extends Path> workingDirectorySupplier) {
		Objects.requireNonNull(workingDirectorySupplier, "Working Directory Supplier must not be null");
		this.workingDirectorySupplier = workingDirectorySupplier;
		return this;
	}

	/**
	 * Sets the {@link WorkingDirectoryInitializer}.
	 * <p>Defaults to {@link DefaultWorkingDirectoryInitializer} with the
	 * underlying {@link WebCassandraDirectoryProvider}.
	 *
	 * @param workingDirectoryInitializer the working directory initializer
	 * @return this builder
	 * @see DefaultWorkingDirectoryInitializer
	 */
	public CassandraBuilder workingDirectoryInitializer(WorkingDirectoryInitializer workingDirectoryInitializer) {
		Objects.requireNonNull(workingDirectoryInitializer, "Working Directory Initializer must not be null");
		this.workingDirectoryInitializer = workingDirectoryInitializer;
		return this;
	}

	/**
	 * Sets the {@link WorkingDirectoryDestroyer}.
	 * <p>
	 * Defaults to {@code WorkingDirectoryDestroyer.deleteOnly("bin", "pylib", "lib", "tools", "doc", "javadoc",
	 * "interface")}
	 *
	 * @param workingDirectoryDestroyer the working directory destroyer
	 * @return this builder
	 * @see WorkingDirectoryDestroyer#doNothing()
	 * @see WorkingDirectoryDestroyer#deleteOnly(String...)
	 * @see WorkingDirectoryDestroyer#deleteAll()
	 */
	public CassandraBuilder workingDirectoryDestroyer(WorkingDirectoryDestroyer workingDirectoryDestroyer) {
		Objects.requireNonNull(workingDirectoryDestroyer, "Working Directory Destroyer must not be null");
		this.workingDirectoryDestroyer = workingDirectoryDestroyer;
		return this;
	}

	/**
	 * Sets the {@link WorkingDirectoryCustomizer}.  Setting this value will replace any previously configured
	 * customizers.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to set
	 * @return this builder
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #workingDirectoryCustomizers(Collection)
	 * @see #addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #addWorkingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder workingDirectoryCustomizers(WorkingDirectoryCustomizer... workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		return workingDirectoryCustomizers(Arrays.asList(workingDirectoryCustomizers));
	}

	/**
	 * Sets the {@link WorkingDirectoryCustomizer}.  Setting this value will replace any previously configured
	 * customizers.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to set
	 * @return this builder
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #workingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #addWorkingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder workingDirectoryCustomizers(
			Collection<? extends WorkingDirectoryCustomizer> workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		this.workingDirectoryCustomizers.clear();
		this.workingDirectoryCustomizers.addAll(deepCopy(workingDirectoryCustomizers));
		return this;
	}

	/**
	 * Adds the {@link WorkingDirectoryCustomizer}.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to add
	 * @return this builder
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #addWorkingDirectoryCustomizers(Collection)
	 * @see #workingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #workingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer... workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		return addWorkingDirectoryCustomizers(Arrays.asList(workingDirectoryCustomizers));
	}

	/**
	 * Adds the {@link WorkingDirectoryCustomizer}.
	 *
	 * @param workingDirectoryCustomizers the working directory customizers to add
	 * @return this builder
	 * @see WorkingDirectoryCustomizer#copy(Resource, String)
	 * @see #addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #workingDirectoryCustomizers(WorkingDirectoryCustomizer...)
	 * @see #workingDirectoryCustomizers(Collection)
	 */
	public CassandraBuilder addWorkingDirectoryCustomizers(
			Collection<? extends WorkingDirectoryCustomizer> workingDirectoryCustomizers) {
		Objects.requireNonNull(workingDirectoryCustomizers, "Working Directory Customizers must not be null");
		this.workingDirectoryCustomizers.addAll(deepCopy(workingDirectoryCustomizers));
		return this;
	}

	/**
	 * Sets Cassandra environment variables. Setting this value will replace any previously configured environment
	 * variables. For example:
	 * <pre>
	 * {@code
	 *  JAVA_HOME=System.getEnv("JAVA_HOME") | System.getProperty("java.home")
	 *  EXTRA_CLASSPATH=new ClassPathResource("lib.jar")
	 * }</pre>
	 *
	 * @param environmentVariables Cassandra environment variables
	 * @return this builder
	 * @see #addEnvironmentVariable(String, Object)
	 * @see #addEnvironmentVariables(Map)
	 */
	public CassandraBuilder environmentVariables(Map<String, ?> environmentVariables) {
		Objects.requireNonNull(environmentVariables, "Environment Variables must not be null");
		this.environmentVariables.clear();
		this.environmentVariables.putAll(deepCopy(environmentVariables));
		return this;
	}

	/**
	 * Puts Cassandra environment variable. For example:
	 * <pre>
	 * {@code
	 * 	  JAVA_HOME=System.getEnv("JAVA_HOME") | System.getProperty("java.home")
	 * 	  EXTRA_CLASSPATH=new ClassPathResource("lib.jar")
	 * }
	 * </pre>
	 *
	 * @param name Cassandra environment variable name
	 * @param value Cassandra environment variable value
	 * @return this builder
	 * @see #addEnvironmentVariables(Map)
	 * @see #environmentVariables(Map)
	 */
	public CassandraBuilder addEnvironmentVariable(String name, Object value) {
		return addEnvironmentVariables(Collections.singletonMap(name, value));
	}

	/**
	 * Puts Cassandra environment variables. For example:
	 * <pre>
	 * {@code
	 *  JAVA_HOME=System.getEnv("JAVA_HOME") | System.getProperty("java.home")
	 *  EXTRA_CLASSPATH=new ClassPathResource("lib.jar")
	 * }</pre>
	 *
	 * @param environmentVariables Cassandra environment variables
	 * @return this builder
	 * @see #addEnvironmentVariable(String, Object)
	 * @see #environmentVariables(Map)
	 */
	public CassandraBuilder addEnvironmentVariables(Map<String, ?> environmentVariables) {
		Objects.requireNonNull(environmentVariables, "Environment Variables must not be null");
		this.environmentVariables.putAll(deepCopy(environmentVariables));
		return this;
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) system parameters. Setting this value will replace any
	 * previously configured system parameters. For example:
	 * <pre>
	 * {@code
	 *  cassandra.config=new ClassPathResource("cassandra.yaml")
	 *  cassandra.native_transport_port=9042
	 *  cassandra.jmx.local.port=7199
	 * }</pre>
	 *
	 * @param systemProperties Cassandra system parameters
	 * @return this builder
	 * @see #addSystemProperty(String, Object)
	 */
	public CassandraBuilder systemProperties(Map<String, ?> systemProperties) {
		Objects.requireNonNull(systemProperties, "System Properties must not be null");
		this.systemProperties.clear();
		this.systemProperties.putAll(deepCopy(systemProperties));
		return this;
	}

	/**
	 * Puts Cassandra native Java Virtual Machine (JVM) system parameter, For example:
	 * <pre>{@code cassandra.config=new ClassPathResource("cassandra.yaml")}.
	 * </pre>
	 *
	 * @param name Cassandra system parameter name
	 * @param value Cassandra system parameter value
	 * @return this builder
	 * @see #addSystemProperties(Map)
	 */
	public CassandraBuilder addSystemProperty(String name, Object value) {
		return addSystemProperties(Collections.singletonMap(name, value));
	}

	/**
	 * Puts Cassandra native Java Virtual Machine (JVM) system parameters. For example:
	 * <pre>
	 * {@code
	 *  cassandra.config=new ClassPathResource("cassandra.yaml")
	 *  cassandra.native_transport_port=9042
	 *  cassandra.jmx.local.port=7199
	 * }</pre>
	 *
	 * @param systemProperties Cassandra system parameters
	 * @return this builder
	 * @see #addSystemProperty(String, Object)
	 */
	public CassandraBuilder addSystemProperties(Map<String, ?> systemProperties) {
		Objects.requireNonNull(systemProperties, "System Properties must not be null");
		this.systemProperties.putAll(deepCopy(systemProperties));
		return this;
	}

	/**
	 * Sets Cassandra native Java Virtual Machine (JVM) Options. Setting this value will replace any previously
	 * configured options.
	 *
	 * @param jvmOptions the JVM options to set
	 * @return this builder
	 * @see #jvmOptions(Collection)
	 * @see #addJvmOptions(Collection)
	 * @see #addJvmOptions(String...)
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
	 * @return this builder
	 * @see #jvmOptions(Collection)
	 * @see #addJvmOptions(Collection)
	 * @see #addJvmOptions(String...)
	 */
	public CassandraBuilder jvmOptions(Collection<String> jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		this.jvmOptions.clear();
		this.jvmOptions.addAll(deepCopy(jvmOptions));
		return this;
	}

	/**
	 * Adds Cassandra native Java Virtual Machine (JVM) Options.
	 *
	 * @param jvmOptions the JVM options to add
	 * @return this builder
	 * @see #addJvmOptions(Collection)
	 * @see #jvmOptions(Collection)
	 * @see #jvmOptions(String...)
	 */
	public CassandraBuilder addJvmOptions(String... jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		return addJvmOptions(Arrays.asList(jvmOptions));
	}

	/**
	 * Adds Cassandra native Java Virtual Machine (JVM) Options.
	 *
	 * @param jvmOptions the JVM options to add
	 * @return this builder
	 * @see #addJvmOptions(String...)
	 * @see #jvmOptions(Collection)
	 * @see #jvmOptions(String...)
	 */
	public CassandraBuilder addJvmOptions(Collection<String> jvmOptions) {
		Objects.requireNonNull(jvmOptions, "JVM Options must not be null");
		this.jvmOptions.addAll(deepCopy(jvmOptions));
		return this;
	}

	/**
	 * Sets Cassandra config properties which should be merged with properties from cassandra.yaml. Setting this value
	 * will replace any previously configured config properties. For example:
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
	 * ...
	 * cluster_name: "MyCluster"
	 * client_encryption_options:
	 *    enabled: true
	 *    ...
	 * ...
	 * </pre>
	 *
	 * @param configProperties Cassandra config properties
	 * @return this builder
	 * @see #addConfigProperty(String, Object)
	 * @see #addConfigProperties(Map)
	 */
	public CassandraBuilder configProperties(Map<String, ?> configProperties) {
		Objects.requireNonNull(configProperties, "Config Properties must not be null");
		this.configProperties.clear();
		this.configProperties.putAll(deepCopy(configProperties));
		return this;
	}

	/**
	 * Puts Cassandra config property which should be merged with a property from cassandra.yaml. For example:
	 * <pre>
	 * {@code
	 * builder.configProperty("client_encryption_options.enabled",true)
	 *        .configProperty("cluster_name","MyCluster")}
	 * </pre>
	 * <p>Output Yaml:
	 * <pre>
	 * ...
	 * cluster_name: "MyCluster"
	 * client_encryption_options:
	 *    enabled: true
	 *    ...
	 * ...
	 * </pre>
	 *
	 * @param name config property name  (e.g. native_transport_port, client_encryption_options.enabled)
	 * @param value config property value
	 * @return this builder
	 * @see #addConfigProperties(Map)
	 * @see #configProperties(Map)
	 */
	public CassandraBuilder addConfigProperty(String name, Object value) {
		return addConfigProperties(Collections.singletonMap(name, value));
	}

	/**
	 * Puts Cassandra config properties which should be merged with properties from cassandra.yaml. For example:
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
	 * ...
	 * cluster_name: "MyCluster"
	 * client_encryption_options:
	 *    enabled: true
	 *    ...
	 * ...
	 * </pre>
	 *
	 * @param configProperties Cassandra config properties
	 * @return this builder
	 * @see #addConfigProperty(String, Object)
	 * @see #configProperties(Map)
	 */
	public CassandraBuilder addConfigProperties(Map<String, ?> configProperties) {
		Objects.requireNonNull(configProperties, "Config Properties must not be null");
		this.configProperties.putAll(deepCopy(configProperties));
		return this;
	}

	/**
	 * Applies the provided configurator to this builder.
	 *
	 * @param configurator configurator to use
	 * @return this builder
	 * @see SimpleSeedProviderConfigurator
	 */
	public CassandraBuilder configure(CassandraBuilderConfigurator configurator) {
		Objects.requireNonNull(configurator, "Cassandra Builder Configurator must not be null");
		configurator.configure(this);
		return this;
	}

	@SuppressWarnings("unchecked")
	private static <T> T deepCopy(T object) {
		if (object instanceof Map<?, ?>) {
			Map<Object, Object> result = new LinkedHashMap<>();
			((Map<?, ?>) object).forEach((name, value) -> {
				if (StringUtils.hasText(Objects.toString(name, null))) {
					result.put(name, deepCopy(value));
				}
			});
			return (T) Collections.unmodifiableMap(result);
		}
		if (object instanceof Collection<?>) {
			List<Object> result = new ArrayList<>();
			((Collection<?>) object).forEach(each -> result.add(deepCopy(each)));
			return (T) Collections.unmodifiableList(result);
		}
		return object;
	}

}
