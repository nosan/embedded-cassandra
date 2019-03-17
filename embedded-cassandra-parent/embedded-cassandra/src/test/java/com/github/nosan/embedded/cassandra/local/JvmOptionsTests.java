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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JvmOptions}.
 *
 * @author Dmytro Nosan
 */
class JvmOptionsTests {

	@Test
	void randomizePorts() throws IOException {
		List<String> options = new ArrayList<>();
		options.add("java.rmi.server.hostname=127.0.0.1");
		options.add("-Dcassandra.native_transport_port=0");
		options.add("-Dcassandra.rpc_port=0");
		options.add("-Dcassandra.storage_port=0");
		options.add("-Dcassandra.ssl_storage_port=0");
		options.add("-Dcassandra.jmx.local.port=0");
		options.add("-Dcassandra.jmx.remote.port=0");
		JvmOptions jvmOptions = new JvmOptions(options, 0, settings());
		assertThat(jvmOptions.getPort()).isNotEqualTo(zero());
		assertThat(jvmOptions.getSslStoragePort()).isNotEqualTo(zero());
		assertThat(jvmOptions.getRpcPort()).isNotEqualTo(zero());
		assertThat(jvmOptions.getStoragePort()).isNotEqualTo(zero());
		assertThat(jvmOptions.getJmxRemotePort()).isNotEqualTo(zero());
		assertThat(jvmOptions.getJmxLocalPort()).isNotEqualTo(zero());
	}

	@Test
	void parseJvmOptions() throws IOException {
		List<String> options = new ArrayList<>();
		options.add("-Dcassandra.native_transport_port=9042");
		options.add("-Dcassandra.rpc_port=9160");
		options.add("-Dcassandra.storage_port=7000");
		options.add("-Dcassandra.ssl_storage_port=7001");
		options.add("-Dcassandra.jmx.local.port=7199");
		options.add("-Dcassandra.jmx.remote.port=8000");
		options.add("-Dcassandra.start_rpc=true");
		options.add("-Dcassandra.start_native_transport=true");
		options.add("-X512m");
		JvmOptions jvmOptions = new JvmOptions(options, 0, settings());
		assertThat(jvmOptions.getPort()).hasValue(9042);
		assertThat(jvmOptions.getRpcPort()).hasValue(9160);
		assertThat(jvmOptions.getSslStoragePort()).hasValue(7001);
		assertThat(jvmOptions.getStoragePort()).hasValue(7000);
		assertThat(jvmOptions.getJmxRemotePort()).hasValue(8000);
		assertThat(jvmOptions.getJmxLocalPort()).hasValue(7199);
		assertThat(jvmOptions.getJmxLocalPort()).hasValue(7199);
		assertThat(jvmOptions.isStartRpc()).hasValue(true);
		assertThat(jvmOptions.isStartNativeTransport()).hasValue(true);

		assertThat(jvmOptions.get())
				.contains("-Dcassandra.jmx.local.port=7199", "-Dcassandra.native_transport_port=9042",
						"-Dcassandra.rpc_port=9160", "-Dcassandra.storage_port=7000",
						"-Dcassandra.ssl_storage_port=7001", "-Dcassandra.jmx.remote.port=8000",
						"-Dcassandra.start_rpc=true", "-Dcassandra.start_native_transport=true", "-X512m");
	}

	private NodeSettings settings() throws IOException {
		try (InputStream is = getClass().getResourceAsStream("/cassandra.yaml")) {
			return new NodeSettings(new Version(3, 11, 3), new Yaml().loadAs(is, Map.class));
		}
	}

	private static Optional<Integer> zero() {
		return Optional.of(0);
	}

}
