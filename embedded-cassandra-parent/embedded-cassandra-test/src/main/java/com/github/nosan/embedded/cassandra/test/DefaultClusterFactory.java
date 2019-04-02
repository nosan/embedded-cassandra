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
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link ClusterFactory} with a default strategy.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public class DefaultClusterFactory implements ClusterFactory {

	@Override
	public Cluster create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");

		QueryOptions queryOptions = new QueryOptions();
		queryOptions.setRefreshNodeIntervalMillis(0);
		queryOptions.setRefreshNodeListIntervalMillis(0);
		queryOptions.setRefreshSchemaIntervalMillis(0);

		PoolingOptions poolingOptions = new PoolingOptions();
		poolingOptions.setPoolTimeoutMillis(10000);

		SocketOptions socketOptions = new SocketOptions();
		socketOptions.setConnectTimeoutMillis(10000);
		socketOptions.setReadTimeoutMillis(10000);

		Cluster.Builder builder = Cluster.builder().addContactPoints(settings.getRealAddress())
				.withCredentials("cassandra", "cassandra").withPort(settings.getPort()).withSocketOptions(socketOptions)
				.withQueryOptions(queryOptions).withPoolingOptions(poolingOptions);
		if (StringUtils.hasText(settings.getClusterName())) {
			builder = builder.withClusterName(settings.getClusterName());
		}
		builder = configure(builder, settings);
		Objects.requireNonNull(builder, "Cluster.Builder must not be null");
		return builder.build();

	}

	/**
	 * Configure a {@link Cluster#builder()}.
	 *
	 * @param builder almost configured builder
	 * @param settings a cassandra settings
	 * @return a builder
	 */
	protected Cluster.Builder configure(Cluster.Builder builder, Settings settings) {
		if (settings.getSslPort() != null && settings.getSslPort() == settings.getPort()) {
			builder = builder.withSSL();
		}
		return builder.withoutJMXReporting().withoutMetrics();
	}

}
