/*
 * Copyright 2020-2021 the original author or authors.
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

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultSettings}.
 *
 * @author Dmytro Nosan
 */
class DefaultSettingsTests {

	@Test
	void checkConstruct(@TempDir Path workingDirectory) {
		String name = "cassandra-0";
		Version version = CassandraBuilder.DEFAULT_VERSION;
		InetAddress address = InetAddress.getLoopbackAddress();
		int port = 9042;
		int sslPort = 9142;
		Path configurationFile = workingDirectory.resolve("conf/cassandra.yaml");
		LinkedHashSet<String> jvmOptions = new LinkedHashSet<>(Collections.singletonList("-Xmx512m"));
		LinkedHashMap<String, String> systemProperties = new LinkedHashMap<>(
				Collections.singletonMap("cassandra.rpc_port", "9160"));
		LinkedHashMap<String, String> environmentVariables = new LinkedHashMap<>(Collections.singletonMap(
				"JAVA_HOME", System.getProperty("java.home")));
		LinkedHashMap<String, Object> configProperties = new LinkedHashMap<>(
				Collections.singletonMap("rpc_port", 9160));
		DefaultSettings settings = new DefaultSettings(name, version, address, true, port, sslPort,
				configurationFile, workingDirectory, jvmOptions, systemProperties,
				environmentVariables, configProperties);

		assertThat(settings.getName()).isEqualTo(name);
		assertThat(settings.getVersion()).isEqualTo(version);
		assertThat(settings.getAddress()).isEqualTo(address);
		assertThat(settings.isNativeTransportEnabled()).isTrue();
		assertThat(settings.getConfigurationFile()).isEqualTo(configurationFile);
		assertThat(settings.getWorkingDirectory()).isEqualTo(workingDirectory);
		assertThat(settings.getJvmOptions()).containsAll(jvmOptions);
		assertThat(settings.getSystemProperties()).containsAllEntriesOf(systemProperties);
		assertThat(settings.getEnvironmentVariables()).containsAllEntriesOf(environmentVariables);
		assertThat(settings.getConfigProperties()).containsAllEntriesOf(configProperties);
	}

}
