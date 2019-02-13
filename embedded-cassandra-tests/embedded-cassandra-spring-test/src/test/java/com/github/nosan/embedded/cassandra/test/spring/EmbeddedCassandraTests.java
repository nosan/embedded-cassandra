/*
 * Copyright 2018-2019 the original author or authors.
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

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.DefaultClusterFactory;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@EmbeddedCassandra(scripts = "/init.cql", statements = "CREATE TABLE IF NOT EXISTS test.roles (   id text PRIMARY" +
		"  KEY );", replace = EmbeddedCassandra.Replace.ANY)
public class EmbeddedCassandraTests {

	@ClassRule
	public static final CaptureOutput OUTPUT = new CaptureOutput();

	@Autowired
	private Cluster cluster;

	@Test
	public void shouldSelectFromRoles() {
		assertThat(OUTPUT.toString()).contains("Cassandra version: 2.2.12");
		assertThat(this.cluster.getClusterName()).isEqualTo("My cluster");
		try (Session session = this.cluster.connect()) {
			assertThat(session.execute("SELECT * FROM  test.roles").wasApplied())
					.isTrue();
		}
	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public LocalCassandraFactory localCassandraFactory() {
			LocalCassandraFactory factory = new LocalCassandraFactory();
			factory.setVersion(Version.parse("2.2.12"));
			return factory;
		}

		@Bean
		public ClusterFactory clusterFactory() {
			return new DefaultClusterFactory() {

				@Nonnull
				@Override
				protected Cluster.Builder configure(@Nonnull Cluster.Builder builder, @Nonnull Settings settings) {
					return builder.withClusterName("My cluster");
				}

			};
		}
	}
}
