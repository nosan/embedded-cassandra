/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.ClusterCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandra} and {@link CassandraDatabase}.
 *
 * @author Dmytro Nosan
 */
class EmbeddedCassandraTests {

	private final EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();

	private final Map<String, Object> configProperties = this.cassandraFactory.getConfigProperties();

	private final Map<String, Object> systemProperties = this.cassandraFactory.getSystemProperties();

	private final CassandraRunner runner = new CassandraRunner(this.cassandraFactory);

	@Test
	void testSuccessWhenDefault() throws Throwable {
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
			}
		});
	}

	@Test
	void testSuccessWhenCustomCassandraYaml() throws Throwable {
		this.cassandraFactory.setConfig(new ClassPathResource("cassandra.yaml"));
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
			}
			assertThat(cassandra.getPort()).isEqualTo(9142);
		});
	}

	@Test
	void testSuccessWhenOnlyNativeEnabled() throws Throwable {
		this.configProperties.put("start_native_transport", true);
		this.configProperties.put("start_rpc", false);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getAddress()).isNotNull();
			assertThat(cassandra.getPort()).isEqualTo(9042);
			assertThat(cassandra.getRpcPort()).isEqualTo(-1);
			assertThat(cassandra.getSslPort()).isEqualTo(-1);
		});
	}

	@Test
	void testSuccessWhenTransportDisabled() throws Throwable {
		this.configProperties.put("start_native_transport", false);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getAddress()).isNull();
			assertThat(cassandra.getPort()).isEqualTo(-1);
			assertThat(cassandra.getSslPort()).isEqualTo(-1);
			assertThat(cassandra.getRpcPort()).isEqualTo(-1);
		});
	}

	@Test
	void testSuccessWhenSslEnabled() throws Throwable {
		Resource keystore = new ClassPathResource("keystore.node0");
		Resource truststore = new ClassPathResource("truststore.node0");
		Map<String, Object> sslOptions = new LinkedHashMap<>();
		sslOptions.put("enabled", true);
		sslOptions.put("require_client_auth", true);
		sslOptions.put("optional", false);
		sslOptions.put("keystore", keystore.toPath().toString());
		sslOptions.put("keystore_password", "cassandra");
		sslOptions.put("truststore", truststore.toPath().toString());
		sslOptions.put("truststore_password", "cassandra");
		this.configProperties.put("native_transport_port_ssl", 9142);
		this.configProperties.put("client_encryption_options", sslOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getSslPort()).isEqualTo(9142);
			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
			clusterFactory.setSslEnabled(true);
			clusterFactory.setTruststore(truststore);
			clusterFactory.setTruststorePassword("cassandra");
			clusterFactory.setKeystore(keystore);
			clusterFactory.setKeystorePassword("cassandra");
			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
			}
		});
	}

	@Test
	void testSuccessWhenAuthenticatorEnabled() throws Throwable {
		this.configProperties.put("authenticator", "PasswordAuthenticator");
		this.configProperties.put("authorizer", "CassandraAuthorizer");
		this.systemProperties.put("cassandra.superuser_setup_delay_ms", 0);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
			clusterFactory.setPassword("cassandra");
			clusterFactory.setUsername("cassandra");
			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
			}
		});
	}

	@Test
	void testFailStartupTimeout() throws Throwable {
		this.cassandraFactory.setTimeout(Duration.ofSeconds(3));
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).isNotNull().hasStackTraceContaining("couldn't be started within 3000ms");
		});
	}

	@Test
	void testFailInvalidProperty() throws Throwable {
		this.configProperties.put("invalid_property", "");
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).isNotNull().hasStackTraceContaining("[invalid_property] from your cassandra.yaml");
		});
	}

	@Test
	void testFailPortsAreBusy() throws Throwable {
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.runner.run((cassandra2, throwable2) -> {
				assertThat(throwable2).isNotNull().hasStackTraceContaining("Address already in use");
			});
		});
	}

	private interface CassandraConsumer {

		void accept(Cassandra cassandra, @Nullable Throwable throwable) throws Throwable;

	}

	private static final class CassandraRunner {

		private final CassandraFactory cassandraFactory;

		CassandraRunner(CassandraFactory cassandraFactory) {
			this.cassandraFactory = cassandraFactory;
		}

		void run(CassandraConsumer consumer) throws Throwable {
			Cassandra cassandra = this.cassandraFactory.create();
			try {
				try {
					cassandra.start();
				}
				catch (Throwable ex) {
					consumer.accept(cassandra, ex);
					return;
				}
				consumer.accept(cassandra, null);
			}
			finally {
				cassandra.stop();
			}
		}

	}

}
