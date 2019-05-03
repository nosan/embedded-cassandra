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

package com.github.nosan.embedded.cassandra.local;

import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationFileRandomPortCustomizer}.
 *
 * @author Dmytro Nosan
 */
class ConfigurationFileRandomPortCustomizerTests {

	private final ConfigurationFileRandomPortCustomizer customizer = new ConfigurationFileRandomPortCustomizer(
			new RandomPortSupplier(InetAddress::getLoopbackAddress));

	@Test
	void shouldReplaceZeroPortWithRandom(@TempDir Path temporaryFolder) throws Exception {
		Path confDir = temporaryFolder.resolve("conf");
		Path configurationFile = confDir.resolve("cassandra.yaml");
		Files.createDirectories(confDir);
		try (InputStream inputStream = getClass().getResourceAsStream("/cassandra-all-ports.yaml")) {
			Files.copy(inputStream, configurationFile);
		}
		this.customizer.customize(confDir.getParent(), Version.parse("3.11.3"));
		try (InputStream inputStream = Files.newInputStream(configurationFile)) {
			Map properties = new Yaml().loadAs(inputStream, Map.class);
			assertThat(properties.get("rpc_port")).isNotEqualTo(0);
			assertThat(properties.get("ssl_storage_port")).isNotEqualTo(0);
			assertThat(properties.get("storage_port")).isNotEqualTo(0);
			assertThat(properties.get("native_transport_port")).isNotEqualTo(0);
			assertThat(properties.get("native_transport_port_ssl")).isNotEqualTo(0);
		}
	}

}
