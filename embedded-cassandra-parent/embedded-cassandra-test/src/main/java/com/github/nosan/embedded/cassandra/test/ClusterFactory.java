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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link Cluster} factory with a default strategy.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class ClusterFactory {

	private final List<TypeCodec<?>> typeCodecs = new ArrayList<>();

	@Nullable
	private String username;

	@Nullable
	private String password;

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

	private boolean metricsEnabled;

	private boolean jmxEnabled;

	private boolean sslEnabled;

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
	 * Enables metrics collection for the created cluster.
	 *
	 * @param metricsEnabled whether metrics should be enabled
	 * @since 2.0.3
	 */
	public void setMetricsEnabled(boolean metricsEnabled) {
		this.metricsEnabled = metricsEnabled;
	}

	/**
	 * Enables JMX reporting of the metrics.
	 *
	 * @param jmxEnabled whether JMX reporting should be enabled
	 * @since 2.0.3
	 */
	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	/**
	 * Enables the use of SSL for the created Cluster.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 * @since 2.0.3
	 */
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
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
	 * Creates a new configured {@link Cluster}.
	 *
	 * @param settings the settings
	 * @return a cluster
	 */
	public Cluster create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		Integer port = settings.portOrSslPort().orElse(null);
		Integer sslPort = settings.sslPort().orElse(null);
		InetAddress address = settings.address().orElse(null);
		if (address != null && port != null) {
			SocketOptions socketOptions = new SocketOptions();
			socketOptions.setConnectTimeoutMillis(30000);
			socketOptions.setReadTimeoutMillis(30000);
			Cluster.Builder builder = Cluster.builder().addContactPoints(address)
					.withPort((this.sslEnabled && sslPort != null) ? sslPort : port)
					.withSocketOptions(socketOptions);
			if (!this.metricsEnabled) {
				builder.withoutMetrics();
			}
			if (!this.jmxEnabled) {
				builder.withoutJMXReporting();
			}
			if (this.username != null && this.password != null) {
				builder.withCredentials(this.username, this.password);
			}
			if (this.sslEnabled) {
				RemoteEndpointAwareJdkSSLOptions.Builder sslOptionsBuilder = RemoteEndpointAwareJdkSSLOptions.builder();
				if (this.keystorePath != null || this.truststorePath != null) {
					sslOptionsBuilder.withSSLContext(getSslContext());
				}
				if (this.cipherSuites != null) {
					sslOptionsBuilder.withCipherSuites(this.cipherSuites);
				}
				builder.withSSL(sslOptionsBuilder.build());
			}
			if (!this.typeCodecs.isEmpty()) {
				builder.withCodecRegistry(new CodecRegistry().register(this.typeCodecs));
			}
			Cluster cluster = buildCluster(builder);
			return Objects.requireNonNull(cluster, "Cluster must not be null");
		}
		throw new IllegalStateException(String.format("Cluster can not be created from %s", settings));

	}

	/**
	 * Creates a new configured {@link Cluster}.
	 *
	 * @param builder a cluster builder
	 * @return a cluster
	 * @since 2.0.1
	 */
	protected Cluster buildCluster(Cluster.Builder builder) {
		return builder.build();
	}

	private SSLContext getSslContext() {
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			TrustManagerFactory tmf = null;
			if (this.truststorePath != null) {
				try (InputStream tsf = new BufferedInputStream(Files.newInputStream(this.truststorePath))) {
					KeyStore ts = KeyStore.getInstance("JKS");
					char[] password = (this.truststorePassword != null) ? this.truststorePassword.toCharArray() : null;
					ts.load(tsf, password);
					tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					tmf.init(ts);
				}
			}
			KeyManagerFactory kmf = null;
			if (this.keystorePath != null) {
				try (InputStream ksf = new BufferedInputStream(Files.newInputStream(this.keystorePath))) {
					KeyStore ks = KeyStore.getInstance("JKS");
					char[] password = (this.keystorePassword != null) ? this.keystorePassword.toCharArray() : null;
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
