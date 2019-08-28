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

package com.github.nosan.embedded.cassandra.junit4.test;

import java.util.Objects;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;

/**
 * JUnit4 {@link TestRule} that allows the Cassandra to be {@link Cassandra#start() started} and {@link Cassandra#stop()
 * stopped}. Cassandra declared as static field with a {@code ClassRule} annotation will be started only once before any
 * test method is executed and stopped after the last test method has executed. Cassandra declared as instance field
 * with a {@code Rule} annotation will be started and stopped for every test method.
 * <p>Example:
 * <pre>
 * class MyCassandraTests {
 *
 *     &#64;ClassRule
 *     public static final CassandraRule cassandraRule = new CassandraRule(createCassandraFactory());
 *
 *     &#64;Test
 *     public void test() {
 *       Cassandra cassandra = cassandraRule.getCassandra();
 *       //
 *     }
 *
 *     private static EmbeddedCassandraFactory createCassandraFactory() {
 *         EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
 *         //...
 *         return cassandraFactory;
 *    }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see EmbeddedCassandraFactory
 * @since 3.0.0
 */
public class CassandraRule implements TestRule {

	private final Cassandra cassandra;

	/**
	 * Constructs a new {@link CassandraRule} with a default {@link CassandraFactory}. Defaults to {@link
	 * EmbeddedCassandraFactory} which is configured to use random ports.
	 */
	public CassandraRule() {
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		cassandraFactory.setPort(0);
		cassandraFactory.setRpcPort(0);
		cassandraFactory.setJmxLocalPort(0);
		cassandraFactory.setStoragePort(0);
		this.cassandra = cassandraFactory.create();
	}

	/**
	 * Constructs a new {@link CassandraRule} with a specified {@link CassandraFactory}.
	 *
	 * @param cassandraFactory {@link CassandraFactory} that creates and configure a {@link Cassandra}.
	 */
	public CassandraRule(CassandraFactory cassandraFactory) {
		Objects.requireNonNull(cassandraFactory, "'cassandraFactory' must not be null");
		this.cassandra = Objects.requireNonNull(cassandraFactory.create(), "'cassandra' must not be null");
	}

	@Override
	public Statement apply(Statement base, Description description) {
		Cassandra cassandra = this.cassandra;
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				cassandra.start();
				try {
					base.evaluate();
				}
				finally {
					cassandra.stop();
				}
			}
		};
	}

	/**
	 * Returns the reference to the {@link Cassandra}.
	 *
	 * @return the cassandra
	 */
	public final Cassandra getCassandra() {
		return this.cassandra;
	}

	@Override
	public String toString() {
		return "CassandraRule [" + this.cassandra + "]";
	}

}