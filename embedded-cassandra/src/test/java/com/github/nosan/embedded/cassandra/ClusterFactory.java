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

package com.github.nosan.embedded.cassandra;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.SocketOptions;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * Factory that can be used to create a {@link Cluster}.
 *
 * @author Dmytro Nosan
 */
class ClusterFactory {

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

	private boolean sslEnabled;

	/**
	 * The path to the truststore.
	 *
	 * @param truststorePath the path
	 */
	void setTruststorePath(@Nullable Path truststorePath) {
		this.truststorePath = truststorePath;
	}

	/**
	 * The password to truststore.
	 *
	 * @param truststorePassword the password
	 */
	void setTruststorePassword(@Nullable String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	/**
	 * The path to the keystore.
	 *
	 * @param keystorePath the path
	 */
	void setKeystorePath(@Nullable Path keystorePath) {
		this.keystorePath = keystorePath;
	}

	/**
	 * The password to keystore.
	 *
	 * @param keystorePassword the password
	 */
	void setKeystorePassword(@Nullable String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	/**
	 * Enables the use of SSL for the created Cluster.
	 *
	 * @param sslEnabled whether SSL should be enabled
	 */
	void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	/**
	 * The username to use to login to Cassandra hosts.
	 *
	 * @param username the username
	 */
	void setUsername(@Nullable String username) {
		this.username = username;
	}

	/**
	 * The password corresponding to username.
	 *
	 * @param password the password
	 */
	void setPassword(@Nullable String password) {
		this.password = password;
	}

	/**
	 * Creates a new configured {@link Cluster}.
	 *
	 * @param cassandra the Cassandra instance
	 * @return a cluster
	 */
	Cluster create(Cassandra cassandra) throws Exception {
		int port = cassandra.getPort();
		int sslPort = cassandra.getSslPort();
		InetAddress address = cassandra.getAddress();
		SocketOptions socketOptions = new SocketOptions();
		socketOptions.setConnectTimeoutMillis(30000);
		socketOptions.setReadTimeoutMillis(30000);
		Cluster.Builder builder = Cluster.builder().addContactPoints(address).withPort(
				(this.sslEnabled && sslPort != -1) ? sslPort : port).withSocketOptions(socketOptions).withoutMetrics()
				.withoutJMXReporting();
		if (this.username != null && this.password != null) {
			builder.withCredentials(this.username, this.password);
		}
		if (this.sslEnabled) {
			RemoteEndpointAwareJdkSSLOptions.Builder sslOptionsBuilder = RemoteEndpointAwareJdkSSLOptions.builder();
			if (this.keystorePath != null || this.truststorePath != null) {
				sslOptionsBuilder.withSSLContext(getSslContext());
			}
			builder.withSSL(sslOptionsBuilder.build());
		}
		return builder.build();

	}

	private SSLContext getSslContext() throws Exception {
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

}
