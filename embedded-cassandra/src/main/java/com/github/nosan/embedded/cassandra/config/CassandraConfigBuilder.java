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
package com.github.nosan.embedded.cassandra.config;

import java.time.Duration;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.customizer.CompositeFileCustomizer;
import com.github.nosan.embedded.cassandra.customizer.EnvironmentFileCustomizer;
import com.github.nosan.embedded.cassandra.customizer.FileCustomizer;
import com.github.nosan.embedded.cassandra.customizer.JVMOptionsFileCustomizer;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.IProperty;
import de.flapdoodle.embed.process.builder.TypedProperty;

/**
 * Simple builder for building {@link CassandraConfig}.
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

	private static final TypedProperty<FileCustomizer> FILE_CUSTOMIZER = TypedProperty
			.with("FileCustomizer", FileCustomizer.class);

	private static final TypedProperty<Boolean> RANDOM_PORTS = TypedProperty
			.with("RandomPorts", Boolean.class);

	/**
	 * Sets default settings.
	 * @return builder with defaults settings.
	 */
	public CassandraConfigBuilder defaults() {
		config().overwriteDefault(new Config());
		timeout().overwriteDefault(Duration.ofMinutes(1));
		version().overwriteDefault(Version.LATEST);
		useRandomPorts().overwriteDefault(false);
		fileCustomizer().overwriteDefault(new CompositeFileCustomizer(
				new JVMOptionsFileCustomizer(), new EnvironmentFileCustomizer()));
		return this;
	}

	/**
	 * Sets the cassandra version.
	 * @param version cassandra version.
	 * @return current builder
	 */
	public CassandraConfigBuilder version(Version version) {
		version().set(version);
		return this;
	}

	/**
	 * Sets the cassandra startup timeout.
	 * @param timeout cassandra startup timeout.
	 * @return current builder.
	 */
	public CassandraConfigBuilder timeout(Duration timeout) {
		timeout().set(timeout);
		return this;
	}

	/**
	 * Sets the cassandra config.
	 * @param config cassandra config.
	 * @return current builder.
	 */
	public CassandraConfigBuilder config(Config config) {
		config().set(config);
		return this;
	}

	/**
	 * Sets the cassandra files customizer.
	 * @param fileCustomizer {@link FileCustomizer} file customizer.
	 * @return current builder.
	 */
	public CassandraConfigBuilder fileCustomizer(FileCustomizer fileCustomizer) {
		fileCustomizer().set(fileCustomizer);
		return this;
	}

	/**
	 * Use random ports or not.
	 * @param useRandomPorts use random port value.
	 * @return current builder.
	 */
	public CassandraConfigBuilder useRandomPorts(boolean useRandomPorts) {
		useRandomPorts().set(useRandomPorts);
		return this;
	}

	@Override
	public CassandraConfig build() {
		Config config = config().get();
		if (useRandomPorts().get()) {
			PortUtils.setRandomPorts(config, Objects::nonNull);
		}
		return new ImmutableCassandraConfig(config, timeout().get(), version().get(),
				fileCustomizer().get());
	}

	/**
	 * Retrieves file customizer property.
	 * @return file customizer {@link IProperty}.
	 */
	protected IProperty<FileCustomizer> fileCustomizer() {
		return property(FILE_CUSTOMIZER);
	}

	/**
	 * Retrieves random ports property.
	 * @return random ports {@link IProperty}.
	 */
	protected IProperty<Boolean> useRandomPorts() {
		return property(RANDOM_PORTS);
	}

	/**
	 * Retrieves version property.
	 * @return version {@link IProperty}.
	 */
	protected IProperty<Version> version() {
		return property(VERSION);
	}

	/**
	 * Retrieves timeout property.
	 * @return timeout {@link IProperty}.
	 */
	protected IProperty<Duration> timeout() {
		return property(TIMEOUT);
	}

	/**
	 * Retrieves config property.
	 * @return config {@link IProperty}.
	 */
	protected IProperty<Config> config() {
		return property(CONFIG);
	}

	private static final class ImmutableCassandraConfig implements CassandraConfig {

		private final Config config;

		private final Duration timeout;

		private final Version version;

		private final FileCustomizer fileCustomizer;

		ImmutableCassandraConfig(Config config, Duration timeout, Version version,
				FileCustomizer fileCustomizer) {
			this.config = config;
			this.timeout = timeout;
			this.version = version;
			this.fileCustomizer = fileCustomizer;
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
		public FileCustomizer getFileCustomizer() {
			return this.fileCustomizer;
		}

	}

}
