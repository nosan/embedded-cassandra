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

import java.util.Objects;

import de.flapdoodle.embed.process.config.IExecutableProcessConfig;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.distribution.IVersion;

/**
 * Basic implementation of {@link IExecutableProcessConfig Executable Config}.
 *
 * @author Dmytro Nosan
 */
public final class ExecutableConfig implements IExecutableProcessConfig {

	private final CassandraConfig cassandraConfig;

	private static final SupportConfig SUPPORT_CONFIG = new SupportConfig();

	public ExecutableConfig(CassandraConfig cassandraConfig) {
		this.cassandraConfig = Objects.requireNonNull(cassandraConfig,
				"Cassandra Config must not be null");
	}

	/**
	 * Retrieves executable version.
	 * @return {@link ExecutableVersion Executable Version}
	 * @see ExecutableVersion
	 */
	@Override
	public IVersion version() {
		return new ExecutableVersion(this.cassandraConfig.getVersion());
	}

	/**
	 * Retrieves Cassandra's config.
	 * @return {@link CassandraConfig Cassandra Config}
	 * @see CassandraConfig
	 */
	public CassandraConfig getCassandraConfig() {
		return this.cassandraConfig;
	}

	/**
	 * Retrieves support config.
	 * @return {@link ISupportConfig Support Config}
	 * @see SupportConfig
	 */
	@Override
	public ISupportConfig supportConfig() {
		return SUPPORT_CONFIG;
	}

}
