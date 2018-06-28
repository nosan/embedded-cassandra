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

package com.github.nosan.embedded.cassandra.jupiter;

import java.util.Objects;

import com.github.nosan.embedded.cassandra.CassandraExecutable;
import com.github.nosan.embedded.cassandra.CassandraStarter;
import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.config.ExecutableConfigBuilder;
import com.github.nosan.embedded.cassandra.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link Extension} for running an embedded cassandra.
 *
 * @author Dmytro Nosan
 * @see RuntimeConfigBuilder
 * @see ExecutableConfigBuilder
 */
public class Cassandra implements BeforeAllCallback, AfterAllCallback {

	private static final String CASSANDRA_EXECUTABLE = "CassandraExecutable";

	private final IRuntimeConfig runtimeConfig;

	private final ExecutableConfig executableConfig;

	public Cassandra(IRuntimeConfig runtimeConfig, ExecutableConfig executableConfig) {
		this.runtimeConfig = Objects.requireNonNull(runtimeConfig,
				"RuntimeConfig must not be null");
		this.executableConfig = Objects.requireNonNull(executableConfig,
				"Cassandra Config must not be null");
	}

	public Cassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	public Cassandra(ExecutableConfig executableConfig) {
		this(new RuntimeConfigBuilder().build(), executableConfig);
	}

	public Cassandra() {
		this(new RuntimeConfigBuilder().build(),
				new ExecutableConfigBuilder().useRandomPorts(true).build());
	}

	/**
	 * Retrieves an embedded cassandra runtime config.
	 * @return the runtime config.
	 */
	public IRuntimeConfig getRuntimeConfig() {
		return this.runtimeConfig;
	}

	/**
	 * Retrieves an embedded cassandra config.
	 * @return Cassandra config.
	 */
	public ExecutableConfig getExecutableConfig() {
		return this.executableConfig;
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		ExecutableConfig executableConfig = getExecutableConfig();
		IRuntimeConfig runtimeConfig = getRuntimeConfig();
		CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig);
		CassandraExecutable executable = cassandraStarter.prepare(executableConfig);
		getStore(context).put(CASSANDRA_EXECUTABLE, executable);
		executable.start();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		CassandraExecutable executable = getStore(context).get(CASSANDRA_EXECUTABLE,
				CassandraExecutable.class);
		if (executable != null) {
			executable.stop();
		}

	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(ExtensionContext.Namespace.create(getClass()));
	}

}
