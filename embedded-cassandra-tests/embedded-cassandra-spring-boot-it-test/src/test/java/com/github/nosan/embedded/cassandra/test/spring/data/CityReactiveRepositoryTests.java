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

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;

import com.github.nosan.embedded.cassandra.test.spring.Cql;
import com.github.nosan.embedded.cassandra.test.spring.EmbeddedCassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample tests for {@link EmbeddedCassandra} using reactive repositories.
 *
 * @author Dmytro Nosan
 */
@EmbeddedCassandra(scripts = "/setup.cql")
@SpringBootTest
class CityReactiveRepositoryTests {

	@Autowired
	private ReactiveCassandraTemplate cassandraTemplate;

	@Autowired
	private CityReactiveRepository cityReactiveRepository;

	@Test
	void testRepository() {
		CityEntity city = new CityEntity();
		city.setId(Long.MAX_VALUE);
		city.setName("Lviv");
		city = this.cityReactiveRepository.save(city).block(Duration.ofSeconds(5));
		assertThat(city).isNotNull();
		assertThat(city.getId()).isNotNull();
		assertThat(this.cassandraTemplate.exists(city.getId(), CityEntity.class).block(Duration.ofSeconds(5))).isTrue();
	}

	@Test
	@Cql(statements = "INSERT INTO \"test\".\"city\" (\"id\", \"name\") VALUES (1000, 'test_city');")
	void testFindRepository() {
		assertThat(this.cassandraTemplate.exists(1000, CityEntity.class).block(Duration.ofSeconds(5))).isTrue();
	}

}
