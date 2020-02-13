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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link DefaultCassandra}.
 *
 * @author Dmytro Nosan
 */
class DefaultCassandraIntegrationTests {

	private final CassandraBuilder builder = new CassandraBuilder().configure(builder -> {
		if (System.getenv("CI") != null) {
			builder.startupTimeout(Duration.ofMinutes(10));
		}
	});

	private final CassandraRunner runner = new CassandraRunner(this.builder);

	@Test
	void unableToInitialize() throws Throwable {
		this.builder.workingDirectoryInitializer((workingDirectory, version) -> {
			throw new IOException("Fail");
		}).build();
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).hasStackTraceContaining("Unable to initialize:");
		});
	}

	@Test
	void getName() {
		Cassandra cassandra = this.builder.name("test").build();
		assertThat(cassandra.getName()).isEqualTo("test");
	}

	@Test
	void getVersion() {
		Cassandra cassandra = this.builder.version("3.11.9").build();
		assertThat(cassandra.getVersion()).isEqualTo(Version.parse("3.11.9"));
	}

	@Test
	void getWorkingDirectory(@TempDir Path workingDirectory) {
		Cassandra cassandra = this.builder.workingDirectory(() -> workingDirectory).build();
		assertThat(cassandra.getWorkingDirectory()).isEqualTo(workingDirectory);
	}

	@ParameterizedTest
	@MethodSource("versions")
	void isRunningAndGetSettings(Version version) {
		assumeJavaVersion(version);
		Cassandra cassandra = this.builder.version(version).build();
		assertThat(cassandra.isRunning()).isFalse();
		assertThatThrownBy(cassandra::getSettings)
				.hasMessage("The getSettings() method was called but start() had not been called");
		cassandra.start();
		cassandra.start();
		Settings settings = cassandra.getSettings();
		assertThat(cassandra.isRunning()).isTrue();
		cassandra.stop();
		cassandra.stop();
		assertThat(cassandra.isRunning()).isFalse();
		assertThat(cassandra.getSettings()).isSameAs(settings);
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenDefault(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			createScheme(clusterFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenCustomCassandraYaml(Version version) throws Throwable {
		assumeJavaVersion(version);
		ClassPathResource configFile = new ClassPathResource(String.format("cassandra-random-%s.yaml", version));
		this.builder.version(version)
				.addSystemProperty("cassandra.jmx.local.port", 0)
				.addWorkingDirectoryCustomizers(WorkingDirectoryCustomizer.copy(configFile, "conf/cassandra.yaml"));
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			createScheme(clusterFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenTransportDisabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version)
				.addConfigProperty("start_native_transport", false);
		if (version.getMajor() < 4) {
			this.builder.addConfigProperty("start_rpc", false);
		}
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getAddress()).isNull();
			assertThat(settings.getPort()).isNull();
			assertThat(settings.getSslPort()).isNull();
			assertThat(settings.getRpcPort()).isNull();
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenSslClientEnabledSslPort(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeTrue(version.getMajor() >= 3, "native_transport_port_ssl only for version >= 3.x.x");
		Resource keystore = new ClassPathResource("keystore.node0");
		Resource truststore = new ClassPathResource("truststore.node0");
		Map<String, Object> clientEncryptionOptions = new LinkedHashMap<>();
		clientEncryptionOptions.put("enabled", true);
		clientEncryptionOptions.put("require_client_auth", true);
		clientEncryptionOptions.put("optional", false);
		clientEncryptionOptions.put("keystore", keystore);
		clientEncryptionOptions.put("keystore_password", "cassandra");
		clientEncryptionOptions.put("truststore", truststore);
		clientEncryptionOptions.put("truststore_password", "cassandra");
		this.builder.version(version)
				.addConfigProperty("native_transport_port_ssl", 9142)
				.addConfigProperty("client_encryption_options", clientEncryptionOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isEqualTo(9142);
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getSslPort();
			assertThatThrownBy(() -> createScheme(clusterFactory)).hasStackTraceContaining("tried for query failed");
			clusterFactory.sslEnabled = true;
			clusterFactory.truststore = truststore;
			clusterFactory.truststorePassword = "cassandra";
			clusterFactory.keystore = keystore;
			clusterFactory.keystorePassword = "cassandra";
			createScheme(clusterFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenSslClientEnabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		Resource keystore = new ClassPathResource("keystore.node0");
		Resource truststore = new ClassPathResource("truststore.node0");
		Map<String, Object> clientEncryptionOptions = new LinkedHashMap<>();
		clientEncryptionOptions.put("enabled", true);
		clientEncryptionOptions.put("require_client_auth", true);
		clientEncryptionOptions.put("optional", false);
		clientEncryptionOptions.put("keystore", keystore);
		clientEncryptionOptions.put("keystore_password", "cassandra");
		clientEncryptionOptions.put("truststore", truststore);
		clientEncryptionOptions.put("truststore_password", "cassandra");
		this.builder.version(version)
				.addConfigProperty("client_encryption_options", clientEncryptionOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isNull();
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

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenSslServerEnabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		Resource keystore = new ClassPathResource("keystore.node0");
		Resource truststore = new ClassPathResource("truststore.node0");
		Map<String, Object> serverEncryptionOptions = new LinkedHashMap<>();
		serverEncryptionOptions.put("internode_encryption", "all");
		if (version.getMajor() >= 4) {
			serverEncryptionOptions.put("enable_legacy_ssl_storage_port", "true");
			serverEncryptionOptions.put("optional", false);
			serverEncryptionOptions.put("enabled", true);
		}
		serverEncryptionOptions.put("require_client_auth", true);
		serverEncryptionOptions.put("keystore", keystore);
		serverEncryptionOptions.put("keystore_password", "cassandra");
		serverEncryptionOptions.put("truststore", truststore);
		serverEncryptionOptions.put("truststore_password", "cassandra");
		this.builder.version(version)
				.addConfigProperty("server_encryption_options", serverEncryptionOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isNull();
			ClusterFactory clusterFactory = new ClusterFactory();
			clusterFactory.address = settings.getAddress();
			clusterFactory.port = settings.getPort();
			createScheme(clusterFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenAuthenticatorEnabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version)
				.addConfigProperty("authenticator", "PasswordAuthenticator")
				.addConfigProperty("authorizer", "CassandraAuthorizer")
				.addSystemProperty("cassandra.superuser_setup_delay_ms", 0);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
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

	@ParameterizedTest
	@MethodSource("versions")
	void testFailStartupTimeout(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version)
				.startupTimeout(Duration.ofMillis(200));
		this.runner.run((cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("couldn't be started within 200ms"));
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailInvalidProperty(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version)
				.addConfigProperty("invalid_property", "");
		this.runner.run((cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("[invalid_property] from your cassandra.yaml"));
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailJmxPortBusy(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Address already in use"));
		});

	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailStoragePortBusy(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.builder.addSystemProperty("cassandra.jmx.local.port", 0);
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("listen_address:storage_port"));
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailNativePortBusy(Version version) throws Throwable {
		assumeJavaVersion(version);
		this.builder.version(version);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.builder.addSystemProperty("cassandra.jmx.local.port", 0)
					.addSystemProperty("cassandra.storage_port", 0)
					.configure(builder -> {
						if (version.getMajor() >= 4) {
							new SimpleSeedProviderConfigurator("localhost:0").configure(builder);
						}
						else {
							builder.addConfigProperty("start_rpc", false);
						}
					});
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Failed to bind port "));
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailRpcPortBusy(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeTrue(version.getMajor() < 4, "RPC only for version < 4.x.x");
		this.builder.version(version)
				.addConfigProperty("start_rpc", true);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.builder.addSystemProperty("cassandra.jmx.local.port", 0)
					.addSystemProperty("cassandra.storage_port", 0)
					.addSystemProperty("cassandra.native_transport_port", 0);
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Unable to create thrift socket"));
		});
	}

	private static void assumeJavaVersion(Version version) {
		if (version.getMajor() < 4) {
			assumeTrue(JRE.JAVA_8.isCurrentVersion(), "Cassandra 3.x.x supports only JAVA 8");
		}
	}

	private static void createScheme(ClusterFactory clusterFactory) {
		try (Cluster cluster = clusterFactory.createCluster()) {
			Session session = cluster.connect();
			CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
		}
	}

	private static Stream<Version> versions() {
		return Stream.of(Version.parse("4.0-beta3"), Version.parse("3.11.9"));
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
			catch (Throwable ex) {
				try {
					cassandra.stop();
				}
				catch (Throwable suppressed) {
					ex.addSuppressed(suppressed);
				}
				throw ex;
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
