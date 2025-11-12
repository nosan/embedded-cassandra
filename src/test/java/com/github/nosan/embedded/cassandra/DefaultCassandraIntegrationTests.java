/*
 * Copyright 2020-2024 the original author or authors.
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
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
@DisabledOnOs(OS.WINDOWS)
class DefaultCassandraIntegrationTests {

	private final CassandraBuilder builder = new CassandraBuilder().configure(builder -> {
		if (System.getenv("CI") != null) {
			builder.startupTimeout(Duration.ofMinutes(10));
			builder.jvmOptions("-Xms512m", "-Xmx512m");
		}
	});

	private final CassandraRunner runner = new CassandraRunner(this.builder);

	@Test
	void isRunningAndGetSettings() {
		Cassandra cassandra = this.builder.build();
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

	@Test
	void testSuccessWhenDefault() throws Throwable {
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			runScripts(sessionFactory);
		});
	}

	@Test
	void testSuccessWhenCustomCassandraYaml() throws Throwable {
		ClassPathResource configFile = new ClassPathResource("cassandra-random.yaml");
		this.builder
				.addSystemProperty("cassandra.jmx.local.port", 0)
				.configFile(configFile);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			runScripts(sessionFactory);
		});
	}

	@Test
	void testSuccessWhenTransportDisabled() throws Throwable {
		this.builder.addConfigProperty("start_native_transport", false);

		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getAddress()).isNull();
			assertThat(settings.getPort()).isNull();
			assertThat(settings.getSslPort()).isNull();
		});
	}

	@Test
	void testSuccessWhenSslClientEnabledSslPort() throws Throwable {
		Map<String, Object> clientEncryptionOptions = new LinkedHashMap<>();
		clientEncryptionOptions.put("enabled", true);
		clientEncryptionOptions.put("require_client_auth", true);
		clientEncryptionOptions.put("optional", false);
		clientEncryptionOptions.put("keystore", "conf/server.keystore");
		clientEncryptionOptions.put("keystore_password", "123456");
		clientEncryptionOptions.put("truststore", "conf/server.truststore");
		clientEncryptionOptions.put("truststore_password", "123456");
		this.builder
				.addWorkingDirectoryResource(new ClassPathResource("server.keystore"), "conf/server.keystore")
				.addWorkingDirectoryResource(new ClassPathResource("server.truststore"), "conf/server.truststore")
				.addConfigProperty("native_transport_port_ssl", 9142)
				.addConfigProperty("client_encryption_options", clientEncryptionOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isEqualTo(9142);
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getSslPort();
			assertThatThrownBy(() -> runScripts(sessionFactory))
					.hasStackTraceContaining("Could not reach any contact point");
			sessionFactory.sslEnabled = true;
			sessionFactory.truststore = new ClassPathResource("client.truststore");
			sessionFactory.truststorePassword = "123456";
			sessionFactory.keystore = new ClassPathResource("client.keystore");
			sessionFactory.keystorePassword = "123456";
			runScripts(sessionFactory);
		});
	}

	@Test
	void testSuccessWhenSslClientEnabled() throws Throwable {
		Map<String, Object> clientEncryptionOptions = new LinkedHashMap<>();
		clientEncryptionOptions.put("enabled", true);
		clientEncryptionOptions.put("require_client_auth", true);
		clientEncryptionOptions.put("optional", false);
		clientEncryptionOptions.put("keystore", new ClassPathResource("server.keystore"));
		clientEncryptionOptions.put("keystore_password", "123456");
		clientEncryptionOptions.put("truststore", new ClassPathResource("server.truststore"));
		clientEncryptionOptions.put("truststore_password", "123456");
		this.builder
				.addConfigProperty("client_encryption_options", clientEncryptionOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isNull();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			assertThatThrownBy(() -> runScripts(sessionFactory))
					.hasStackTraceContaining("Could not reach any contact point");
			sessionFactory.sslEnabled = true;
			sessionFactory.truststore = new ClassPathResource("client.truststore");
			sessionFactory.truststorePassword = "123456";
			sessionFactory.keystore = new ClassPathResource("client.keystore");
			sessionFactory.keystorePassword = "123456";
			runScripts(sessionFactory);
		});
	}

	@Test
	void testSuccessWhenSslServerEnabled() throws Throwable {
		Map<String, Object> serverEncryptionOptions = new LinkedHashMap<>();
		serverEncryptionOptions.put("internode_encryption", "all");
		serverEncryptionOptions.put("enable_legacy_ssl_storage_port", "true");
		serverEncryptionOptions.put("optional", false);
		serverEncryptionOptions.put("enabled", true);
		serverEncryptionOptions.put("require_client_auth", true);
		serverEncryptionOptions.put("keystore", new ClassPathResource("server.keystore"));
		serverEncryptionOptions.put("keystore_password", "123456");
		serverEncryptionOptions.put("truststore", new ClassPathResource("server.truststore"));
		serverEncryptionOptions.put("truststore_password", "123456");
		this.builder
				.addConfigProperty("server_encryption_options", serverEncryptionOptions);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			assertThat(settings.getSslPort()).isNull();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			runScripts(sessionFactory);
		});
	}

	@Test
	void testSuccessWhenAuthenticatorEnabled() throws Throwable {
		this.builder
				.addConfigProperty("authenticator", "PasswordAuthenticator")
				.addConfigProperty("authorizer", "CassandraAuthorizer")
				.addSystemProperty("cassandra.superuser_setup_delay_ms", 0);
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			assertThat(cassandra.getName()).isNotBlank();
			assertThat(cassandra.getWorkingDirectory()).isNotNull();
			assertThat(cassandra.getVersion()).isEqualTo(CassandraBuilder.DEFAULT_VERSION);
			assertThat(cassandra.isRunning()).isTrue();
			Settings settings = cassandra.getSettings();
			SessionFactory sessionFactory = new SessionFactory();
			sessionFactory.address = settings.getAddress();
			sessionFactory.port = settings.getPort();
			assertThatThrownBy(() -> runScripts(sessionFactory)).hasStackTraceContaining(
					"requires authentication");
			sessionFactory.username = "cassandra";
			sessionFactory.password = "cassandra";
			runScripts(sessionFactory);
		});
	}

	@Test
	void testFailStartupTimeout() throws Throwable {
		this.builder
				.startupTimeout(Duration.ofMillis(200));
		this.runner.run((cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("couldn't be started within 200ms"));
	}

	@Test
	void testFailInvalidProperty() throws Throwable {
		this.builder
				.addConfigProperty("invalid_property", "");
		this.runner.run((cassandra, throwable) -> assertThat(throwable).isNotNull()
				.hasStackTraceContaining("[invalid_property] from your cassandra.yaml"));
	}

	@Test
	void testFailJmxPortBusy() throws Throwable {
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Address already in use"));
		});

	}

	@Test
	void testFailStoragePortBusy() throws Throwable {
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.builder.addSystemProperty("cassandra.jmx.local.port", 0);
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("listen_address:storage_port"));
		});
	}

	@Test
	void testFailNativePortBusy() throws Throwable {
		this.runner.run((cassandra, throwable) -> {
			assertThat(throwable).doesNotThrowAnyException();
			this.builder.addSystemProperty("cassandra.jmx.local.port", 0)
					.addSystemProperty("cassandra.storage_port", 0)
					.configure(new SimpleSeedProviderConfigurator("localhost:0"));
			this.runner.run((cassandra2, throwable2) -> assertThat(throwable2).isNotNull()
					.hasStackTraceContaining("Failed to bind port "));
		});
	}

	private static void runScripts(SessionFactory sessionFactory) {
		try (CqlSession session = sessionFactory.createSession()) {
			CqlScript.ofClassPath("statements.cql").forEachStatement(session::execute);
		}
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
					driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PATH, getPath(this.truststore));
				}
				if (this.truststorePassword != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PASSWORD, this.truststorePassword);
				}
				if (this.keystore != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PATH, getPath(this.keystore));
				}
				if (this.keystorePassword != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PASSWORD, this.keystorePassword);
				}
			}

			return CqlSession.builder().addContactPoint(new InetSocketAddress(this.address, this.port))
					.withConfigLoader(driverBuilder.build())
					.withLocalDatacenter("datacenter1")
					.build();
		}

		private String getPath(Resource resource) {
			try {
				return Paths.get(resource.toURI()).toString();
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}

	}

}
