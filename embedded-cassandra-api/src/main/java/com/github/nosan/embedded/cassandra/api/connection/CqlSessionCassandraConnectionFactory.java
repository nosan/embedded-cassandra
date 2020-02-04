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

package com.github.nosan.embedded.cassandra.api.connection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CassandraConnectionFactory} that can be used to create and configure a {@link CqlSessionCassandraConnection}.
 *
 * @author Dmytro Nosan
 * @see CqlSessionCassandraConnectionBuilder
 * @since 3.0.0
 */
public final class CqlSessionCassandraConnectionFactory implements CassandraConnectionFactory {

	private final List<TypeCodec<?>> typeCodecs = new ArrayList<>();

	private final List<String> cipherSuites = new ArrayList<>();

	private final List<Consumer<? super CqlSessionBuilder>> sessionBuilderCustomizers = new ArrayList<>();

	private final List<Consumer<? super ProgrammaticDriverConfigLoaderBuilder>> driverConfigLoaderBuilderCustomizers =
			new ArrayList<>();

	@Nullable
	private String username;

	@Nullable
	private String password;

	@Nullable
	private String localDataCenter = "datacenter1";

	@Nullable
	private Resource truststore;

	@Nullable
	private String truststorePassword;

	@Nullable
	private Resource keystore;

	@Nullable
	private String keystorePassword;

	private boolean hostnameValidation;

	private boolean sslEnabled;

	/**
	 * Additional codecs for custom type mappings.
	 *
	 * @return type codes
	 */
	public List<TypeCodec<?>> getTypeCodecs() {
		return this.typeCodecs;
	}

	/**
	 * Cipher suites to use. The default is to present all the eligible client ciphers to the server.
	 *
	 * @return cipher suites
	 */
	public List<String> getCipherSuites() {
		return this.cipherSuites;
	}

	public List<Consumer<? super CqlSessionBuilder>> getSessionBuilderCustomizers() {
		return this.sessionBuilderCustomizers;
	}

	public List<Consumer<? super ProgrammaticDriverConfigLoaderBuilder>> getDriverConfigLoaderBuilderCustomizers() {
		return this.driverConfigLoaderBuilderCustomizers;
	}

	/**
	 * Returns username to use to login to Cassandra hosts.
	 *
	 * @return the username
	 */
	@Nullable
	public String getUsername() {
		return this.username;
	}

	/**
	 * Sets the username to use to login to Cassandra hosts.
	 *
	 * @param username the username
	 */
	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	/**
	 * Returns password to use to login to Cassandra hosts.
	 *
	 * @return the password
	 */
	@Nullable
	public String getPassword() {
		return this.password;
	}

	/**
	 * Sets the password corresponding to username.
	 *
	 * @param password the password
	 */
	public void setPassword(@Nullable String password) {
		this.password = password;
	}

	/**
	 * The data center that is considered "local" by the load balancing policy.
	 *
	 * @return the data center
	 */
	@Nullable
	public String getLocalDataCenter() {
		return this.localDataCenter;
	}

	/**
	 * Specifies the data center that is considered "local" by the load balancing policy.
	 *
	 * @param localDataCenter the data center
	 */
	public void setLocalDataCenter(@Nullable String localDataCenter) {
		this.localDataCenter = localDataCenter;
	}

	/**
	 * Returns truststore resource.
	 *
	 * @return the resource
	 */
	@Nullable
	public Resource getTruststore() {
		return this.truststore;
	}

	/**
	 * Sets truststore resource.
	 *
	 * @param truststore the resource
	 */
	public void setTruststore(@Nullable Resource truststore) {
		this.truststore = truststore;
	}

	/**
	 * Returns the password to truststore.
	 *
	 * @return the password
	 */
	@Nullable
	public String getTruststorePassword() {
		return this.truststorePassword;
	}

