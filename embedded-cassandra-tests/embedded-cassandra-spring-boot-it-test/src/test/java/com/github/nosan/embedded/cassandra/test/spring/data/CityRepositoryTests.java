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

package com.github.nosan.embedded.cassandra.test.spring.data;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cassandra.ClusterBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.CassandraTemplate;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.test.spring.EmbeddedCassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample tests for {@link EmbeddedCassandra}.
 *
 * @author Dmytro Nosan
 */
@SpringBootTest
@EmbeddedCassandra(scripts = "/setup.cql", port = "0")
class CityRepositoryTests {

	@Autowired
	private CassandraTemplate cassandraTemplate;

	@Autowired
	private CityRepository cityRepository;

	@Test
	void testRepository() {
		CityEntity city = new CityEntity();
		city.setId(Long.MAX_VALUE - 1);
		city.setName("Lviv");
		city = this.cityRepository.save(city);
		assertThat(city.getId()).isNotNull();
		assertThat(this.cassandraTemplate.exists(city.getId(), CityEntity.class)).isTrue();
	}

	@TestConfiguration
	static class EmbeddedClusterConfiguration {

		@Bean
		ClusterBuilderCustomizer embeddedClusterCustomizer(TestCassandra cassandra) {
			return builder -> {
				Settings settings = cassandra.getSettings();
				Integer port = settings.portOrSslPort().orElse(null);
				InetAddress address = settings.address().orElse(null);
				if (port != null && address != null) {
					builder.withPort(port).addContactPoints(address);
				}
			};
		}

	}

}
