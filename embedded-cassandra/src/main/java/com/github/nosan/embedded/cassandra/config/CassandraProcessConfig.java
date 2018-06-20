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

import com.github.nosan.embedded.cassandra.CassandraVersion;
import de.flapdoodle.embed.process.config.IExecutableProcessConfig;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.distribution.IVersion;

/**
 * An Embedded Cassandra {@link IExecutableProcessConfig process config}.
 *
 * @author Dmytro Nosan
 */
public class CassandraProcessConfig implements IExecutableProcessConfig {

	private static final ISupportConfig SUPPORT_CONFIG = new CassandraSupportConfig();

	private CassandraVersion version = CassandraVersion.LATEST;

	private Duration timeout = Duration.ofMinutes(1);

	private CassandraConfig config = new CassandraConfig();

	/**
	 * Get an embedded cassandra version.
	 * @return {@link IVersion} retrieves cassandra version.
	 */
	@Override
	public IVersion version() {
		return this.version;
	}

	/**
	 * Retrieves support config.
	 * @return {@link ISupportConfig} support config.
	 * @see CassandraSupportConfig
	 */
	@Override
	public ISupportConfig supportConfig() {
		return SUPPORT_CONFIG;
	}

	/**
	 * Sets the cassandra version.
	 * @param version cassandra version.
	 */
	public void setVersion(CassandraVersion version) {
		this.version = Objects.requireNonNull(version, "Version must not be null");
	}

	/**
	 * Retrieves the cassandra version.
	 * @return cassandra version
	 */
	public CassandraVersion getVersion() {
		return this.version;
	}

	/**
	 * Sets the cassandra config.
	 * @param config cassandra config.
	 */
	public void setConfig(CassandraConfig config) {
		this.config = Objects.requireNonNull(config, "Config must not be null");
	}

	/**
	 * Retrieves an embedded cassandra config.
	 * @return Cassandra config.
	 */
	public CassandraConfig getConfig() {
		return this.config;
	}

	/**
	 * Sets the cassandra startup timeout.
	 * @param timeout cassandra startup timeout.
	 */
	public void setTimeout(Duration timeout) {
		this.timeout = Objects.requireNonNull(timeout, "Timeout must not be null");
	}

	/**
	 * Retrieves an embedded cassandra startup timeout.
	 * @return Cassandra startup timeout.
	 */
	public Duration getTimeout() {
		return this.timeout;
	}

}
