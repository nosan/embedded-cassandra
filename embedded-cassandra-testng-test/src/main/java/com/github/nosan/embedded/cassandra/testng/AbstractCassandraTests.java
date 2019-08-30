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
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;

/**
 * Abstract TestNG {@code class} that allows the Cassandra to be {@link Cassandra#start() started} and {@link
 * Cassandra#stop() stopped}. Cassandra will be started only once before any test method is executed and stopped after
 * the last test method has executed.
 * <p>Example:
 * <pre>
 * class CassandraTests extends AbstractCassandraTests {
 *
 *     &#64;Test
 *     public void test() {
 *        Cassandra cassandra = getCassandra();
 *        //
 *     }
 *
 * }
 * </pre>
 * Default {@link EmbeddedCassandraFactory} could be customized via {@link CassandraFactoryCustomizer
 * CassandraFactoryCustomizers}.
 * <pre>
 * class CassandraTests extends AbstractCassandraTests {
 *
 *     public CassandraTests(){
 *         super(factory -&gt; factory.setPort(9042));
 *     }
 *
 *     &#64;Test
 *     public void test() {
 *        Cassandra cassandra = getCassandra();
 *        //
 *     }
 *
 * }
 * </pre>
 * It is possible to register you own factory to control Cassandra instance.
 * <p>Example:
 * <pre>
 * class CassandraTests extends AbstractCassandraTests {
 *
 *     public CassandraTests(){
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
 * @see CassandraFactoryCustomizer
 * @since 3.0.0
 */
public abstract class AbstractCassandraTests {

	private final Cassandra cassandra;

	/**
	 * Constructs a new {@link AbstractCassandraTests} with a default {@link CassandraFactory}. Defaults to {@link
	 * EmbeddedCassandraFactory} which is configured to use random ports.
	 */
	public AbstractCassandraTests() {
		this(EmbeddedCassandraFactory.random());
	}

	/**
	 * Constructs a new {@link AbstractCassandraTests} with a default {@link CassandraFactory} with the specified {@link
	 * CassandraFactoryCustomizer CassandraFactoryCustomizers}. Defaults to {@link EmbeddedCassandraFactory} which is
	 * configured to use random ports.
	 *
	 * @param customizers Any instances of this type will get a callback with the {@link EmbeddedCassandraFactory}
	 * before the {@link Cassandra} itself is started
	 */
	@SafeVarargs
	public AbstractCassandraTests(CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>... customizers) {
		Objects.requireNonNull(customizers, "'customizers' must not be null");
		EmbeddedCassandraFactory cassandraFactory = EmbeddedCassandraFactory.random();
		for (CassandraFactoryCustomizer<? super EmbeddedCassandraFactory> customizer : customizers) {
			customizer.customize(cassandraFactory);
		}
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
