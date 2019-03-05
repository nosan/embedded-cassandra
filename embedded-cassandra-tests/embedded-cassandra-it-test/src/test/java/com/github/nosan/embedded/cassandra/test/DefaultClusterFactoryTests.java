/*
 * Copyright 2018-2019 the original author or authors.
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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.MetricsOptions;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.lang.Nullable;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultClusterFactory}.
 *
 * @author Dmytro Nosan
 */
public class DefaultClusterFactoryTests {

	private final DefaultClusterFactory factory = new DefaultClusterFactory();

	@Test
	public void defaultSettings() {
		Cluster cluster = this.factory.create(new TestSettings(9042, null, null));

		Configuration configuration = cluster.getConfiguration();

		ProtocolOptions protocolOptions = configuration.getProtocolOptions();
		assertThat(protocolOptions.getPort()).isEqualTo(9042);
		assertThat(ReflectionUtils.getField(protocolOptions.getAuthProvider(), "username")).isEqualTo("cassandra");
		assertThat(ReflectionUtils.getField(protocolOptions.getAuthProvider(), "password")).isEqualTo("cassandra");
		assertThat(cluster.getClusterName()).isEqualTo("cluster1");

		SocketOptions socketOptions = configuration.getSocketOptions();
		assertThat(socketOptions.getConnectTimeoutMillis()).isEqualTo(30000);
		assertThat(socketOptions.getReadTimeoutMillis()).isEqualTo(30000);

		MetricsOptions metricsOptions = configuration.getMetricsOptions();
		assertThat(metricsOptions.isEnabled()).isFalse();
		assertThat(metricsOptions.isJMXReportingEnabled()).isFalse();

	}

	@Test
	public void customSettings() {
		Cluster cluster = this.factory.create(new TestSettings(9000, "google.com", "my name"));

		Configuration configuration = cluster.getConfiguration();

		SocketOptions socketOptions = configuration.getSocketOptions();
		socketOptions.setConnectTimeoutMillis(200);
		socketOptions.setReadTimeoutMillis(200);

		ProtocolOptions protocolOptions = configuration.getProtocolOptions();
		assertThat(protocolOptions.getPort()).isEqualTo(9000);
		assertThat(cluster.getClusterName()).isEqualTo("my name");

		assertThatThrownBy(cluster::connect)
				.isInstanceOf(NoHostAvailableException.class)
				.hasStackTraceContaining("google.com");
	}

	private static final class TestSettings implements Settings {

		private final int port;

		@Nullable
		private final String address;

		@Nullable
		private final String clusterName;

		private TestSettings(int port, @Nullable String address, @Nullable String clusterName) {
			this.port = port;
			this.address = address;
			this.clusterName = clusterName;
		}

		@Nullable
		@Override
		public String getClusterName() {
			return this.clusterName;
		}

		@Nullable
		@Override
		public String getRpcAddress() {
			return this.address;
		}

		@Override
		public int getPort() {
			return this.port;
		}

	}

}
