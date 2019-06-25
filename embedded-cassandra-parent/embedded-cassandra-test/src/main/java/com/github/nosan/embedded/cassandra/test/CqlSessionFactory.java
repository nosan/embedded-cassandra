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

package com.github.nosan.embedded.cassandra.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link CqlSession} factory with a default strategy.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public class CqlSessionFactory {

	private final List<TypeCodec<?>> typeCodecs = new ArrayList<>();

	@Nullable
	private String username;

	@Nullable
	private String password;

	@Nullable
	private String localDataCenter = "datacenter1";

	@Nullable
	private Path truststorePath;

	@Nullable
	private String truststorePassword;

	@Nullable
	private Path keystorePath;

	@Nullable
	private String keystorePassword;

	@Nullable
	private String[] cipherSuites;

	private boolean hostnameValidation;

	private boolean sslEnabled;

	/**
	 * Specifies the data center that is considered "local" by the load balancing policy.
	 *
	 * @param localDataCenter the data center
	 * @since 2.0.3
	 */
	public void setLocalDataCenter(@Nullable String localDataCenter) {
		this.localDataCenter = localDataCenter;
	}

	/**
	 * Whether or not to require validation that the hostname of the server certificate's common
	 * name matches the hostname of the server being connected to.
	 *
	 * @param hostnameValidation whether hostname validation should be enabled
	 * @since 2.0.3
	 */
	public void setHostnameValidation(boolean hostnameValidation) {
		this.hostnameValidation = hostnameValidation;
	}

	/**
	 * Enables the use of SSL for the created Session.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 * @since 2.0.3
	 */
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	/**
	 * The path to the truststore.
	 *
	 * @param truststorePath the path
	 * @since 2.0.3
	 */
	public void setTruststorePath(@Nullable Path truststorePath) {
		this.truststorePath = truststorePath;
	}

	/**
	 * The password to truststore.
	 *
	 * @param truststorePassword the password
	 * @since 2.0.3
	 */
	public void setTruststorePassword(@Nullable String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	/**
	 * The path to the keystore.
	 *
	 * @param keystorePath the path
	 * @since 2.0.3
	 */
	public void setKeystorePath(@Nullable Path keystorePath) {
		this.keystorePath = keystorePath;
	}

	/**
	 * The password to keystore.
	 *
	 * @param keystorePassword the password
	 * @since 2.0.3
	 */
	public void setKeystorePassword(@Nullable String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	/**
	 * Set the cipher suites to use. The default is to present all the eligible client ciphers to the server.
	 *
	 * @param cipherSuites the cipher suites to use
	 * @since 2.0.3
	 */
	public void setCipherSuites(@Nullable String... cipherSuites) {
		this.cipherSuites = cipherSuites;
	}

	/**
	 * The username to use to login to Cassandra hosts.
	 *
	 * @param username the username
	 * @since 2.0.3
	 */
	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	/**
	 * The password corresponding to username.
	 *
	 * @param password the password
	 * @since 2.0.3
	 */
	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	/**
	 * Registers additional codecs for custom type mappings.
	 *
	 * @param typeCodecs type codes
	 * @since 2.0.4
	 */
	public void addTypeCodecs(TypeCodec<?>... typeCodecs) {
		Objects.requireNonNull(typeCodecs, "TypeCodec must not be null");
		this.typeCodecs.addAll(Arrays.asList(typeCodecs));
	}

	/**
	 * Creates a new configured {@link CqlSession}.
	 *
	 * @param settings the settings
	 * @return a cql session
	 */
	public CqlSession create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		Integer port = settings.portOrSslPort().orElse(null);
		Integer sslPort = settings.sslPort().orElse(null);
		InetAddress address = settings.address().orElse(null);
		if (address != null && port != null) {
			ProgrammaticDriverConfigLoaderBuilder driverBuilder = DriverConfigLoader.programmaticBuilder()
					.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
					.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(3));
			if (this.username != null && this.password != null) {
				driverBuilder.withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, this.username)
						.withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, this.password)
						.withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS,
								com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider.class);
			}
			if (this.sslEnabled) {
				driverBuilder.withBoolean(DefaultDriverOption.SSL_HOSTNAME_VALIDATION, this.hostnameValidation)
						.withClass(DefaultDriverOption.SSL_ENGINE_FACTORY_CLASS, DefaultSslEngineFactory.class);
				if (this.cipherSuites != null) {
					driverBuilder.withStringList(DefaultDriverOption.SSL_CIPHER_SUITES,
							new ArrayList<>(Arrays.asList(this.cipherSuites)));
				}
				if (this.truststorePath != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PATH, this.truststorePath.toString());
				}
				if (this.truststorePassword != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PASSWORD, this.truststorePassword);
				}
				if (this.keystorePath != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PATH, this.keystorePath.toString());
				}
				if (this.keystorePassword != null) {
					driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PASSWORD, this.keystorePassword);
				}
			}
			DriverConfigLoader driverConfigLoader = buildDriverConfigLoader(driverBuilder);
			Objects.requireNonNull(driverConfigLoader, "Driver Config must not be null");
			InetSocketAddress contactPoint = new InetSocketAddress(address,
					(this.sslEnabled && sslPort != null) ? sslPort : port);
			CqlSessionBuilder sessionBuilder = CqlSession.builder().addContactPoint(contactPoint)
					.withConfigLoader(driverConfigLoader);
			if (this.localDataCenter != null) {
				sessionBuilder.withLocalDatacenter(this.localDataCenter);
			}
			if (!this.typeCodecs.isEmpty()) {
				sessionBuilder.addTypeCodecs(this.typeCodecs.toArray(new TypeCodec[0]));
			}
			CqlSession cqlSession = buildCqlSession(sessionBuilder);
			return Objects.requireNonNull(cqlSession, "Cql Session must not be null");
		}
		throw new IllegalStateException(String.format("Cql Session can not be created from %s", settings));
	}

	/**
	 * Creates a new configured {@link CqlSession}.
	 *
	 * @param sessionBuilder a session builder
	 * @return a session
	 * @since 2.0.1
	 */
	protected CqlSession buildCqlSession(CqlSessionBuilder sessionBuilder) {
		return sessionBuilder.build();
	}

	/**
	 * Creates a new configured {@link DriverConfigLoader}.
	 *
	 * @param driverBuilder a driver builder
	 * @return a driver config
	 * @since 2.0.1
	 */
	protected DriverConfigLoader buildDriverConfigLoader(ProgrammaticDriverConfigLoaderBuilder driverBuilder) {
		return driverBuilder.build();
	}

	/**
	 * A simple authentication provider that extends
	 * {@link com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider} and do not print a warn message.
	 *
	 * @deprecated since 2.0.3 in favor of {@link com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider}.
	 */
	@Deprecated
	public static class PlainTextAuthProvider extends com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider {

		public PlainTextAuthProvider(DriverContext context) {
			super(context);
		}

		@Override
		public void onMissingChallenge(EndPoint endPoint) {
		}

	}

}
