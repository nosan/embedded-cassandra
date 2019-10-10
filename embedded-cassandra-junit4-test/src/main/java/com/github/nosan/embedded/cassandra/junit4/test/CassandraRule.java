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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraCreationException;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.connection.DefaultCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;

/**
 * JUnit4 {@link TestRule} that allows the Cassandra to be {@link Cassandra#start() started} and {@link Cassandra#stop()
 * stopped}. Cassandra declared as static field with a {@code ClassRule} annotation will be started only once before any
 * test method is executed and stopped after the last test method has executed. Cassandra declared as instance field
 * with a {@code Rule} annotation will be started and stopped for every test method.
 * <p>Example:
 * <pre>
 * class CassandraTests {
 *
 *     &#64;ClassRule
 *     public static final CassandraRule CASSANDRA_RULE = new CassandraRule();
 *
 *     &#64;Test
 *     public void test() {
 *     //
 *     }
 * }
 * </pre>
 * <p><strong>Exposed properties:</strong>
 * The following properties will be added to {@code System Properties} after {@link Cassandra} has started:
 * <pre>
 *     - embedded.cassandra.version
 *     - embedded.cassandra.address
 *     - embedded.cassandra.port
 *     - embedded.cassandra.ssl-port
 *     - embedded.cassandra.rpc-port
 * </pre>
 * <p>
 * Use {@link #withExposeProperties}  to disable properties exposing.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class CassandraRule extends ExternalResource {

	private CassandraFactory cassandraFactory;

	private CassandraConnectionFactory cassandraConnectionFactory;

	private CqlDataSet dataSet;

	private boolean exposeProperties;

	@Nullable
	private volatile Cassandra cassandra;

	@Nullable
	private volatile CassandraConnection cassandraConnection;

	/**
	 * Constructs a new {@link CassandraRule} with a default {@link CassandraFactory} with the specified {@link
	 * CassandraFactoryCustomizer}(s). The default factory is {@link EmbeddedCassandraFactory} which is configured to
	 * use random ports.
	 *
	 * @param customizers Any instances of this type will get a callback with the {@link EmbeddedCassandraFactory}
	 * before the {@link Cassandra} itself is started
	 */
	@SafeVarargs
	public CassandraRule(CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>... customizers) {
		this.cassandraFactory = new DefaultCassandraFactory(customizers);
		this.cassandraConnectionFactory = new DefaultCassandraConnectionFactory();
		this.dataSet = CqlDataSet.empty();
		this.exposeProperties = true;
	}

	/**
	 * Set a {@link CassandraFactory}.This factory will be used to create a {@link Cassandra}.
	 *
	 * @param cassandraFactory factory that can be used to create and configure {@link Cassandra}
	 * @return this instance
	 */
	public CassandraRule withCassandraFactory(CassandraFactory cassandraFactory) {
		Objects.requireNonNull(cassandraFactory, "'cassandraFactory' must not be null");
		this.cassandraFactory = cassandraFactory;
		return this;
	}

	/**
	 * Set a {@link CassandraConnectionFactory}. Factory will be used to create a {@link #getCassandraConnection()
	 * connection}. The latest used for {@link CqlDataSet} execution.
	 *
	 * @param cassandraConnectionFactory factory that can be used to create and configure {@link CassandraConnection}
	 * @return this instance
	 */
	public CassandraRule withCassandraConnectionFactory(CassandraConnectionFactory cassandraConnectionFactory) {
		Objects.requireNonNull(cassandraConnectionFactory, "'cassandraConnectionFactory' must not be null");
		this.cassandraConnectionFactory = cassandraConnectionFactory;
		return this;
	}

	/**
	 * Set a {@link CqlDataSet}. CQL statements will be executed immediately after Cassandra start.
	 *
	 * @param dataSet a {@link CqlDataSet} to execute
	 * @return this instance
	 */
	public CassandraRule withCqlDataSet(CqlDataSet dataSet) {
		Objects.requireNonNull(dataSet, "'dataSet' must not be null");
		this.dataSet = dataSet;
		return this;
	}

	/**
	 * Sets if {@link CassandraRule} should add {@link Cassandra}'s properties such as {@code embedded.cassandra.port}
	 * to System Properties after start.
	 *
	 * @param exposeProperties if the properties should be added
	 * @return this instance
	 */
	public CassandraRule withExposeProperties(boolean exposeProperties) {
		this.exposeProperties = exposeProperties;
		return this;
	}

	/**
	 * Returns the {@link Cassandra} instance.
	 *
	 * @return the cassandra
	 */
	public Cassandra getCassandra() {
		Cassandra cassandra = this.cassandra;
		if (cassandra == null) {
			synchronized (this) {
				cassandra = this.cassandra;
				if (cassandra == null) {
					cassandra = this.cassandraFactory.create();
					this.cassandra = cassandra;
				}
			}
		}
		return cassandra;
	}

	/**
	 * Returns the {@link CassandraConnection} to the {@link Cassandra}.
	 *
	 * @return the connection
	 */
	public CassandraConnection getCassandraConnection() {
		CassandraConnection connection = this.cassandraConnection;
		if (connection == null) {
			synchronized (this) {
				connection = this.cassandraConnection;
				if (connection == null) {
					connection = this.cassandraConnectionFactory.create(getCassandra());
					this.cassandraConnection = connection;
				}
			}
		}
		return connection;
	}

	/**
	 * Returns the {@link CassandraConnection#getConnection()}.
	 *
	 * @param <T> the native connection type
	 * @param connectionType the connection type
	 * @return a native connection
	 * @throws ClassCastException if the native connection is not assignable to the type {@code T}.
	 */
	public <T> T getConnection(Class<? extends T> connectionType) throws ClassCastException {
		Objects.requireNonNull(connectionType, "'connectionType' must not be null");
		CassandraConnection cassandraConnection = getCassandraConnection();
		return connectionType.cast(cassandraConnection.getConnection());
	}

	@Override
	protected synchronized void before() {
		List<String> statements = this.dataSet.getStatements();
		Cassandra cassandra = getCassandra();
		cassandra.start();
		if (!statements.isEmpty()) {
			CassandraConnection cassandraConnection = getCassandraConnection();
			statements.forEach(cassandraConnection::execute);
		}
		if (this.exposeProperties) {
			Map<String, Object> properties = new LinkedHashMap<>();
			InetAddress address = cassandra.getAddress();
			if (address != null) {
				properties.put("embedded.cassandra.address", address.getHostAddress());
			}
			int port = cassandra.getPort();
			if (port != -1) {
				properties.put("embedded.cassandra.port", Objects.toString(port));
			}
			int sslPort = cassandra.getSslPort();
			if (sslPort != -1) {
				properties.put("embedded.cassandra.ssl-port", Objects.toString(sslPort));
			}
			int rpcPort = cassandra.getRpcPort();
			if (rpcPort != -1) {
				properties.put("embedded.cassandra.rpc-port", Objects.toString(rpcPort));
			}
			properties.put("embedded.cassandra.version", Objects.toString(cassandra.getVersion()));
			System.getProperties().putAll(properties);
		}
	}

	@Override
	protected synchronized void after() {
		CassandraConnection connection = this.cassandraConnection;
		if (connection != null) {
			try {
				connection.close();
			}
			catch (Throwable ex) {
				//ignore
			}
		}
		Cassandra cassandra = this.cassandra;
		if (cassandra != null) {
			cassandra.stop();
		}
		if (this.exposeProperties) {
			System.clearProperty("embedded.cassandra.address");
			System.clearProperty("embedded.cassandra.port");
			System.clearProperty("embedded.cassandra.ssl-port");
			System.clearProperty("embedded.cassandra.rpc-port");
			System.clearProperty("embedded.cassandra.version");
		}
	}

	private static final class DefaultCassandraFactory implements CassandraFactory {

		private final List<CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>> customizers;

		@SafeVarargs
		DefaultCassandraFactory(CassandraFactoryCustomizer<? super EmbeddedCassandraFactory>... customizers) {
			Objects.requireNonNull(customizers, "'customizers' must not be null");
			this.customizers = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(customizers)));
		}

		@Override
		public Cassandra create() throws CassandraCreationException {
			EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
			cassandraFactory.setPort(0);
			cassandraFactory.setRpcPort(0);
			cassandraFactory.setJmxLocalPort(0);
			cassandraFactory.setStoragePort(0);
			this.customizers.forEach(customizer -> customizer.customize(cassandraFactory));
			return cassandraFactory.create();
		}

	}

}
