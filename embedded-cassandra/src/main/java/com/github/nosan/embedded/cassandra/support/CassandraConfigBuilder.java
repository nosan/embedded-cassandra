/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.support;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;
import de.flapdoodle.embed.process.runtime.Network;

import com.github.nosan.embedded.cassandra.CassandraConfig;
import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.process.customizer.FileCustomizer;
import com.github.nosan.embedded.cassandra.process.customizer.JVMOptionsCustomizer;
import com.github.nosan.embedded.cassandra.process.customizer.JmxPortCustomizer;

/**
 * Simple {@link CassandraConfig Cassandra Config} builder.
 *
 * @author Dmytro Nosan
 */
public class CassandraConfigBuilder extends AbstractBuilder<CassandraConfig> {

	private static final TypedProperty<Config> CONFIG = TypedProperty.with("Config",
			Config.class);

	private static final TypedProperty<Duration> TIMEOUT = TypedProperty.with("Timeout",
			Duration.class);

	private static final TypedProperty<Version> VERSION = TypedProperty.with("Version",
			Version.class);

	private static final TypedProperty<FileCustomizer[]> FILE_CUSTOMIZERS = TypedProperty
			.with("FileCustomizers", FileCustomizer[].class);

	private static final TypedProperty<Integer> JMX_PORT = TypedProperty.with("JMXPort",
			Integer.class);

	private static final TypedProperty<String[]> JVM_OPTIONS = TypedProperty
			.with("JvmOptions", String[].class);

	private static final TypedProperty<Boolean> RANDOM_PORTS = TypedProperty
			.with("RandomPorts", Boolean.class);

	public CassandraConfigBuilder() {
		config().overwriteDefault(new Config());
		timeout().overwriteDefault(Duration.ofMinutes(1));
		version().overwriteDefault(Version.LATEST);
		jmxPort().overwriteDefault(7199);
		jvmOptions().overwriteDefault(new String[0]);
		useRandomPorts().overwriteDefault(false);
		fileCustomizers().overwriteDefault(new FileCustomizer[0]);
	}

	/**
	 * Sets JMX port.
	 * @param jmxPort JMX port.
	 * @return current builder.
	 * @see JmxPortCustomizer
	 */
	public CassandraConfigBuilder jmxPort(int jmxPort) {
		jmxPort().overwriteDefault(jmxPort);
		return this;
	}

	/**
	 * Sets JVM Options.
	 * @param jvmOptions JVM Options.
	 * @return current builder
	 * @see JVMOptionsCustomizer
	 */
	public CassandraConfigBuilder jvmOptions(String... jvmOptions) {
		jvmOptions().set(jvmOptions);
		return this;
	}

	/**
	 * Sets version.
	 * @param version version to use.
	 * @return current builder
	 */
	public CassandraConfigBuilder version(Version version) {
		version().set(version);
		return this;
	}

	/**
	 * Sets startup timeout.
	 * @param timeout startup timeout.
	 * @return current builder.
	 */
	public CassandraConfigBuilder timeout(Duration timeout) {
		timeout().set(timeout);
		return this;
	}

	/**
	 * Sets config.
	 * @param config config to use.
	 * @return current builder.
	 */
	public CassandraConfigBuilder config(Config config) {
		config().set(config);
		return this;
	}

	/**
	 * Sets file customizers.
	 * @param fileCustomizers {@link FileCustomizer} file customizers to use.
	 * @return current builder.
	 * @see FileCustomizer
	 */
	public CassandraConfigBuilder fileCustomizers(FileCustomizer... fileCustomizers) {
		fileCustomizers().set(fileCustomizers);
		return this;
	}

	/**
	 * Use random ports for {@link Config Config} and JMX.
	 * @param useRandomPorts whether use random ports or not.
	 * @return current builder.
	 */
	public CassandraConfigBuilder useRandomPorts(boolean useRandomPorts) {
		useRandomPorts().set(useRandomPorts);
		return this;
	}

	@Override
	public CassandraConfig build() {
		int jmxPort = jmxPort().get();

		Config config = config().get();

		if (useRandomPorts().get()) {
			config.setSslStoragePort(0);
			config.setStoragePort(0);
			config.setNativeTransportPort(0);
			config.setRpcPort(0);
			if (config.getNativeTransportPortSsl() != null) {
				config.setNativeTransportPortSsl(0);
			}
			jmxPort = 0;
		}

		if (config.getSslStoragePort() == 0) {
			config.setSslStoragePort(getRandomPort());
		}
		if (config.getStoragePort() == 0) {
			config.setStoragePort(getRandomPort());
		}
		if (config.getNativeTransportPort() == 0) {
			config.setNativeTransportPort(getRandomPort());
		}
		if (config.getRpcPort() == 0) {
			config.setRpcPort(getRandomPort());
		}
		if (config.getNativeTransportPortSsl() != null
				&& config.getNativeTransportPortSsl() == 0) {
			config.setNativeTransportPortSsl(getRandomPort());
		}

		if (jmxPort == 0) {
			jmxPort = getRandomPort();
		}

		return new ImmutableCassandraConfig(config, timeout().get(), version().get(),
				jmxPort, Arrays.asList(fileCustomizers().get()),
				Arrays.asList(jvmOptions().get()));
	}

	protected IProperty<FileCustomizer[]> fileCustomizers() {
		return property(FILE_CUSTOMIZERS);
	}

	protected IProperty<Boolean> useRandomPorts() {
		return property(RANDOM_PORTS);
	}

	protected IProperty<Version> version() {
		return property(VERSION);
	}

	protected IProperty<Duration> timeout() {
		return property(TIMEOUT);
	}

	protected IProperty<Integer> jmxPort() {
		return property(JMX_PORT);
	}

	protected IProperty<Config> config() {
		return property(CONFIG);
	}

	protected IProperty<String[]> jvmOptions() {
		return property(JVM_OPTIONS);
	}

	private static int getRandomPort() {
		try {
			return Network.getFreeServerPort();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static final class ImmutableCassandraConfig implements CassandraConfig {

		private final Config config;

		private final Duration timeout;

		private final Version version;

		private final int jmxPort;

		private final List<FileCustomizer> fileCustomizers;

		private final List<String> jvmOptions;

		ImmutableCassandraConfig(Config config, Duration timeout, Version version,
				int jmxPort, List<FileCustomizer> fileCustomizers,
				List<String> jvmOptions) {
			this.config = config;
			this.timeout = timeout;
			this.version = version;
			this.jmxPort = jmxPort;
			this.fileCustomizers = Collections.unmodifiableList(fileCustomizers);
			this.jvmOptions = Collections.unmodifiableList(jvmOptions);
		}

		@Override
		public Config getConfig() {
			return this.config;
		}

		@Override
		public Duration getTimeout() {
			return this.timeout;
		}

		@Override
		public Version getVersion() {
			return this.version;
		}

		@Override
		public int getJmxPort() {
			return this.jmxPort;
		}

		@Override
		public List<String> getJvmOptions() {
			return this.jvmOptions;
		}

		@Override
		public List<FileCustomizer> getFileCustomizers() {
			return this.fileCustomizers;
		}

	}

}
