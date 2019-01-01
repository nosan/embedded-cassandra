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
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.DefaultClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@EmbeddedLocalCassandra(version = "2.2.12", scripts = "/init.cql", statements =
		"CREATE TABLE IF NOT EXISTS test.roles (   id text PRIMARY" +
				"  KEY );")
public class EmbeddedLocalCassandraTests {

	@ClassRule
	public static final CaptureOutput OUTPUT = new CaptureOutput();

	@Autowired
	private TestCassandra cassandra;

	@Autowired
	private RemoteArtifactFactory remoteArtifactFactory;

	@Autowired
	private LocalCassandraFactory localCassandraFactory;

	@Autowired
	private Cluster cluster;

	@Test
	public void shouldOverrideVersion() {
		assertThat(OUTPUT.toString()).contains("Cassandra version: 2.2.12");
		assertThat(this.cluster.getClusterName()).isEqualTo("My cluster");
		try (Session session = this.cluster.connect()) {
			assertThat(session.execute("SELECT * FROM  test.roles").wasApplied())
					.isTrue();
		}
		assertThat(this.localCassandraFactory.getArtifactFactory()).isSameAs(this.remoteArtifactFactory);
	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public RemoteArtifactFactory remoteArtifactFactory() {
			return new RemoteArtifactFactory();
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
