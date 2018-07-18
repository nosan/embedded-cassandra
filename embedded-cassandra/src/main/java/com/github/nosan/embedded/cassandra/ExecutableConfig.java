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

package com.github.nosan.embedded.cassandra;

import java.time.Duration;
import java.util.Objects;

import de.flapdoodle.embed.process.config.IExecutableProcessConfig;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.distribution.IVersion;

/**
 * Basic implementation of {@link IExecutableProcessConfig Executable Config}.
 *
 * @author Dmytro Nosan
 * @see com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder
 */
public final class ExecutableConfig implements IExecutableProcessConfig {

	private static final SupportConfig SUPPORT_CONFIG = new SupportConfig();

	private Config config = new Config();

	private Version version = Version.LATEST;

	private Duration timeout = Duration.ofMinutes(1);

	/**
	 * Retrieves cassandra's startup timeout.
	 *
	 * @return startup timeout.
	 */
	public Duration getTimeout() {
		return this.timeout;
	}

	/**
	 * Sets startup timeout.
	 *
	 * @param timeout startup timeout.
	 */
	public void setTimeout(Duration timeout) {
		this.timeout = Objects.requireNonNull(timeout, "Timeout must not be null");
	}

	/**
	 * Retrieves cassandra's version.
	 *
	 * @return {@link Version version}
	 * @see Version
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Sets version.
	 *
	 * @param version version to use.
	 */
	public void setVersion(Version version) {
		this.version = Objects.requireNonNull(version, "Version must not be null");
	}

	/**
	 * Retrieves cassandra's config.
	 *
	 * @return {@link Cassandra Cassandra Config}
	 * @see Config
	 */
	public Config getConfig() {
		return this.config;
	}

	/**
	 * Sets config.
	 *
	 * @param config {@link Config config} to use.
	 */
	public void setConfig(Config config) {
		this.config = Objects.requireNonNull(config, "Config must not be null");
	}

	/**
	 * Retrieves executable version.
	 *
	 * @return {@link ExecutableVersion Executable Version}
	 * @see ExecutableVersion
	 */
	@Override
	public IVersion version() {
		return new ExecutableVersion(this.version);
	}

	/**
	 * Retrieves support config.
	 *
	 * @return {@link ISupportConfig Support Config}
	 * @see SupportConfig
	 */
	@Override
	public ISupportConfig supportConfig() {
		return SUPPORT_CONFIG;
	}

}
