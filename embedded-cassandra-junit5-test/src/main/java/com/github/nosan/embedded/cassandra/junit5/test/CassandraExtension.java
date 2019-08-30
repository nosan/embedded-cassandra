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

package com.github.nosan.embedded.cassandra.junit5.test;

import java.lang.reflect.Parameter;
import java.util.Objects;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;

/**
 * JUnit5 {@link RegisterExtension Extension} that allows the Cassandra to be {@link Cassandra#start() started} and
 * {@link Cassandra#stop() stopped}. Cassandra will be started only once before any test method is executed and stopped
 * after the last test method has executed.
 * <p>Example:</p>
 * <pre>
 * &#64;ExtendWith(CassandraExtension.class)
 * class MyCassandraTests {
 *
 *     &#64;Test
 *     void test(Cassandra cassandra) {
 *      //
 *     }
 *
 * }
 * </pre>
 * In contrast to {@code @ExtendWith} which is used to register extensions declaratively, {@code @RegisterExtension} can
 * be used to register an extension programmatically to pass arguments to the {@code CassandraExtension's} constructor.
 * <p>Example:</p>
 * <pre>
 * class CassandraTests {
 *
 *     &#64;RegisterExtension
 *     static final CassandraExtension cassandraExtension = new CassandraExtension(factory -&gt; factory.setPort(9042));
 *
 *     &#64;Test
 *     void test() {
 *      Cassandra cassandra = cassandraExtension.getCassandra();
 *      //
 *     }
 * }
 * </pre>
 * It is possible to register you own factory to control Cassandra instance.
 * <p>Example:</p>
 * <pre>
 * class CassandraTests {
 *
 *     &#64;RegisterExtension
 *     static final CassandraExtension cassandraExtension = new CassandraExtension(createCassandraFactory());
 *
 *     &#64;Test
 *     void test() {
 *      Cassandra cassandra = cassandraExtension.getCassandra();
 *      //
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
public class CassandraExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

	private final Cassandra cassandra;

	/**
	 * Constructs a new {@link CassandraExtension} with a default {@link CassandraFactory}. Defaults to {@link
	 * EmbeddedCassandraFactory} which is configured to use random ports.
	 */
	public CassandraExtension() {
		this(EmbeddedCassandraFactory.random());
	}

	/**
	 * Constructs a new {@link CassandraExtension} with a default {@link CassandraFactory} with the specified {@link
	 * CassandraFactoryCustomizer CassandraFactoryCustomizers}. Defaults to {@link EmbeddedCassandraFactory} which is
	 * configured to use random ports.
	 *
	 * @param customizers Any instances of this type will get a callback with the {@link EmbeddedCassandraFactory}
	 * before the {@link Cassandra} itself is started
	 */
	@SafeVarargs
	public CassandraExtension(CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>... customizers) {
		Objects.requireNonNull(customizers, "'customizers' must not be null");
		EmbeddedCassandraFactory cassandraFactory = EmbeddedCassandraFactory.random();
		for (CassandraFactoryCustomizer<? super EmbeddedCassandraFactory> customizer : customizers) {
			customizer.customize(cassandraFactory);
		}
		this.cassandra = cassandraFactory.create();
	}

	/**
	 * Constructs a new {@link CassandraExtension} with a specified {@link CassandraFactory}.
	 *
	 * @param cassandraFactory {@link CassandraFactory} that creates and configure a {@link Cassandra}.
	 */
	public CassandraExtension(CassandraFactory cassandraFactory) {
		Objects.requireNonNull(cassandraFactory, "'cassandraFactory' must not be null");
		this.cassandra = Objects.requireNonNull(cassandraFactory.create(), "'cassandra' must not be null");
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		this.cassandra.start();
	}

	@Override
	public void afterAll(ExtensionContext context) {
		this.cassandra.stop();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Parameter parameter = parameterContext.getParameter();
		Class<?> type = parameter.getType();
		return type.isAssignableFrom(Cassandra.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.cassandra;
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
		return "CassandraExtension [" + this.cassandra + "]";
	}

}
