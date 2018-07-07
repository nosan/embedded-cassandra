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

import java.time.Duration;

import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.builder.TypedProperty;

import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.Version;

/**
 * Simple {@link ExecutableConfig Cassandra Executable Config} builder.
 *
 * @author Dmytro Nosan
 */
public class ExecutableConfigBuilder extends AbstractBuilder<ExecutableConfig> {

	private static final TypedProperty<Config> CONFIG = TypedProperty.with("Config",
			Config.class);

	private static final TypedProperty<Duration> TIMEOUT = TypedProperty.with("Timeout",
			Duration.class);

	private static final TypedProperty<Version> VERSION = TypedProperty.with("Version",
			Version.class);

	public ExecutableConfigBuilder() {
		property(CONFIG).overwriteDefault(new Config());
		property(TIMEOUT).overwriteDefault(Duration.ofMinutes(1));
		property(VERSION).overwriteDefault(Version.LATEST);

	}

	/**
	 * Sets version.
	 *
	 * @param version version to use.
	 * @return current builder
	 */
	public ExecutableConfigBuilder version(Version version) {
		property(VERSION).set(version);
		return this;
	}

	/**
	 * Sets startup timeout.
	 *
	 * @param timeout startup timeout.
	 * @return current builder.
	 */
	public ExecutableConfigBuilder timeout(Duration timeout) {
		property(TIMEOUT).set(timeout);
		return this;
	}

	/**
	 * Sets config.
	 *
	 * @param config {@link Config config} to use.
	 * @return current builder.
	 */
	public ExecutableConfigBuilder config(Config config) {
		property(CONFIG).set(config);
		return this;
	}

	@Override
	public ExecutableConfig build() {
		ExecutableConfig executableConfig = new ExecutableConfig();
		executableConfig.setConfig(get(CONFIG));
		executableConfig.setTimeout(get(TIMEOUT));
		executableConfig.setVersion(get(VERSION));
		return executableConfig;
	}

}
