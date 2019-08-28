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

package com.github.nosan.embedded.cassandra.testng;

import java.util.Objects;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;

/**
 * Abstract TestNG {@code class} that allows the Cassandra to be {@link Cassandra#start() started} and {@link
 * Cassandra#stop() stopped}. Cassandra will be started only once before any test method is executed and stopped after
 * the last test method has executed.
 * <p>Example:
 * <pre>
 * class MyCassandraTests extends AbstractCassandraTests {
 *
 *     public MyCassandraTests(){
 *         super(createCassandraFactory());
 *     }
 *
 *     &#64;Test
 *     public void test() {
 *        Cassandra cassandra = getCassandra();
 *        //
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
public abstract class AbstractCassandraTests {

	private final Cassandra cassandra;

	/**
	 * Constructs a new {@link AbstractCassandraTests} with a default {@link CassandraFactory}. Defaults to {@link
	 * EmbeddedCassandraFactory} which is configured to use random ports.
	 */
	public AbstractCassandraTests() {
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		cassandraFactory.setPort(0);
		cassandraFactory.setRpcPort(0);
		cassandraFactory.setJmxLocalPort(0);
		cassandraFactory.setStoragePort(0);
		this.cassandra = cassandraFactory.create();
	}

	/**
	 * Constructs a new {@link AbstractCassandraTests} with a specified {@link CassandraFactory}.
	 *
	 * @param cassandraFactory {@link CassandraFactory} that creates and configure a {@link Cassandra}.
	 */
	public AbstractCassandraTests(CassandraFactory cassandraFactory) {
		Objects.requireNonNull(cassandraFactory, "'cassandraFactory' must not be null");
		this.cassandra = Objects.requireNonNull(cassandraFactory.create(), "'cassandra' must not be null");
	}

	/**
	 * Starts the Cassandra.
	 */
	@BeforeClass(alwaysRun = true)
	public final void startCassandra() {
		this.cassandra.start();
	}

	/**
	 * Stops the Cassandra.
	 */
	@AfterClass(alwaysRun = true)
	public final void stopCassandra() {
		this.cassandra.stop();
	}

	/**
	 * Returns the reference to the {@link Cassandra}.
	 *
	 * @return the cassandra instance.
	 */
	public final Cassandra getCassandra() {
		return this.cassandra;
	}

	public String toString() {
		return "AbstractCassandraTests [" + this.cassandra + "]";
	}

}