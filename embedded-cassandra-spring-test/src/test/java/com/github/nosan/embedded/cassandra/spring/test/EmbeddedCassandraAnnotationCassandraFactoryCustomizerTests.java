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

package com.github.nosan.embedded.cassandra.spring.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandra} with a custom {@link CassandraFactoryCustomizer}.
 *
 * @author Dmytro Nosan
 */
@EmbeddedCassandra
@ExtendWith(SpringExtension.class)
class EmbeddedCassandraAnnotationCassandraFactoryCustomizerTests {

	@Test
	void test(@Autowired Cassandra cassandra) {
		assertThat(cassandra.getPort()).isEqualTo(9042);
	}

	@Configuration
	static class TestConfig {

		@Bean
		public CassandraFactoryCustomizer<EmbeddedCassandraFactory> portFactoryCustomizer() {
			return cassandraFactory -> cassandraFactory.setPort(9042);
		}

	}

}
