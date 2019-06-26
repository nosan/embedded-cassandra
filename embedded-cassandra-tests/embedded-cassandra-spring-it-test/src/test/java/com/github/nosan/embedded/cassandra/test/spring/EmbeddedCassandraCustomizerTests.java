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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterConnection;
import com.github.nosan.embedded.cassandra.test.Connection;
import com.github.nosan.embedded.cassandra.test.ConnectionFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings({"NullableProblems", "deprecation"})
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@EmbeddedCassandra
class EmbeddedCassandraCustomizerTests {

	@Autowired
	private TestCassandra cassandra;

	@Autowired
	private CqlSession session;

	@Test
	void shouldSelectFromRoles() {
		assertThat(this.session.execute("SELECT * FROM test.roles").wasApplied()).isTrue();
		assertThat(getSession(this.cassandra.getConnection()).execute("SELECT * FROM test.roles").wasApplied())
				.isTrue();
	}

	@Test
	void customizerWasInvoked() {
		assertThat(this.cassandra.getVersion()).isEqualTo(Version.parse("3.11.3"));
	}

	private Session getSession(Connection connection) {
		Assert.isInstanceOf(ClusterConnection.class, connection);
		Cluster cluster = ((ClusterConnection) connection).get();
		return cluster.connect();
	}

	@Import(CqlSessionConfiguration.class)
	@Configuration
	static class TestConfiguration {

		@Order(0)
		@Bean
		public EmbeddedCassandraFactoryCustomizer<LocalCassandraFactory> versionCustomizer() {
			return factory -> factory.setVersion(Version.parse("3.11.3"));
		}

		@Order(1)
		@Bean
		public EmbeddedCassandraFactoryCustomizer<LocalCassandraFactory> assertCustomizer() {
			return factory -> Assert.state(Version.parse("3.11.3").equals(factory.getVersion()),
					"version customizer was not invoked");
		}

		@Bean
		public EmbeddedCassandraFactoryCustomizer<TestFactory> typeMismatchCustomizer() {
			return factory -> {
				throw new IllegalStateException();
			};

		}

		@Bean
		public ConnectionFactory connectionFactory() {
			return ClusterConnection::new;
		}

		@Bean
		public TestCassandraFactory testCassandraFactory() {
			return (cassandraFactory, scripts) -> new TestCassandra(cassandraFactory, CqlScript.classpath("init.cql"));
		}

		private static final class TestFactory implements CassandraFactory {

			@Override
			public Cassandra create() {
				throw new IllegalStateException();
			}

		}

	}

}
