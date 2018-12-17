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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MapSettings}.
 *
 * @author Dmytro Nosan
 */
public class MapSettingsTests {

	@Test
	public void shouldParseCassandraYamlUtf8() throws Exception {
		assertSettings(load("/cassandra.yaml"));
	}

	@Test
	public void shouldParseCassandraYamlUtf16() throws Exception {
		assertSettings(load("/cassandra-utf16.yaml"));
	}

	@Test
	public void shouldBeTheSameSettings() throws Exception {
		assertThat(load("/cassandra-utf16.yaml"))
				.isEqualTo(load("/cassandra.yaml"));
	}

	@Test
	public void prettyToString() throws Exception {
		assertThat(load("/cassandra.yaml").toString())
				.isEqualTo("MapSettings[port=9042, rpcPort=9160, storagePort=7000, sslStoragePort=7001," +
						" startNativeTransport=true, startRpc=false, clusterName='Test Cluster', " +
						"sslPort=9142, rpcAddress='localhost', listenAddress='localhost'," +
						" broadcastAddress='1.2.3.4', broadcastRpcAddress='1.2.3.4', listenInterface='eth0', " +
						"rpcInterface='eth1', version=3.11.3, rpcInterfacePreferIpv6=false, " +
						"listenInterfacePreferIpv6=false, listenOnBroadcastAddress=false]");
	}

	@Test
	public void defaultSettingsV3() {
		MapSettings settings = new MapSettings(null, new Version(3, 11, 3));
		assertThat(settings.getClusterName()).isEqualTo("Test Cluster");
		assertThat(settings.getPort()).isEqualTo(9042);
		assertThat(settings.getRpcPort()).isEqualTo(9160);
		assertThat(settings.getStoragePort()).isEqualTo(7000);
		assertThat(settings.getSslStoragePort()).isEqualTo(7001);
		assertThat(settings.isStartNativeTransport()).isTrue();
		assertThat(settings.isStartRpc()).isTrue();
		assertThat(settings.getSslPort()).isNull();
		assertThat(settings.getRpcAddress()).isNull();
		assertThat(settings.getListenAddress()).isNull();
		assertThat(settings.getListenInterface()).isNull();
		assertThat(settings.getBroadcastAddress()).isNull();
		assertThat(settings.getRpcInterface()).isNull();
		assertThat(settings.getVersion()).isEqualTo(new Version(3, 11, 3));
		assertThat(settings.isListenInterfacePreferIpv6()).isEqualTo(false);
		assertThat(settings.isRpcInterfacePreferIpv6()).isEqualTo(false);
		assertThat(settings.isListenOnBroadcastAddress()).isEqualTo(false);
		assertThat(settings.getBroadcastRpcAddress()).isNull();
	}

	@Test
	public void defaultSettingsV4() {
		MapSettings settings = new MapSettings(null, new Version(4, 0, 0));
		assertThat(settings.getClusterName()).isEqualTo("Test Cluster");
		assertThat(settings.getPort()).isEqualTo(9042);
		assertThat(settings.getRpcPort()).isEqualTo(9160);
		assertThat(settings.getStoragePort()).isEqualTo(7000);
		assertThat(settings.getSslStoragePort()).isEqualTo(7001);
		assertThat(settings.isStartNativeTransport()).isTrue();
		assertThat(settings.isStartRpc()).isFalse();
		assertThat(settings.getSslPort()).isNull();
		assertThat(settings.getRpcAddress()).isNull();
		assertThat(settings.getListenAddress()).isNull();
		assertThat(settings.getListenInterface()).isNull();
		assertThat(settings.getBroadcastAddress()).isNull();
		assertThat(settings.getRpcInterface()).isNull();
		assertThat(settings.getVersion()).isEqualTo(new Version(4, 0, 0));
		assertThat(settings.isListenInterfacePreferIpv6()).isEqualTo(false);
		assertThat(settings.isRpcInterfacePreferIpv6()).isEqualTo(false);
		assertThat(settings.isListenOnBroadcastAddress()).isEqualTo(false);
		assertThat(settings.getBroadcastRpcAddress()).isNull();
	}

	private void assertSettings(MapSettings settings) {
		assertThat(settings.getClusterName()).isEqualTo("Test Cluster");
		assertThat(settings.getPort()).isEqualTo(9042);
		assertThat(settings.getRpcPort()).isEqualTo(9160);
		assertThat(settings.getStoragePort()).isEqualTo(7000);
		assertThat(settings.getSslStoragePort()).isEqualTo(7001);
		assertThat(settings.isStartNativeTransport()).isTrue();
		assertThat(settings.isStartRpc()).isFalse();
		assertThat(settings.getSslPort()).isEqualTo(9142);
		assertThat(settings.getRpcAddress()).isEqualTo("localhost");
		assertThat(settings.getListenAddress()).isEqualTo("localhost");
		assertThat(settings.getListenInterface()).isEqualTo("eth0");
		assertThat(settings.getBroadcastAddress()).isEqualTo("1.2.3.4");
		assertThat(settings.getRpcInterface()).isEqualTo("eth1");
		assertThat(settings.getVersion()).isEqualTo(new Version(3, 11, 3));
		assertThat(settings.isListenInterfacePreferIpv6()).isEqualTo(false);
		assertThat(settings.isRpcInterfacePreferIpv6()).isEqualTo(false);
		assertThat(settings.isListenOnBroadcastAddress()).isEqualTo(false);
		assertThat(settings.getBroadcastRpcAddress()).isEqualTo("1.2.3.4");
	}

	private MapSettings load(String resource) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(resource)) {
			return new MapSettings(new Yaml().loadAs(is, Map.class), new Version(3, 11, 3));
		}
	}
}
