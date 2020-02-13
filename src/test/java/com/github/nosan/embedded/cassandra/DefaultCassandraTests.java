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

import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.SocketOptions;
import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultCassandra}.
 *
 * @author Dmytro Nosan
 */
class DefaultCassandraTests {

	private final CassandraBuilder builder = new CassandraBuilder()
			.startupTimeout(Duration.ofMinutes(5));

	@Test
	void testSuccessWhenDownloadingFromArchive40beta1() throws Throwable {
		run(this.builder.version("4.0-beta1"), (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			createScheme(clusterFactory);
		});
	}

	@Test
	void testSuccessWhenDefault() throws Throwable {
		run(this.builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			createScheme(clusterFactory);
		});
	}

	@Test
	void testSuccessWhenCustomCassandraYaml() throws Throwable {
		CassandraBuilder builder = this.builder
				.systemProperty("cassandra.config", new ClassPathResource("cassandra-random-4.0-beta2.yaml"));
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			createScheme(clusterFactory);
		});
	}

	@Test
	void testSuccessWhenTransportDisabled() throws Throwable {
		CassandraBuilder builder = this.builder.configProperty("start_native_transport", false);
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getAddress()).isEqualTo(InetAddress.getByName("localhost"));
			assertThat(settings.getPort()).isEqualTo(9042);
			assertThat(settings.getSslPort()).isEmpty();
			assertThat(settings.getRpcPort()).isEqualTo(9160);
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			assertThatThrownBy(() -> createScheme(clusterFactory)).hasStackTraceContaining("tried for query failed");
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
		sslOptions.put("keystore", keystore);
		sslOptions.put("keystore_password", "cassandra");
		sslOptions.put("truststore", truststore);
		sslOptions.put("truststore_password", "cassandra");
		CassandraBuilder builder = this.builder
				.configProperty("native_transport_port_ssl", 9142)
				.configProperty("client_encryption_options", sslOptions);
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).hasValue(9142);
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getSslPort().orElse(settings.getPort());
			assertThatThrownBy(() -> createScheme(clusterFactory)).hasStackTraceContaining("tried for query failed");
			clusterFactory.sslEnabled = true;
			clusterFactory.truststore = truststore;
			clusterFactory.truststorePassword = "cassandra";
			clusterFactory.keystore = keystore;
			clusterFactory.keystorePassword = "cassandra";
			createScheme(clusterFactory);
		});
	}

	@Test
	void testSuccessWhenSslEnabledSamePort() throws Throwable {
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
		CassandraBuilder builder = this.builder
				.configProperty("client_encryption_options", sslOptions);
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isEmpty();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			assertThatThrownBy(() -> createScheme(clusterFactory)).hasStackTraceContaining("tried for query failed");
			clusterFactory.sslEnabled = true;
			clusterFactory.truststore = truststore;
			clusterFactory.truststorePassword = "cassandra";
			clusterFactory.keystore = keystore;
			clusterFactory.keystorePassword = "cassandra";
			createScheme(clusterFactory);
		});
	}

	@Test
	void testSuccessWhenAuthenticatorEnabled() throws Throwable {
		CassandraBuilder builder = this.builder
				.configProperty("authenticator", "PasswordAuthenticator")
				.configProperty("authorizer", "CassandraAuthorizer")
				.systemProperty("cassandra.superuser_setup_delay_ms", 0);
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			Settings settings = cassandra.getSettings();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			assertThatThrownBy(() -> createScheme(clusterFactory)).hasStackTraceContaining("requires authentication");
			clusterFactory.username = "cassandra";
			clusterFactory.password = "cassandra";
			createScheme(clusterFactory);
		});
	}

	@Test
	void testFailStartupTimeout() throws Throwable {
		CassandraBuilder builder = this.builder
				.startupTimeout(Duration.ofSeconds(3));
		run(builder, (cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("couldn't be started within 3000ms"));
	}

	@Test
	void testFailInvalidProperty() throws Throwable {
		CassandraBuilder builder = this.builder
				.configProperty("invalid_property", "");
		run(builder, (cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("[invalid_property] from your cassandra.yaml"));
	}

	@Test
	void testFailPortsAreBusy() throws Throwable {
		CassandraBuilder builder = this.builder;
		run(builder, (cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			run(builder, (cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Address already in use"));
		});
	}

	private static void createScheme(ClusterFactory clusterFactory) {
		try (Cluster cluster = clusterFactory.createCluster()) {
			CqlScript.ofClassPath("schema.cql").forEachStatement(cluster.connect()::execute);
		}
	}

	private static void run(CassandraBuilder builder, CassandraConsumer cassandraConsumer) throws Throwable {
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

	private static final class ClusterFactory {

		private InetAddress address;

		private int port;

		private boolean sslEnabled;

		private String username;

		private String password;

		private Resource truststore;

		private String truststorePassword;

		private Resource keystore;

		private String keystorePassword;

		Cluster createCluster() {
			SocketOptions socketOptions = new SocketOptions();
			socketOptions.setConnectTimeoutMillis(30000);
			socketOptions.setReadTimeoutMillis(30000);
			Cluster.Builder builder = Cluster.builder().addContactPoints(this.address)
					.withoutMetrics()
					.withoutJMXReporting()
					.withPort(this.port)
					.withSocketOptions(socketOptions);
			if (this.username != null && this.password != null) {
				builder.withCredentials(this.username, this.password);
			}
			if (this.sslEnabled) {
				RemoteEndpointAwareJdkSSLOptions.Builder sslOptionsBuilder = RemoteEndpointAwareJdkSSLOptions
						.builder();
				if (this.keystore != null || this.truststore != null) {
					sslOptionsBuilder.withSSLContext(getSslContext());
				}
				builder.withSSL(sslOptionsBuilder.build());
			}
			return builder.build();
		}

		private SSLContext getSslContext() {
			try {
				SSLContext context = SSLContext.getInstance("SSL");
				TrustManagerFactory tmf = null;
				Resource truststore = this.truststore;
				if (truststore != null) {
					try (InputStream tsf = truststore.getInputStream()) {
						KeyStore ts = KeyStore.getInstance("JKS");
						String truststorePassword = this.truststorePassword;
						char[] password = (truststorePassword != null) ? truststorePassword.toCharArray() : null;
						ts.load(tsf, password);
						tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
						tmf.init(ts);
					}
				}
				KeyManagerFactory kmf = null;
				Resource keystore = this.keystore;
				if (keystore != null) {
					try (InputStream ksf = keystore.getInputStream()) {
						KeyStore ks = KeyStore.getInstance("JKS");
						String keystorePassword = this.keystorePassword;
						char[] password = (keystorePassword != null) ? keystorePassword.toCharArray() : null;
						ks.load(ksf, password);
						kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
						kmf.init(ks, password);
					}
				}
				KeyManager[] keyManagers = (kmf != null) ? kmf.getKeyManagers() : null;
				TrustManager[] trustManagers = (tmf != null) ? tmf.getTrustManagers() : null;
				context.init(keyManagers, trustManagers, new SecureRandom());
				return context;
			}
			catch (Exception ex) {
				throw new IllegalStateException("Can not initialize SSL Context", ex);
			}
		}

	}

}
