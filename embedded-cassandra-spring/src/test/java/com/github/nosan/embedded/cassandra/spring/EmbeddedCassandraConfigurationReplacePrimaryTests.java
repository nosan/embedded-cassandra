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

package com.github.nosan.embedded.cassandra.spring;

import com.datastax.driver.core.Cluster;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraConfiguration}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@EmbeddedCassandra
public class EmbeddedCassandraConfigurationReplacePrimaryTests {

	@Autowired
	private TestService testService;

	@Test
	public void createKeyspace() {
		assertThat(this.testService.createKeyspace("test")).isTrue();
	}

	@Configuration
	@Import(TestService.class)
	static class Context {

		@Bean
		@Primary
		public Cluster cluster1() {
			return Cluster.builder().withPort(9000).addContactPoint("localhost").build();
		}

		@Bean
		public Cluster cluster2() {
			return Cluster.builder().withPort(9000).addContactPoint("localhost").build();
		}

	}

}
