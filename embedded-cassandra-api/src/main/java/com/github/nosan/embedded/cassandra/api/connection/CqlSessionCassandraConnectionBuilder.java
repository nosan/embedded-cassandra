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

package com.github.nosan.embedded.cassandra.api.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * Builder that can be used to create and configure a {@link CqlSessionCassandraConnection}.
 *
 * @author Dmytro Nosan
 * @see CqlSessionCassandraConnectionFactory
 * @since 3.0.0
 */
public final class CqlSessionCassandraConnectionBuilder implements CassandraConnectionFactory {

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
	private String localDataCenter;

	@Nullable
	private Resource truststore;

	@Nullable
	private String truststorePassword;

	@Nullable
	private Resource keystore;

	@Nullable
	private String keystorePassword;

	@Nullable
	private Boolean hostnameValidation;

	@Nullable
	private Boolean sslEnabled;

	/**
	 * Sets the username to use to login to Cassandra hosts.
	 *
	 * @param username the username
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withUsername(@Nullable String username) {
		this.username = username;
		return this;
	}

	/**
	 * Sets the password corresponding to username.
	 *
	 * @param password the password
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withPassword(@Nullable String password) {
		this.password = password;
		return this;
	}

	/**
	 * Specifies the data center that is considered "local" by the load balancing policy.
	 *
	 * @param localDataCenter the data center
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withLocalDataCenter(@Nullable String localDataCenter) {
		this.localDataCenter = localDataCenter;
		return this;
	}

	/**
	 * Sets truststore resource.
	 *
	 * @param truststore the resource
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withTruststore(@Nullable Resource truststore) {
		this.truststore = truststore;
		return this;
	}

	/**
	 * Sets the password to truststore.
	 *
	 * @param truststorePassword the password
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withTruststorePassword(@Nullable String truststorePassword) {
		this.truststorePassword = truststorePassword;
		return this;
	}

	/**
	 * Sets keystore resource.
	 *
	 * @param keystore the resource
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withKeystore(@Nullable Resource keystore) {
		this.keystore = keystore;
		return this;
	}

	/**
	 * Sets the password to keystore.
	 *
	 * @param keystorePassword the password
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withKeystorePassword(@Nullable String keystorePassword) {
		this.keystorePassword = keystorePassword;
		return this;
	}

	/**
	 * Enables hostname validation.
	 *
	 * @param hostnameValidation whether hostname validation should be enabled
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withHostnameValidation(@Nullable Boolean hostnameValidation) {
		this.hostnameValidation = hostnameValidation;
		return this;
	}

	/**
	 * Enables the use of SSL for the created CqlSession.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder withSslEnabled(@Nullable Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
	}

	/**
	 * Add Additional codecs for custom type mappings.
	 *
	 * @param typeCodecs additional type codecs
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder addTypeCodecs(TypeCodec<?>... typeCodecs) {
		Objects.requireNonNull(typeCodecs, "'typeCodecs' must not be null");
		this.typeCodecs.addAll(Arrays.asList(typeCodecs));
		return this;
	}

	/**
	 * Add cipher suites.
	 *
	 * @param cipherSuites cipher suites
	 * @return this builder
	 */
	public CqlSessionCassandraConnectionBuilder addCipherSuites(String... cipherSuites) {
		Objects.requireNonNull(cipherSuites, "'cipherSuites' must not be null");
		this.cipherSuites.addAll(Arrays.asList(cipherSuites));
		return this;
	}

	/**
	 * Add customizers that should be applied to the {@link CqlSessionBuilder}.
	 *
	 * @param sessionBuilderCustomizers the customizers to add
	 * @return this builder
	 */
	@SafeVarargs
	public final CqlSessionCassandraConnectionBuilder addSessionBuilderCustomizers(
			Consumer<? super CqlSessionBuilder>... sessionBuilderCustomizers) {
		Objects.requireNonNull(sessionBuilderCustomizers, "'sessionBuilderCustomizers' must not be null");
		this.sessionBuilderCustomizers.addAll(Arrays.asList(sessionBuilderCustomizers));
		return this;
	}

	/**
	 * Add customizers that should be applied to the {@link ProgrammaticDriverConfigLoaderBuilder}.
	 *
	 * @param driverConfigLoaderBuilderCustomizers the customizers to add
	 * @return this builder
	 */
	@SafeVarargs
	public final CqlSessionCassandraConnectionBuilder addDriverConfigLoaderBuilderCustomizers(
			Consumer<? super ProgrammaticDriverConfigLoaderBuilder>... driverConfigLoaderBuilderCustomizers) {
		Objects.requireNonNull(driverConfigLoaderBuilderCustomizers,
				"'driverConfigLoaderBuilderCustomizers' must not be null");
		this.driverConfigLoaderBuilderCustomizers.addAll(Arrays.asList(driverConfigLoaderBuilderCustomizers));
		return this;
	}

	@Override
	public CqlSessionCassandraConnection create(Cassandra cassandra) {
		Objects.requireNonNull(cassandra, "'cassandra' must not be null");
		CqlSessionCassandraConnectionFactory factory = new CqlSessionCassandraConnectionFactory();
		factory.getSessionBuilderCustomizers().addAll(this.sessionBuilderCustomizers);
		factory.getCipherSuites().addAll(this.cipherSuites);
		factory.getTypeCodecs().addAll(this.typeCodecs);
		factory.getDriverConfigLoaderBuilderCustomizers().addAll(this.driverConfigLoaderBuilderCustomizers);
		Optional.ofNullable(this.localDataCenter).ifPresent(factory::setLocalDataCenter);
		Optional.ofNullable(this.username).ifPresent(factory::setUsername);
		Optional.ofNullable(this.hostnameValidation).ifPresent(factory::setHostnameValidation);
		Optional.ofNullable(this.password).ifPresent(factory::setPassword);
		Optional.ofNullable(this.truststore).ifPresent(factory::setTruststore);
		Optional.ofNullable(this.truststorePassword).ifPresent(factory::setTruststorePassword);
		Optional.ofNullable(this.keystore).ifPresent(factory::setKeystore);
		Optional.ofNullable(this.keystorePassword).ifPresent(factory::setKeystorePassword);
		Optional.ofNullable(this.sslEnabled).ifPresent(factory::setSslEnabled);
		return factory.create(cassandra);
	}

}
