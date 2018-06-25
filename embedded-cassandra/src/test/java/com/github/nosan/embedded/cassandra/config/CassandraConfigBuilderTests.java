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

package com.github.nosan.embedded.cassandra.config;

import java.time.Duration;

import com.github.nosan.embedded.cassandra.customizer.FileCustomizer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraConfigBuilder}.
 *
 * @author Dmytro Nosan
 */
public class CassandraConfigBuilderTests {

	@Test
	public void buildDefaults() {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder().build();
		assertThat(cassandraConfig.getConfig()).isNotNull();
		assertThat(cassandraConfig.getTimeout()).isEqualTo(Duration.ofMinutes(1));
		assertThat(cassandraConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(cassandraConfig.getFileCustomizers()).hasSize(0);
		assertThat(cassandraConfig.getJmxPort()).isEqualTo(7199);
		Config config = cassandraConfig.getConfig();
		assertThat(config.getRpcPort()).isEqualTo(9160);
		assertThat(config.getNativeTransportPort()).isEqualTo(9042);
		assertThat(config.getStoragePort()).isEqualTo(7000);
		assertThat(config.getSslStoragePort()).isEqualTo(7001);
		assertThat(config.getNativeTransportPortSsl()).isNull();

	}

	@Test
	public void buildWithRandomPorts() {
		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.useRandomPorts(true).build();
		assertThat(cassandraConfig.getConfig()).isNotNull();
		assertThat(cassandraConfig.getTimeout()).isEqualTo(Duration.ofMinutes(1));
		assertThat(cassandraConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(cassandraConfig.getFileCustomizers()).hasSize(0);
		assertThat(cassandraConfig.getJmxPort()).isNotEqualTo(7199);
		assertThat(cassandraConfig.getJvmOptions()).isEmpty();

		Config config = cassandraConfig.getConfig();
		assertThat(config.getRpcPort()).isNotEqualTo(9160);
		assertThat(config.getNativeTransportPort()).isNotEqualTo(9042);
		assertThat(config.getStoragePort()).isNotEqualTo(7000);
		assertThat(config.getSslStoragePort()).isNotEqualTo(7001);
		assertThat(config.getNativeTransportPortSsl()).isNull();

	}

	@Test
	public void build() {
		Config config = new Config();

		CassandraConfig cassandraConfig = new CassandraConfigBuilder()
				.jvmOptions("-Xmx512").jmxPort(5000)
				.fileCustomizers((FileCustomizer) (file, distribution) -> {
				}).version(Version.LATEST).timeout(Duration.ofMinutes(15)).config(config)
				.build();

		assertThat(cassandraConfig.getJvmOptions()).containsExactly("-Xmx512");
		assertThat(cassandraConfig.getConfig()).isEqualTo(config);
		assertThat(cassandraConfig.getTimeout()).isEqualTo(Duration.ofMinutes(15));
		assertThat(cassandraConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(cassandraConfig.getFileCustomizers()).hasSize(1);
		assertThat(cassandraConfig.getJmxPort()).isEqualTo(5000);
	}

}