	/**
	 * Sets the password to truststore.
	 *
	 * @param truststorePassword the password
	 */
	public void setTruststorePassword(@Nullable String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	/**
	 * Returns keystore resource.
	 *
	 * @return the resource
	 */
	@Nullable
	public Resource getKeystore() {
		return this.keystore;
	}

	/**
	 * Sets keystore resource.
	 *
	 * @param keystore the resource
	 */
	public void setKeystore(@Nullable Resource keystore) {
		this.keystore = keystore;
	}

	/**
	 * Returns the password to keystore.
	 *
	 * @return the password
	 */
	@Nullable
	public String getKeystorePassword() {
		return this.keystorePassword;
	}

	/**
	 * Sets the password to keystore.
	 *
	 * @param keystorePassword the password
	 */
	public void setKeystorePassword(@Nullable String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	/**
	 * Whether or not to require validation that the hostname of the server certificate's common name matches the
	 * hostname of the server being connected to.
	 *
	 * @return whether hostname validation should be enabled
	 */
	public boolean isHostnameValidation() {
		return this.hostnameValidation;
	}

	/**
	 * Enables hostname validation.
	 *
	 * @param hostnameValidation whether hostname validation should be enabled
	 */
	public void setHostnameValidation(boolean hostnameValidation) {
		this.hostnameValidation = hostnameValidation;
	}

	/**
	 * Whether SSL is enabled.
	 *
	 * @return whether SSL is enabled
	 */
	public boolean isSslEnabled() {
		return this.sslEnabled;
	}

	/**
	 * Enables the use of SSL for the created Session.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 */
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	@Override
	public CqlSessionCassandraConnection create(Cassandra cassandra) {
		Objects.requireNonNull(cassandra, "'cassandra' must not be null");
		CqlSession session = createSession(cassandra);
		return new CqlSessionCassandraConnection(session);
	}

	private CqlSession createSession(Cassandra cassandra) {
		ProgrammaticDriverConfigLoaderBuilder driverBuilder = DriverConfigLoader.programmaticBuilder()
				.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
				.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(3));
		String username = getUsername();
		String password = getPassword();
		if (username != null && password != null) {
			driverBuilder.withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, username)
					.withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, password)
					.withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class);
		}
		if (isSslEnabled()) {
			driverBuilder.withBoolean(DefaultDriverOption.SSL_HOSTNAME_VALIDATION, isHostnameValidation())
					.withClass(DefaultDriverOption.SSL_ENGINE_FACTORY_CLASS, DefaultSslEngineFactory.class);
			List<String> cipherSuites = getCipherSuites();
			if (!cipherSuites.isEmpty()) {
				driverBuilder.withStringList(DefaultDriverOption.SSL_CIPHER_SUITES, new ArrayList<>(cipherSuites));
			}
			Resource truststore = getTruststore();
			if (truststore != null) {
				driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PATH, getPath(truststore).toString());
			}
			String truststorePassword = getTruststorePassword();
			if (truststorePassword != null) {
				driverBuilder.withString(DefaultDriverOption.SSL_TRUSTSTORE_PASSWORD, truststorePassword);
			}
			Resource keystore = getKeystore();
			if (keystore != null) {
				driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PATH, getPath(keystore).toString());
			}
			String keystorePassword = getKeystorePassword();
			if (keystorePassword != null) {
				driverBuilder.withString(DefaultDriverOption.SSL_KEYSTORE_PASSWORD, keystorePassword);
			}
		}
		List<Consumer<? super ProgrammaticDriverConfigLoaderBuilder>> driverConfigLoaderBuilderCustomizers =
				getDriverConfigLoaderBuilderCustomizers();
		driverConfigLoaderBuilderCustomizers.forEach(customizer -> customizer.accept(driverBuilder));
		int port = cassandra.getPort();
		int sslPort = cassandra.getSslPort();
		InetSocketAddress contactPoint = new InetSocketAddress(cassandra.getAddress(),
				(isSslEnabled() && sslPort != -1) ? sslPort : port);
		CqlSessionBuilder sessionBuilder = CqlSession.builder().addContactPoint(contactPoint)
				.withConfigLoader(driverBuilder.build());
		String localDataCenter = getLocalDataCenter();
		if (localDataCenter != null) {
			sessionBuilder.withLocalDatacenter(localDataCenter);
		}
		List<TypeCodec<?>> typeCodecs = getTypeCodecs();
		if (!typeCodecs.isEmpty()) {
			sessionBuilder.addTypeCodecs(typeCodecs.toArray(new TypeCodec[0]));
		}
		List<Consumer<? super CqlSessionBuilder>> sessionBuilderCustomizers = getSessionBuilderCustomizers();
		sessionBuilderCustomizers.forEach(customizer -> customizer.accept(sessionBuilder));
		return sessionBuilder.build();
	}

	private Path getPath(Resource resource) {
		try {
			return resource.toPath();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
