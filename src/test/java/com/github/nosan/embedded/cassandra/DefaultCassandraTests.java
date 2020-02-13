/*
 * Copyright 2020 the original author or authors.
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

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO leave javadoc here!
 *
 * @author Dmytro Nosan
 */
class DefaultCassandraTests {

	//	private final CassandraBuilder builder = new CassandraBuilder();

	//	@Test
	//	void testSuccessWhenDefault() throws Throwable {
	//		run((cassandra, throwable) -> {
	//			assertThat(throwable).doesNotThrowAnyException();
	//			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
	//			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
	//				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
	//			}
	//		});
	//	}
	//
	//	@Test
	//	void testSuccessWhenCustomCassandraYaml() throws Throwable {
	//		this.builder.setConfig(new ClassPathResource("cassandra.yaml"));
	//		run((cassandra, throwable) -> {
	//			assertThat(throwable).doesNotThrowAnyException();
	//			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
	//			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
	//				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
	//			}
	//			assertThat(cassandra.getPort()).isEqualTo(9142);
	//		});
	//	}
	//
	//	@Test
	//	void testSuccessWhenOnlyNativeEnabled() throws Throwable {
	//		this.configProperties.put("start_native_transport", true);
	//		run((cassandra, throwable) -> {
	//			assertThat(throwable).doesNotThrowAnyException();
	//			assertThat(cassandra.getAddress()).isNotNull();
	//			assertThat(cassandra.getPort()).isEqualTo(9042);
	//			assertThat(cassandra.getRpcPort()).isEqualTo(-1);
	//			assertThat(cassandra.getSslPort()).isEqualTo(-1);
	//		});
	//	}
	//
	//	@Test
	//	void testSuccessWhenTransportDisabled() throws Throwable {
	//		this.configProperties.put("start_native_transport", false);
	//		run((cassandra, throwable) -> {
	//			assertThat(throwable).doesNotThrowAnyException();
	//			assertThat(cassandra.getAddress()).isNull();
	//			assertThat(cassandra.getPort()).isEqualTo(-1);
	//			assertThat(cassandra.getSslPort()).isEqualTo(-1);
	//			assertThat(cassandra.getRpcPort()).isEqualTo(-1);
	//		});
	//	}

	@Test
	void testSuccessWhenSslEnabled() throws Throwable {
		Resource keystore = new ClassPathResource("keystore.node0");
		Resource truststore = new ClassPathResource("truststore.node0");
		Map<String, Object> sslOptions = new LinkedHashMap<>();
		sslOptions.put("enabled", true);
		sslOptions.put("require_client_auth", true);
		sslOptions.put("optional", false);
		sslOptions.put("keystore", keystore);
		sslOptions.put("keystore_password", "cassandra");
		sslOptions.put("truststore", truststore);
		sslOptions.put("truststore_password", "cassandra");
		CassandraBuilder builder = new CassandraBuilder()
				.configProperty("native_transport_port_ssl", 9142)
				.configProperty("client_encryption_options", sslOptions);
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			//			assertThat(cassandra.getSettings().getSslPort()).hasValue(9142);
			//			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
			//			clusterFactory.setSslEnabled(true);
			//			clusterFactory.setTruststore(truststore);
			//			clusterFactory.setTruststorePassword("cassandra");
			//			clusterFactory.setKeystore(keystore);
			//			clusterFactory.setKeystorePassword("cassandra");
			//			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
			//				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
			//			}
		});
	}

	@Test
	void testSuccessWhenAuthenticatorEnabled() throws Throwable {
		CassandraBuilder builder = new CassandraBuilder()
				.configProperty("authenticator", "PasswordAuthenticator")
				.configProperty("authorizer", "CassandraAuthorizer")
				.systemProperty("cassandra.superuser_setup_delay_ms", 0);
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			//			ClusterCassandraConnectionFactory clusterFactory = new ClusterCassandraConnectionFactory();
			//			clusterFactory.setPassword("cassandra");
			//			clusterFactory.setUsername("cassandra");
			//			try (CassandraConnection connection = clusterFactory.create(cassandra)) {
			//				CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
			//			}
		});
	}

	@Test
	void testFailStartupTimeout() throws Throwable {
		CassandraBuilder builder = new CassandraBuilder()
				.startupTimeout(Duration.ofSeconds(3));
		run(builder, (cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("couldn't be started within 3000ms"));
	}

	@Test
	void testFailInvalidProperty() throws Throwable {
		CassandraBuilder builder = new CassandraBuilder()
				.configProperty("invalid_property", "");
		run(builder, (cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("[invalid_property] from your cassandra.yaml"));
	}

	@Test
	void testFailPortsAreBusy() throws Throwable {
		CassandraBuilder builder = new CassandraBuilder();
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			run(builder, (cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Address already in use"));
		});
	}

	private void run(CassandraBuilder builder, CassandraConsumer cassandraConsumer) throws Throwable {
		new CassandraRunner(builder).run(cassandraConsumer);
	}

	private interface CassandraConsumer {

		void accept(Cassandra cassandra, Throwable throwable) throws Throwable;

	}

	private static final class CassandraRunner {

		private final CassandraBuilder builder;

		private CassandraRunner(CassandraBuilder builder) {
			this.builder = builder;
		}

		void run(CassandraConsumer consumer) throws Throwable {
			Cassandra cassandra = this.builder.build();
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
