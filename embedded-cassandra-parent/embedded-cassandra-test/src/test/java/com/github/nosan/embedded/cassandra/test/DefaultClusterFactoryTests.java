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

import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.MetricsOptions;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultClusterFactory}.
 *
 * @author Dmytro Nosan
 */
public class DefaultClusterFactoryTests {

	private final DefaultClusterFactory factory = new DefaultClusterFactory();

	@Test
	public void create() {
		Settings settings = new Settings() {
			@Nullable
			@Override
			public String getClusterName() {
				return null;
			}

			@Override
			public int getStoragePort() {
				return 0;
			}

			@Override
			public int getSslStoragePort() {
				return 0;
			}

			@Nullable
			@Override
			public String getListenAddress() {
				return null;
			}

			@Nullable
			@Override
			public String getListenInterface() {
				return null;
			}

			@Nullable
			@Override
			public String getBroadcastAddress() {
				return null;
			}

			@Override
			public String getAddress() {
				return "localhost";
			}

			@Nullable
			@Override
			public String getRpcInterface() {
				return null;
			}

			@Nullable
			@Override
			public String getBroadcastRpcAddress() {
				return null;
			}

			@Override
			public boolean isStartNativeTransport() {
				return false;
			}

			@Override
			public int getPort() {
				return 9042;
			}

			@Nullable
			@Override
			public Integer getSslPort() {
				return null;
			}

			@Override
			public boolean isStartRpc() {
				return false;
			}

			@Override
			public int getRpcPort() {
				return 0;
			}
		};

		Cluster cluster = this.factory.create(settings);

		Configuration configuration = cluster.getConfiguration();

		ProtocolOptions protocolOptions = configuration.getProtocolOptions();
		assertThat(protocolOptions.getPort()).isEqualTo(9042);
		assertThat(ReflectionUtils.getField(protocolOptions.getAuthProvider(), "username")).isEqualTo("cassandra");
		assertThat(ReflectionUtils.getField(protocolOptions.getAuthProvider(), "password")).isEqualTo("cassandra");


		SocketOptions socketOptions = configuration.getSocketOptions();
		assertThat(socketOptions.getConnectTimeoutMillis()).isEqualTo(10000);
		assertThat(socketOptions.getReadTimeoutMillis()).isEqualTo(10000);

		MetricsOptions metricsOptions = configuration.getMetricsOptions();
		assertThat(metricsOptions.isEnabled()).isFalse();
		assertThat(metricsOptions.isJMXReportingEnabled()).isFalse();

		QueryOptions queryOptions = configuration.getQueryOptions();
		assertThat(queryOptions.getRefreshNodeIntervalMillis()).isZero();
		assertThat(queryOptions.getRefreshSchemaIntervalMillis()).isZero();
		assertThat(queryOptions.getRefreshNodeListIntervalMillis()).isZero();


		PoolingOptions poolingOptions = configuration.getPoolingOptions();
		assertThat(poolingOptions.getMaxRequestsPerConnection(HostDistance.LOCAL)).isEqualTo(32768);
		assertThat(poolingOptions.getMaxRequestsPerConnection(HostDistance.REMOTE)).isEqualTo(2048);
		assertThat(poolingOptions.getCoreConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(4);
		assertThat(poolingOptions.getMaxConnectionsPerHost(HostDistance.LOCAL)).isEqualTo(10);
		assertThat(poolingOptions.getCoreConnectionsPerHost(HostDistance.REMOTE)).isEqualTo(2);
		assertThat(poolingOptions.getMaxConnectionsPerHost(HostDistance.REMOTE)).isEqualTo(4);
		assertThat(poolingOptions.getPoolTimeoutMillis()).isEqualTo(10000);

	}
}
