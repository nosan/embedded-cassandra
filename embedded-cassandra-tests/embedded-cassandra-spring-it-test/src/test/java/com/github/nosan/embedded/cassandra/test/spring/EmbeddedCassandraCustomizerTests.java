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

import com.datastax.driver.core.Session;
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
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterConnection;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
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
@EmbeddedCassandra
class EmbeddedCassandraCustomizerTests {

	@Autowired
	private TestCassandra cassandra;

	@Autowired
	private CqlSession session;

	@Test
	void shouldSelectFromRoles() {
		assertThat(this.session.execute("SELECT * FROM test.roles").wasApplied()).isTrue();
		assertThat(((Session) this.cassandra.getConnection().getNativeConnection())
				.execute("SELECT * FROM test.roles").wasApplied()).isTrue();
	}

	@Test
	void customizerWasInvoked() {
		assertThat(this.cassandra.getVersion()).isEqualTo(Version.parse("3.11.3"));
	}

	@Import(CqlSessionConfiguration.class)
	@Configuration
	static class TestConfiguration {

		@Bean
		public EmbeddedCassandraFactoryCustomizer<LocalCassandraFactory> versionCustomizer() {
			return factory -> factory.setVersion(Version.parse("3.11.3"));
		}

		@Bean
		public ConnectionFactory connectionFactory() {
			return settings -> new ClusterConnection(new ClusterFactory().create(settings));
		}

		@Bean
		public TestCassandraFactory testCassandraFactory() {
			return (cassandraFactory, connectionFactory, scripts) -> new TestCassandra(cassandraFactory,
					connectionFactory, CqlScript.classpath("init.cql"));
		}

	}

}
