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

import java.util.Objects;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.SocketOptions;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * {@link Cluster} factory with a default strategy.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class ClusterFactory {

	private static final String USERNAME = "cassandra";

	private static final String PASSWORD = "cassandra";

	/**
	 * Creates a new configured {@link Cluster}.
	 *
	 * @param settings the settings
	 * @return a cluster
	 */
	public final Cluster create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		if (settings.address().isPresent() && (settings.port().isPresent() || settings.sslPort().isPresent())) {
			SocketOptions socketOptions = new SocketOptions();
			socketOptions.setConnectTimeoutMillis(30000);
			socketOptions.setReadTimeoutMillis(30000);
			Cluster.Builder builder = Cluster.builder()
					.addContactPoints(settings.getAddress())
					.withPort(settings.port().orElseGet(settings::getSslPort))
					.withCredentials(USERNAME, PASSWORD)
					.withSocketOptions(socketOptions)
					.withoutJMXReporting()
					.withoutMetrics();
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

}
