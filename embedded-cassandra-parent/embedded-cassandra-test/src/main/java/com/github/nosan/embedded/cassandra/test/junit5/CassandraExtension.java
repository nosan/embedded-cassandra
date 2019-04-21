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

package com.github.nosan.embedded.cassandra.test.junit5;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * JUnit {@link RegisterExtension Extension} that allows the Cassandra to be {@link Cassandra#start() started} and
 * {@link Cassandra#stop() stopped}.
 * <p>
 * The typical usage is:
 * <pre>
 * public class CassandraExtensionTests {
 * &#64;RegisterExtension
 * public static CassandraExtension cassandra = new CassandraExtension(&#47;* constructor parameters *&#47;);
 * &#64;Test
 * public void test() {
 *  //
 * }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @see CassandraFactory
 * @since 1.0.0
 */
public class CassandraExtension extends TestCassandra implements BeforeAllCallback, AfterAllCallback,
		ParameterResolver {

	/**
	 * Creates a {@link CassandraExtension}.
	 */
	public CassandraExtension() {
		super();
	}

	/**
	 * Creates a {@link CassandraExtension}.
	 *
	 * @param scripts CQL scripts to execute
	 */
	public CassandraExtension(CqlScript... scripts) {
		super(scripts);
	}

	/**
	 * Creates a {@link CassandraExtension}.
	 *
	 * @param cassandraFactory factory to create a {@link Cassandra}
	 * @param scripts CQL scripts to execute
	 */
	public CassandraExtension(@Nullable CassandraFactory cassandraFactory, CqlScript... scripts) {
		super(cassandraFactory, scripts);
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		start();
	}

	@Override
	public void afterAll(ExtensionContext context) {
		stop();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.getParameter().getType().isAssignableFrom(TestCassandra.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this;
	}

}
