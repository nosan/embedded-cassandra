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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.TypeCodec;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * Builder that can be used to create and configure a {@link ClusterCassandraConnection}.
 *
 * @author Dmytro Nosan
 * @see ClusterCassandraConnectionFactory
 * @since 3.0.0
 */
public final class ClusterCassandraConnectionBuilder implements CassandraConnectionFactory {

	private final List<TypeCodec<?>> typeCodecs = new ArrayList<>();

	private final List<String> cipherSuites = new ArrayList<>();

	private final List<Consumer<? super Cluster.Builder>> clusterBuilderCustomizers = new ArrayList<>();

	@Nullable
	private String username;

	@Nullable
	private String password;

	@Nullable
	private Resource truststore;

	@Nullable
	private String truststorePassword;

	@Nullable
	private Resource keystore;

	@Nullable
	private String keystorePassword;

	@Nullable
	private Boolean metricsEnabled;

	@Nullable
	private Boolean jmxEnabled;

	@Nullable
	private Boolean sslEnabled;

	/**
	 * Sets the username to use to login to Cassandra hosts.
	 *
	 * @param username the username
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withUsername(@Nullable String username) {
		this.username = username;
		return this;
	}

	/**
	 * Sets the password corresponding to username.
	 *
	 * @param password the password
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withPassword(@Nullable String password) {
		this.password = password;
		return this;
	}

	/**
	 * Sets truststore resource.
	 *
	 * @param truststore the resource
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withTruststore(@Nullable Resource truststore) {
		this.truststore = truststore;
		return this;
	}

	/**
	 * Sets the password to truststore.
	 *
	 * @param truststorePassword the password
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withTruststorePassword(@Nullable String truststorePassword) {
		this.truststorePassword = truststorePassword;
		return this;
	}

	/**
	 * Sets keystore resource.
	 *
	 * @param keystore the resource
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withKeystore(@Nullable Resource keystore) {
		this.keystore = keystore;
		return this;
	}

	/**
	 * Sets the password to keystore.
	 *
	 * @param keystorePassword the password
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withKeystorePassword(@Nullable String keystorePassword) {
		this.keystorePassword = keystorePassword;
		return this;
	}

	/**
	 * Enables metrics collection for the created cluster.
	 *
	 * @param metricsEnabled whether metrics should be enabled
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withMetricsEnabled(@Nullable Boolean metricsEnabled) {
		this.metricsEnabled = metricsEnabled;
		return this;
	}

	/**
	 * Enables JMX reporting of the metrics.
	 *
	 * @param jmxEnabled whether JMX reporting should be enabled
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withJmxEnabled(@Nullable Boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
		return this;
	}

	/**
	 * Enables the use of SSL for the created Cluster.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder withSslEnabled(@Nullable Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
	}

	/**
	 * Add Additional codecs for custom type mappings.
	 *
	 * @param typeCodecs additional type codecs
	 * @return this builder
	 */
	public ClusterCassandraConnectionBuilder addTypeCodecs(TypeCodec<?>... typeCodecs) {
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
	public ClusterCassandraConnectionBuilder addCipherSuites(String... cipherSuites) {
		Objects.requireNonNull(cipherSuites, "'cipherSuites' must not be null");
		this.cipherSuites.addAll(Arrays.asList(cipherSuites));
		return this;
	}

	/**
	 * Add customizers that should be applied to the {@link Cluster.Builder}.
	 *
	 * @param clusterBuilderCustomizers the customizers to add
	 * @return this builder
	 */
	@SafeVarargs
	public final ClusterCassandraConnectionBuilder addClusterBuilderCustomizers(
			Consumer<? super Cluster.Builder>... clusterBuilderCustomizers) {
		Objects.requireNonNull(clusterBuilderCustomizers, "'clusterBuilderCustomizers' must not be null");
		this.clusterBuilderCustomizers.addAll(Arrays.asList(clusterBuilderCustomizers));
		return this;
	}

	@Override
	public CassandraConnection create(Cassandra cassandra) {
		Objects.requireNonNull(cassandra, "'cassandra' must not be null");
		ClusterCassandraConnectionFactory factory = new ClusterCassandraConnectionFactory();
		factory.getClusterBuilderCustomizers().addAll(this.clusterBuilderCustomizers);
		factory.getTypeCodecs().addAll(this.typeCodecs);
		factory.getCipherSuites().addAll(this.cipherSuites);
		Optional.ofNullable(this.username).ifPresent(factory::setUsername);
		Optional.ofNullable(this.truststore).ifPresent(factory::setTruststore);
		Optional.ofNullable(this.password).ifPresent(factory::setPassword);
		Optional.ofNullable(this.truststorePassword).ifPresent(factory::setTruststorePassword);
		Optional.ofNullable(this.keystore).ifPresent(factory::setKeystore);
		Optional.ofNullable(this.keystorePassword).ifPresent(factory::setKeystorePassword);
		Optional.ofNullable(this.metricsEnabled).ifPresent(factory::setMetricsEnabled);
		Optional.ofNullable(this.jmxEnabled).ifPresent(factory::setJmxEnabled);
		Optional.ofNullable(this.sslEnabled).ifPresent(factory::setSslEnabled);
		return factory.create(cassandra);
	}

}
