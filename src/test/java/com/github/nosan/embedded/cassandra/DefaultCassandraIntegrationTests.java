/*
 * Copyright 2020-2021 the original author or authors.
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
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
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

	@ParameterizedTest
	@MethodSource("versions")
	void keepDataBetweenLaunches(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
		Cassandra cassandra = this.builder.version(version).build();
		this.runner.run(cassandra, throwable -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.isRunning()).isTrue();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			createScheme(sessionFactory);
		});
		this.runner.run(cassandra, throwable -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.isRunning()).isTrue();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			assertThatThrownBy(() -> createScheme(sessionFactory))
					.hasStackTraceContaining("Keyspace test already exists");
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void isRunningAndGetSettings(Version version) {
		assumeJavaVersion(version);
		assumeOs(version);
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
		assumeOs(version);
		this.builder.version(version);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			createScheme(sessionFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenCustomCassandraYaml(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
		ClassPathResource configFile = new ClassPathResource(String.format("cassandra-random-%s.yaml", version));
		this.builder.version(version)
				.addSystemProperty("cassandra.jmx.local.port", 0)
				.configFile(configFile);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(version);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			createScheme(sessionFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenTransportDisabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
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
		assumeOs(version);
		assumeTrue(version.getMajor() >= 3, "native_transport_port_ssl only for version >= 3.x.x");
		Map<String, Object> clientEncryptionOptions = new LinkedHashMap<>();
		clientEncryptionOptions.put("enabled", true);
		clientEncryptionOptions.put("require_client_auth", true);
		clientEncryptionOptions.put("optional", false);
		clientEncryptionOptions.put("keystore", "conf/server.keystore");
		clientEncryptionOptions.put("keystore_password", "123456");
		clientEncryptionOptions.put("truststore", "conf/server.truststore");
		clientEncryptionOptions.put("truststore_password", "123456");
		this.builder.version(version)
				.addWorkingDirectoryResource(new ClassPathResource("server.keystore"), "conf/server.keystore")
				.addWorkingDirectoryResource(new ClassPathResource("server.truststore"), "conf/server.truststore")
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
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getSslPort();
			assertThatThrownBy(() -> createScheme(sessionFactory))
					.hasStackTraceContaining("Could not reach any contact point");
			sessionFactory.sslEnabled = true;
			sessionFactory.truststore = new ClassPathResource("client.truststore");
			sessionFactory.truststorePassword = "123456";
			sessionFactory.keystore = new ClassPathResource("client.keystore");
			sessionFactory.keystorePassword = "123456";
			createScheme(sessionFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenSslClientEnabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
		Map<String, Object> clientEncryptionOptions = new LinkedHashMap<>();
		clientEncryptionOptions.put("enabled", true);
		clientEncryptionOptions.put("require_client_auth", true);
		clientEncryptionOptions.put("optional", false);
		clientEncryptionOptions.put("keystore", new ClassPathResource("server.keystore"));
		clientEncryptionOptions.put("keystore_password", "123456");
		clientEncryptionOptions.put("truststore", new ClassPathResource("server.truststore"));
		clientEncryptionOptions.put("truststore_password", "123456");
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
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			assertThatThrownBy(() -> createScheme(sessionFactory))
					.hasStackTraceContaining("Could not reach any contact point");
			sessionFactory.sslEnabled = true;
			sessionFactory.truststore = new ClassPathResource("client.truststore");
			sessionFactory.truststorePassword = "123456";
			sessionFactory.keystore = new ClassPathResource("client.keystore");
			sessionFactory.keystorePassword = "123456";
			createScheme(sessionFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenSslServerEnabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
		Map<String, Object> serverEncryptionOptions = new LinkedHashMap<>();
		serverEncryptionOptions.put("internode_encryption", "all");
		if (version.getMajor() >= 4) {
			serverEncryptionOptions.put("enable_legacy_ssl_storage_port", "true");
			serverEncryptionOptions.put("optional", false);
			serverEncryptionOptions.put("enabled", true);
		}
		serverEncryptionOptions.put("require_client_auth", true);
		serverEncryptionOptions.put("keystore", new ClassPathResource("server.keystore"));
		serverEncryptionOptions.put("keystore_password", "123456");
		serverEncryptionOptions.put("truststore", new ClassPathResource("server.truststore"));
		serverEncryptionOptions.put("truststore_password", "123456");
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
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			createScheme(sessionFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testSuccessWhenAuthenticatorEnabled(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
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
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			assertThatThrownBy(() -> createScheme(sessionFactory)).hasStackTraceContaining("requires authentication");
			sessionFactory.username = "cassandra";
			sessionFactory.password = "cassandra";
			createScheme(sessionFactory);
		});
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailStartupTimeout(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
		this.builder.version(version)
				.startupTimeout(Duration.ofMillis(200));
		this.runner.run((cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("couldn't be started within 200ms"));
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailInvalidProperty(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
		this.builder.version(version)
				.addConfigProperty("invalid_property", "");
		this.runner.run((cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("[invalid_property] from your cassandra.yaml"));
	}

	@ParameterizedTest
	@MethodSource("versions")
	void testFailJmxPortBusy(Version version) throws Throwable {
		assumeJavaVersion(version);
		assumeOs(version);
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
		assumeOs(version);
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
		assumeOs(version);
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
		assumeOs(version);
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

	private static void assumeOs(Version version) {
		if (version.getMajor() >= 4) {
			assumeFalse(OS.WINDOWS.isCurrentOs(), "Cassandra 4.X.X is not supported on Windows anymore");
		}
	}

	private static void createScheme(SessionFactory sessionFactory) {
		try (CqlSession session = sessionFactory.createSession()) {
			CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
		}
	}

	private static Stream<Version> versions() {
		return Stream.of(Version.parse("4.0-beta4"), Version.parse("3.11.9"));
	}

	private interface CassandraConsumer {

		void accept(Cassandra cassandra, Throwable throwable) throws Throwable;

	}

	private interface ThrowableConsumer {

		void accept(Throwable throwable) throws Throwable;

	}

	private static final class CassandraRunner {

		private final CassandraBuilder builder;

		private CassandraRunner(CassandraBuilder builder) {
			this.builder = builder;
		}

		void run(Cassandra cassandra, ThrowableConsumer consumer) throws Throwable {
			try {
				try {
					cassandra.start();
				}
				catch (Throwable ex) {
					consumer.accept(ex);
					return;
				}
				consumer.accept(null);
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

	private static final class SessionFactory {

		private InetAddress address;

		private int port;

		private boolean sslEnabled;

		private String username;

		private String password;

		private Resource truststore;

		private String truststorePassword;

		private Resource keystore;

		private String keystorePassword;

		private CqlSession createSession() {
			ProgrammaticDriverConfigLoaderBuilder driverBuilder = DriverConfigLoader.programmaticBuilder()
					.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(10))
					.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(3));
			if (this.username != null && this.password != null) {
				driverBuilder.withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, this.username)
						.withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, this.password)
						.withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class);
			}
			if (this.sslEnabled) {
				driverBuilder.withBoolean(DefaultDriverOption.SSL_HOSTNAME_VALIDATION, false)
						.withClass(DefaultDriverOption.SSL_ENGINE_FACTORY_CLASS, DefaultSslEngineFactory.class);
				if (this.truststore != null) {
					driverBuilder
							.withString(DefaultDriverOption.SSL_TRUSTSTORE_PATH, getPath(this.truststore).toString());
				}
				if (this.truststorePassword != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PASSWORD, this.truststorePassword);
				}
				if (this.keystore != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PATH, getPath(this.keystore).toString());
				}
				if (this.keystorePassword != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PASSWORD, this.keystorePassword);
				}
			}
			InetSocketAddress contactPoint = new InetSocketAddress(this.address, this.port);
			CqlSessionBuilder sessionBuilder = CqlSession.builder().addContactPoint(contactPoint)
					.withConfigLoader(driverBuilder.build());
			sessionBuilder.withLocalDatacenter("datacenter1");
			return sessionBuilder.build();
		}

		private Path getPath(Resource resource) {
			try {
				return Paths.get(resource.toURI());
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}

	}

}
