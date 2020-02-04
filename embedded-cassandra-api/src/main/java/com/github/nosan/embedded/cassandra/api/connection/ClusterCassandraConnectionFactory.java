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

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.TypeCodec;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

/**
 * {@link CassandraConnectionFactory} that can be used to create and configure a {@link ClusterCassandraConnection}.
 *
 * @author Dmytro Nosan
 * @see ClusterCassandraConnectionBuilder
 * @since 3.0.0
 */
public final class ClusterCassandraConnectionFactory implements CassandraConnectionFactory {

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

	private boolean metricsEnabled;

	private boolean jmxEnabled;

	private boolean sslEnabled;

	/**
	 * Whether metrics collection for the created cluster.
	 *
	 * @return whether metrics should be enabled
	 */
	public boolean isMetricsEnabled() {
		return this.metricsEnabled;
	}

	/**
	 * Enables metrics collection for the created cluster.
	 *
	 * @param metricsEnabled whether metrics should be enabled
	 */
	public void setMetricsEnabled(boolean metricsEnabled) {
		this.metricsEnabled = metricsEnabled;
	}

	/**
	 * Whether JMX reporting of the metrics.
	 *
	 * @return whether JMX reporting should be enabled
	 */
	public boolean isJmxEnabled() {
		return this.jmxEnabled;
	}

	/**
	 * Enables JMX reporting of the metrics.
	 *
	 * @param jmxEnabled whether JMX reporting should be enabled
	 */
	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	/**
	 * Additional {@link Cluster.Builder} customizers.
	 *
	 * @return builder customizers.
	 */
	public List<Consumer<? super Cluster.Builder>> getClusterBuilderCustomizers() {
		return this.clusterBuilderCustomizers;
	}

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
	 * Whether SSL is enabled.
	 *
	 * @return whether SSL is enabled
	 */
	public boolean isSslEnabled() {
		return this.sslEnabled;
	}

	/**
	 * Enables the use of SSL for the created Cluster.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 */
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	@Override
	public ClusterCassandraConnection create(Cassandra cassandra) {
		Objects.requireNonNull(cassandra, "'cassandra' must not be null");
		Cluster cluster = createCluster(cassandra);
		return new ClusterCassandraConnection(cluster);
	}

	private Cluster createCluster(Cassandra cassandra) {
		SocketOptions socketOptions = new SocketOptions();
		socketOptions.setConnectTimeoutMillis(30000);
		socketOptions.setReadTimeoutMillis(30000);
		int port = cassandra.getPort();
		int sslPort = cassandra.getSslPort();
		Cluster.Builder builder = Cluster.builder().addContactPoints(cassandra.getAddress())
				.withPort(isSslEnabled() && sslPort != -1 ? sslPort : port)
				.withSocketOptions(socketOptions);
		if (!isMetricsEnabled()) {
			builder.withoutMetrics();
		}
		if (!isJmxEnabled()) {
			builder.withoutJMXReporting();
		}
		String username = getUsername();
		String password = getPassword();
		if (username != null && password != null) {
			builder.withCredentials(username, password);
		}
		if (isSslEnabled()) {
			RemoteEndpointAwareJdkSSLOptions.Builder sslOptionsBuilder = RemoteEndpointAwareJdkSSLOptions.builder();
			if (getKeystore() != null || getTruststore() != null) {
				sslOptionsBuilder.withSSLContext(getSslContext());
			}
			List<String> cipherSuites = getCipherSuites();
			if (!cipherSuites.isEmpty()) {
				sslOptionsBuilder.withCipherSuites(cipherSuites.toArray(new String[0]));
			}
			builder.withSSL(sslOptionsBuilder.build());
		}
		List<TypeCodec<?>> typeCodecs = getTypeCodecs();
		if (!typeCodecs.isEmpty()) {
			builder.withCodecRegistry(new CodecRegistry().register(typeCodecs));
		}
		this.clusterBuilderCustomizers.forEach(customizer -> customizer.accept(builder));
		return builder.build();
	}

	private SSLContext getSslContext() {
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			TrustManagerFactory tmf = null;
			Resource truststore = getTruststore();
			if (truststore != null) {
				try (InputStream tsf = truststore.getInputStream()) {
					KeyStore ts = KeyStore.getInstance("JKS");
					String truststorePassword = getTruststorePassword();
					char[] password = (truststorePassword != null) ? truststorePassword.toCharArray() : null;
					ts.load(tsf, password);
					tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					tmf.init(ts);
				}
			}
			KeyManagerFactory kmf = null;
			Resource keystore = getKeystore();
			if (keystore != null) {
				try (InputStream ksf = keystore.getInputStream()) {
					KeyStore ks = KeyStore.getInstance("JKS");
					String keystorePassword = getKeystorePassword();
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
