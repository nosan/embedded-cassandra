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

	public CassandraConfigBuilder() {
		property(CONFIG).setDefault(new Config());
		property(TIMEOUT).setDefault(Duration.ofMinutes(1));
		property(VERSION).setDefault(Version.LATEST);
		property(RANDOM_PORTS).setDefault(false);
		property(FILE_CUSTOMIZER).setDefault(new CompositeFileCustomizer(
				new JVMOptionsFileCustomizer(), new EnvironmentFileCustomizer()));
	}

	/**
	 * Sets the cassandra version.
	 * @param version cassandra version.
	 * @return current builder
	 */
	public CassandraConfigBuilder version(Version version) {
		property(VERSION).set(version);
		return this;
	}

	/**
	 * Sets the cassandra startup timeout.
	 * @param timeout cassandra startup timeout.
	 * @return current builder.
	 */
	public CassandraConfigBuilder timeout(Duration timeout) {
		property(TIMEOUT).set(timeout);
		return this;
	}

	/**
	 * Sets the cassandra config.
	 * @param config cassandra config.
	 * @return current builder.
	 */
	public CassandraConfigBuilder config(Config config) {
		property(CONFIG).set(config);
		return this;
	}

	/**
	 * Sets the cassandra files customizer.
	 * @param fileCustomizer {@link FileCustomizer} file customizer.
	 * @return current builder.
	 */
	public CassandraConfigBuilder fileCustomizer(FileCustomizer fileCustomizer) {
		property(FILE_CUSTOMIZER).set(fileCustomizer);
		return this;
	}

	/**
	 * Use random ports or not.
	 * @param useRandomPorts use random port value.
	 * @return current builder.
	 */
	public CassandraConfigBuilder useRandomPorts(boolean useRandomPorts) {
		property(RANDOM_PORTS).set(useRandomPorts);
		return this;
	}

	@Override
	public CassandraConfig build() {
		Config config = get(CONFIG);
		if (property(RANDOM_PORTS).get()) {
			PortUtils.setRandomPorts(config, Objects::nonNull);
		}
		return new ImmutableCassandraConfig(config, get(TIMEOUT), get(VERSION),
				get(FILE_CUSTOMIZER));
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
