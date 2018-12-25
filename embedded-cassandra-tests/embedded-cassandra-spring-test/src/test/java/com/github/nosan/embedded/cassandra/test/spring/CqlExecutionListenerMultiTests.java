/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.spring;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.nosan.embedded.cassandra.test.junit.CassandraRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlExecutionListener}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CqlExecutionListenerMultiTests.Context.class,
		CqlExecutionListenerMultiTests.Context2.class})
@Cql(scripts = "/init.cql")
@Cql(statements = "DROP KEYSPACE test", executionPhase = Cql.ExecutionPhase.AFTER_TEST_METHOD)
public class CqlExecutionListenerMultiTests {

	@ClassRule
	public static final CassandraRule cassandra = new CassandraRule();

	@Autowired
	private Cluster cluster;

	@Test
	@Cql(scripts = "/users-data.cql")
	public void shouldHaveUser() {
		try (Session session = this.cluster.connect()) {
			ResultSet rs = session.execute("SELECT COUNT(*) FROM test.users");
			assertThat(rs.one().getLong(0)).isEqualTo(1);
		}
	}

	@Test
	public void shouldNotHaveUser() {
		try (Session session = this.cluster.connect()) {
			ResultSet rs = session.execute("SELECT COUNT(*) FROM test.users");
			assertThat(rs.one().getLong(0)).isZero();
		}
	}

	@Configuration
	static class Context {

		@Bean
		public Cluster customCluster() {
			return Cluster.builder().withPort(9000).addContactPoint("localhost").build();

		}

	}

	@Configuration
	static class Context2 {

		@Bean
		public Cluster customCluster() {
			return cassandra.getCluster();
		}

	}

}
