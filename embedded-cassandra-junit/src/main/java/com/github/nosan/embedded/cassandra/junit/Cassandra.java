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

package com.github.nosan.embedded.cassandra.junit;

import java.util.Objects;

import com.github.nosan.embedded.cassandra.CassandraExecutable;
import com.github.nosan.embedded.cassandra.CassandraStarter;
import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import com.github.nosan.embedded.cassandra.config.CassandraConfigBuilder;
import com.github.nosan.embedded.cassandra.config.CassandraRuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for running an embedded cassandra.
 *
 * @author Dmytro Nosan
 * @see CassandraRuntimeConfigBuilder
 * @see CassandraConfigBuilder
 */
public class Cassandra implements TestRule {

	private final IRuntimeConfig runtimeConfig;

	private final CassandraConfig cassandraConfig;

	public Cassandra(IRuntimeConfig runtimeConfig, CassandraConfig cassandraConfig) {
		this.runtimeConfig = Objects.requireNonNull(runtimeConfig,
				"RuntimeConfig must not be null");
		this.cassandraConfig = Objects.requireNonNull(cassandraConfig,
				"Cassandra Config must not be null");
	}

	public Cassandra(IRuntimeConfig runtimeConfig) {
		this(runtimeConfig, new CassandraConfigBuilder().useRandomPorts(true).build());
	}

	public Cassandra(CassandraConfig cassandraConfig) {
		this(new CassandraRuntimeConfigBuilder().defaults().build(), cassandraConfig);
	}

	public Cassandra() {
		this(new CassandraRuntimeConfigBuilder().defaults().build(),
				new CassandraConfigBuilder().useRandomPorts(true).build());
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
	public CassandraConfig getCassandraConfig() {
		return this.cassandraConfig;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		CassandraConfig cassandraConfig = getCassandraConfig();
		IRuntimeConfig runtimeConfig = getRuntimeConfig();
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				CassandraStarter cassandraStarter = new CassandraStarter(runtimeConfig);
				CassandraExecutable executable = cassandraStarter
						.prepare(cassandraConfig);
				executable.start();
				try {
					base.evaluate();
				}
				finally {
					executable.stop();
				}
			}
		};
	}

}
