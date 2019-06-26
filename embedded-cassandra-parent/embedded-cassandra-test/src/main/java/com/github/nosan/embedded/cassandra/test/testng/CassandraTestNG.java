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

package com.github.nosan.embedded.cassandra.test.testng;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.test.Connection;
import com.github.nosan.embedded.cassandra.test.ConnectionFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * Base {@code test class} that allows the Cassandra to be {@link Cassandra#start() started} and {@link Cassandra#stop()
 * stopped}.
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see CassandraFactory
 * @since 1.0.0
 */
public class CassandraTestNG extends TestCassandra {

	/**
	 * Creates a {@link CassandraTestNG} with default settings.
	 */
	public CassandraTestNG() {
		super();
	}

	/**
	 * Creates a {@link CassandraTestNG} with the given scripts.
	 *
	 * @param scripts CQL scripts to execute.
	 */
	public CassandraTestNG(CqlScript... scripts) {
		super(scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG} with the given scripts and cassandra factory.
	 *
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute.
	 */
	public CassandraTestNG(@Nullable CassandraFactory cassandraFactory, CqlScript... scripts) {
		super(cassandraFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG} with the given scripts and connection factory.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param scripts CQL scripts to execute.
	 * @since 2.0.4
	 */
	public CassandraTestNG(@Nullable ConnectionFactory connectionFactory, CqlScript... scripts) {
		super(connectionFactory, scripts);
	}

	/**
	 * Creates a {@link CassandraTestNG} with the given scripts , connection factory and {@code
	 * CassandraFactory}.
	 *
	 * @param connectionFactory factory that creates {@link Connection}
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts CQL scripts to execute.
	 * @since 2.0.4
	 */
	public CassandraTestNG(@Nullable CassandraFactory cassandraFactory, @Nullable ConnectionFactory connectionFactory,
			CqlScript... scripts) {
		super(cassandraFactory, connectionFactory, scripts);
	}

	@BeforeClass(alwaysRun = true)
	@Override
	public void start() throws CassandraException {
		super.start();
	}

	@AfterClass(alwaysRun = true)
	@Override
	public void stop() throws CassandraException {
		super.stop();
	}

}
