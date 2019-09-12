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

package com.github.nosan.embedded.cassandra;

import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandra} and {@link Database}.
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
			try (Cluster cluster = new ClusterFactory().create(cassandra)) {
				createKeyspace(cluster.connect());
			}
			int port = cassandra.getPort();
			InetAddress address = cassandra.getAddress();
			assertThat(address).isNotNull();
			hasSystemProperty("embedded.cassandra.port", port);
			hasSystemProperty("embedded.cassandra.address", address.getHostAddress());
			cassandra.stop();
			doesNotContainSystemProperty("embedded.cassandra.port");
			doesNotContainSystemProperty("embedded.cassandra.address");
		});
	}

	@Test
	void testSuccessWhenCustomCassandraYaml() throws Throwable {
		this.cassandraFactory.setConfig(new ClassPathResource("cassandra.yaml"));
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			try (Cluster cluster = new ClusterFactory().create(cassandra)) {
				createKeyspace(cluster.connect());
			}
			assertThat(cassandra.getPort()).isEqualTo(9142);
		});
	}

	@Test
	void testSuccessWhenPropertiesNotExposed() throws Throwable {
		this.cassandraFactory.setExposeProperties(false);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			doesNotContainSystemProperty("embedded.cassandra.address");
			doesNotContainSystemProperty("embedded.cassandra.port");
			doesNotContainSystemProperty("embedded.cassandra.ssl-port");
			doesNotContainSystemProperty("embedded.cassandra.rpc-port");
			doesNotContainSystemProperty("embedded.cassandra.version");
		});
	}

	@Test
	void testSuccessWhenOnlyNativeEnabled() throws Throwable {
		this.configProperties.put("start_rpc", false);
		this.configProperties.put("start_native_transport", true);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getAddress()).isNotNull();
			assertThat(cassandra.getPort()).isEqualTo(9042);
			assertThat(cassandra.getRpcPort()).isEqualTo(-1);
			assertThat(cassandra.getSslPort()).isEqualTo(-1);
			hasSystemProperty("embedded.cassandra.port", 9042);
		});
	}

	@Test
	void testSuccessWhenOnlyRpcEnabled() throws Throwable {
		this.configProperties.put("start_rpc", true);
		this.configProperties.put("start_native_transport", false);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getAddress()).isNotNull();
			assertThat(cassandra.getPort()).isEqualTo(-1);
			assertThat(cassandra.getRpcPort()).isEqualTo(9160);
			assertThat(cassandra.getSslPort()).isEqualTo(-1);
			hasSystemProperty("embedded.cassandra.rpc-port", 9160);
		});
	}

	@Test
	void testSuccessWhenTransportEnabled() throws Throwable {
		this.configProperties.put("start_rpc", true);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getAddress()).isNotNull();
			assertThat(cassandra.getPort()).isEqualTo(9042);
			assertThat(cassandra.getRpcPort()).isEqualTo(9160);
			assertThat(cassandra.getSslPort()).isEqualTo(-1);
			hasSystemProperty("embedded.cassandra.port", 9042);
			hasSystemProperty("embedded.cassandra.rpc-port", 9160);
		});
	}

	@Test
	void testSuccessWhenTransportDisabled() throws Throwable {
		this.configProperties.put("start_rpc", false);
		this.configProperties.put("start_native_transport", false);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getAddress()).isNull();
			assertThat(cassandra.getPort()).isEqualTo(-1);
			assertThat(cassandra.getSslPort()).isEqualTo(-1);
			assertThat(cassandra.getRpcPort()).isEqualTo(-1);
			doesNotContainSystemProperty("embedded.cassandra.port");
			doesNotContainSystemProperty("embedded.cassandra.rpc-port");
			doesNotContainSystemProperty("embedded.cassandra.address");
			doesNotContainSystemProperty("embedded.cassandra.ssl-port");
		});
	}

	@Test
	void testSuccessWhenSslEnabled() throws Throwable {
		Path keystore = new ClassPathResource("keystore.node0").toPath();
		Path truststore = new ClassPathResource("truststore.node0").toPath();
		Map<String, Object> sslOptions = new LinkedHashMap<>();
		sslOptions.put("enabled", true);
		sslOptions.put("require_client_auth", true);
		sslOptions.put("optional", false);
		sslOptions.put("keystore", keystore.toString());
		sslOptions.put("keystore_password", "cassandra");
		sslOptions.put("truststore", truststore.toString());
		sslOptions.put("truststore_password", "cassandra");
		this.configProperties.put("native_transport_port_ssl", 9142);
		this.configProperties.put("client_encryption_options", sslOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getSslPort()).isEqualTo(9142);
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.setSslEnabled(true);
			clusterFactory.setTruststorePath(truststore);
			clusterFactory.setTruststorePassword("cassandra");
			clusterFactory.setKeystorePath(keystore);
			clusterFactory.setKeystorePassword("cassandra");
			try (Cluster cluster = clusterFactory.create(cassandra)) {
				createKeyspace(cluster.connect());
			}
			hasSystemProperty("embedded.cassandra.ssl-port", 9142);
		});
	}

	@Test
	void testSuccessWhenAuthenticatorEnabled() throws Throwable {
		this.configProperties.put("authenticator", "PasswordAuthenticator");
		this.configProperties.put("authorizer", "CassandraAuthorizer");
		this.systemProperties.put("cassandra.superuser_setup_delay_ms", 0);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.setPassword("cassandra");
			clusterFactory.setUsername("cassandra");
			try (Cluster cluster = clusterFactory.create(cassandra)) {
				createKeyspace(cluster.connect());
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

	private static void hasSystemProperty(String name, Object value) {
		assertThat(System.getProperties()).containsEntry(name, value.toString());
	}

	private static void doesNotContainSystemProperty(String name) {
		assertThat(System.getProperties()).doesNotContainKeys(name);
	}

	private static void createKeyspace(Session session) {
		session.execute("CREATE KEYSPACE test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}");
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
