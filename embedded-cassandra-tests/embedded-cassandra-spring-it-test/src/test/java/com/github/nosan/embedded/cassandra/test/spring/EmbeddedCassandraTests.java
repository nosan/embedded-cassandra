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

package com.github.nosan.embedded.cassandra.test.spring;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterConnection;
import com.github.nosan.embedded.cassandra.test.ClusterConnectionFactory;
import com.github.nosan.embedded.cassandra.test.ConnectionFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("NullableProblems")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@EmbeddedCassandra(scripts = "/init.cql",
		statements = "CREATE TABLE IF NOT EXISTS test.roles (   id text PRIMARY  KEY );")
class EmbeddedCassandraTests {

	@Autowired
	private TestCassandra cassandra;

	@Autowired
	private CqlSession session;

	@Test
	void shouldSelectFromRoles() {
		assertThat(this.cassandra.getVersion()).isEqualTo(Version.parse("3.11.3"));
		assertThat(this.cassandra.getConnection()).isInstanceOf(ClusterConnection.class);
		assertThat(this.session.execute("SELECT * FROM test.roles").wasApplied()).isTrue();
	}

	@Configuration
	@Import(CqlSessionConfiguration.class)
	static class TestConfiguration {

		@Bean
		public LocalCassandraFactory localCassandraFactory() {
			LocalCassandraFactory factory = new LocalCassandraFactory();
			factory.setVersion(Version.parse("3.11.3"));
			return factory;
		}

		@Bean
		public ConnectionFactory clusterConnectionFactory() {
			return new ClusterConnectionFactory();
		}

	}

}
