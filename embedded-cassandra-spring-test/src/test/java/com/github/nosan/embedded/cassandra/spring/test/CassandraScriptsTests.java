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

import java.net.InetAddress;
import java.util.Optional;

import com.datastax.driver.core.Cluster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.api.Cassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraScripts}.
 *
 * @author Dmytro Nosan
 */
@EmbeddedCassandra
@CassandraScripts("schema.cql")
@ExtendWith(SpringExtension.class)
@DirtiesContext
class CassandraScriptsTests {

	private final Cluster cluster;

	CassandraScriptsTests(@Autowired Cluster cluster) {
		this.cluster = cluster;
	}

	@Test
	void testCqlScripts() {
		assertThat(this.cluster.getMetadata().getKeyspace("test")).isNotNull();
	}

	@Configuration
	static class TestCassandraConfiguration extends AbstractCassandraConfiguration {

		private final Cassandra cassandra;

		TestCassandraConfiguration(Cassandra cassandra) {
			this.cassandra = cassandra;
		}

		@Override
		protected String getContactPoints() {
			return Optional.ofNullable(this.cassandra.getAddress()).map(InetAddress::getHostAddress).orElse(
					super.getContactPoints());
		}

		@Override
		protected int getPort() {
			return this.cassandra.getPort();
		}

		@Override
		protected String getKeyspaceName() {
			return "test";
		}

	}

}
