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
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.nosan.embedded.cassandra.JvmOptions;
import com.github.nosan.embedded.cassandra.junit.CassandraRule;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Cql}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CqlScriptPrimaryTests {


	@ClassRule
	public static CassandraRule cassandra = new CassandraRule(new ExecutableConfigBuilder()
			.jvmOptions(new JvmOptions("-Xmx256m", "-Xms256m"))
			.build());


	@Autowired
	private Cluster cluster;

	@Test
	@Cql(scripts = {"/keyspace.cql", "/users.cql", "/users-data.cql"})
	@Cql(statements = "DROP KEYSPACE test", executionPhase = Cql.ExecutionPhase.AFTER_TEST_METHOD)
	public void shouldHaveUser() {
		try (Session session = this.cluster.connect()) {
			ResultSet rs = session.execute("SELECT COUNT(*) FROM test.users");
			assertThat(rs.one().getLong(0)).isEqualTo(1);
		}
	}

	@Test
	@Cql(scripts = {"/keyspace.cql", "/users.cql"})
	public void shouldNotHaveUser() {
		try (Session session = this.cluster.connect()) {
			ResultSet rs = session.execute("SELECT COUNT(*) FROM test.users");
			assertThat(rs.one().getLong(0)).isZero();
		}
	}


	@Configuration
	static class Context {

		@Bean
		@Primary
		public Cluster cluster1() {
			return cassandra.getCluster();
		}

		@Bean
		public Cluster cluster2() {
			return Cluster.builder().withPort(9000).addContactPoint("localhost").build();
		}

	}

}
