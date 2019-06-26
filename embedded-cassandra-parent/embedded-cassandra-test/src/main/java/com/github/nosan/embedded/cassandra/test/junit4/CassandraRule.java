/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.junit4;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.test.Connection;
import com.github.nosan.embedded.cassandra.test.ConnectionFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.test.TestCassandraBuilder;

/**
 * JUnit {@link TestRule} that allows the Cassandra to be {@link Cassandra#start() started} and {@link
 * Cassandra#stop() stopped}.
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see CassandraFactory
 * @see TestCassandraBuilder
 * @since 1.0.0
 */
public class CassandraRule extends TestCassandra implements TestRule {

	/**
	 * Creates a {@link CassandraRule} with default settings.
	 */
	public CassandraRule() {
		super();
	}

	/**
	 * Creates a {@link CassandraRule} with the given scripts.
	 *
	 * @param scripts CQL scripts to execute.
	 */
	public CassandraRule(CqlScript... scripts) {
		super(scripts);
	}

	/**
	 * Creates a {@link CassandraRule} with the given scripts and cassandra factory.
	 *
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute.
	 */
	public CassandraRule(@Nullable CassandraFactory cassandraFactory, CqlScript... scripts) {
		super(cassandraFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraRule} with the given scripts and connection factory.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param scripts CQL scripts to execute.
	 * @since 2.0.4
	 */
	public CassandraRule(@Nullable ConnectionFactory connectionFactory, CqlScript... scripts) {
		super(connectionFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraRule} with the given scripts , connection factory and cassandra factory.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute.
	 * @since 2.0.4
	 */
	public CassandraRule(@Nullable CassandraFactory cassandraFactory, @Nullable ConnectionFactory connectionFactory,
			CqlScript... scripts) {
		super(cassandraFactory, connectionFactory, scripts);
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				start();
				try {
					base.evaluate();
				}
				finally {
					stop();
				}
			}
		};
	}

}
