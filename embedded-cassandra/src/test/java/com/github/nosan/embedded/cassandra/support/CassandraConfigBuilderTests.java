/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.support;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.CassandraConfig;
import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.process.customizer.FileCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraConfigBuilder}.
 *
 * @author Dmytro Nosan
 */
public class CassandraConfigBuilderTests {

	@Test
	public void buildDefaults() {
		CassandraConfig executableConfig = new CassandraConfigBuilder().build();
		assertThat(executableConfig.getConfig()).isNotNull();
		assertThat(executableConfig.getTimeout()).isEqualTo(Duration.ofMinutes(1));
		assertThat(executableConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(executableConfig.getFileCustomizers()).hasSize(0);
		assertThat(executableConfig.getJmxPort()).isEqualTo(7199);
		Config config = executableConfig.getConfig();
		assertThat(config.getRpcPort()).isEqualTo(9160);
		assertThat(config.getNativeTransportPort()).isEqualTo(9042);
		assertThat(config.getStoragePort()).isEqualTo(7000);
		assertThat(config.getSslStoragePort()).isEqualTo(7001);
		assertThat(config.getNativeTransportPortSsl()).isNull();

	}

	@Test
	public void buildWithRandomPorts() {
		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		assertThat(executableConfig.getConfig()).isNotNull();
		assertThat(executableConfig.getTimeout()).isEqualTo(Duration.ofMinutes(1));
		assertThat(executableConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(executableConfig.getFileCustomizers()).hasSize(0);
		assertThat(executableConfig.getJmxPort()).isNotEqualTo(7199);
		assertThat(executableConfig.getJvmOptions()).isEmpty();

		Config config = executableConfig.getConfig();
		assertThat(config.getRpcPort()).isNotEqualTo(9160);
		assertThat(config.getNativeTransportPort()).isNotEqualTo(9042);
		assertThat(config.getStoragePort()).isNotEqualTo(7000);
		assertThat(config.getSslStoragePort()).isNotEqualTo(7001);
		assertThat(config.getNativeTransportPortSsl()).isNull();

	}

	@Test
	public void build() {
		Config config = new Config();

		CassandraConfig executableConfig = new CassandraConfigBuilder()
				.jvmOptions("-Xmx512").jmxPort(5000)
				.fileCustomizers((FileCustomizer) (file, distribution) -> {
				}).version(Version.LATEST).timeout(Duration.ofMinutes(15)).config(config)
				.build();

		assertThat(executableConfig.getJvmOptions()).containsExactly("-Xmx512");
		assertThat(executableConfig.getConfig()).isEqualTo(config);
		assertThat(executableConfig.getTimeout()).isEqualTo(Duration.ofMinutes(15));
		assertThat(executableConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(executableConfig.getFileCustomizers()).hasSize(1);
		assertThat(executableConfig.getJmxPort()).isEqualTo(5000);
	}

	@Test
	public void buildSetRandomPortIfZero() {

		Config config = new Config();

		config.setNativeTransportPort(0);
		config.setNativeTransportPortSsl(0);

		CassandraConfig executableConfig = new CassandraConfigBuilder().config(config)
				.build();

		assertThat(executableConfig.getConfig()).isNotNull();
		assertThat(executableConfig.getTimeout()).isEqualTo(Duration.ofMinutes(1));
		assertThat(executableConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(executableConfig.getFileCustomizers()).hasSize(0);
		assertThat(executableConfig.getJmxPort()).isEqualTo(7199);
		assertThat(executableConfig.getJvmOptions()).isEmpty();

		assertThat(config.getRpcPort()).isEqualTo(9160);
		assertThat(config.getNativeTransportPort()).isNotEqualTo(0);
		assertThat(config.getStoragePort()).isEqualTo(7000);
		assertThat(config.getSslStoragePort()).isEqualTo(7001);
		assertThat(config.getNativeTransportPortSsl()).isNotEqualTo(0);

	}

	@Test
	public void randomPorts() {
		Set<Integer> ports = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			Config config = new Config();
			config.setNativeTransportPortSsl(0);
			CassandraConfig cassandraConfig = new CassandraConfigBuilder().config(config)
					.useRandomPorts(true).build();
			ports.add(cassandraConfig.getJmxPort());
			ports.add(config.getRpcPort());
			ports.add(config.getNativeTransportPort());
			ports.add(config.getStoragePort());
			ports.add(config.getSslStoragePort());
			ports.add(config.getNativeTransportPortSsl());
		}
		assertThat(ports).hasSize(100 * 6);
	}

}
