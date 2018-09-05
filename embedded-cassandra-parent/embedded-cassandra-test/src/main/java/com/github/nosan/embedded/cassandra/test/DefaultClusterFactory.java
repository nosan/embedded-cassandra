/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.test;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * {@link ClusterFactory} with a default strategy.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class DefaultClusterFactory implements ClusterFactory {

	@Nonnull
	@Override
	public Cluster create(@Nonnull Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");

		QueryOptions queryOptions = new QueryOptions();
		queryOptions.setRefreshNodeIntervalMillis(0);
		queryOptions.setRefreshNodeListIntervalMillis(0);
		queryOptions.setRefreshSchemaIntervalMillis(0);

		PoolingOptions poolingOptions = new PoolingOptions()
				.setPoolTimeoutMillis(10000)
				.setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
				.setMaxRequestsPerConnection(HostDistance.REMOTE, 2048)
				.setConnectionsPerHost(HostDistance.LOCAL, 4, 10)
				.setConnectionsPerHost(HostDistance.REMOTE, 2, 4);

		SocketOptions socketOptions = new SocketOptions();
		socketOptions.setConnectTimeoutMillis(10000);
		socketOptions.setReadTimeoutMillis(10000);

		Cluster.Builder builder = Cluster.builder()
				.addContactPoint(Objects.requireNonNull(settings.getAddress(), "Address must not be null"))
				.withCredentials("cassandra", "cassandra")
				.withPort(settings.getPort())
				.withSocketOptions(socketOptions)
				.withQueryOptions(queryOptions)
				.withPoolingOptions(poolingOptions)
				.withoutJMXReporting()
				.withoutMetrics();
		builder = configure(builder, settings);
		Objects.requireNonNull(builder, "Cluster.Builder must not be null");

		return builder.build();

	}

	@Nonnull
	protected Cluster.Builder configure(@Nonnull Cluster.Builder builder, @Nonnull Settings settings) {
		return builder;
	}
}
