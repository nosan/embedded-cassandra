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
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedLocalCassandra}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@EmbeddedLocalCassandra(version = "2.2.12", scripts = "/init.cql", statements =
		"CREATE TABLE IF NOT EXISTS test.roles (   id text PRIMARY" +
				"  KEY );", replace = EmbeddedCassandra.Replace.ANY)
public class EmbeddedLocalCassandraTests {

	@ClassRule
	public static final CaptureOutput OUTPUT = new CaptureOutput();

	@Autowired
	private TestCassandra cassandra;

	@Autowired
	private RemoteArtifactFactory remoteArtifactFactory;

	@Autowired
	private LocalCassandraFactory cassandraFactory;

	@Autowired
	private ClusterFactory clusterFactory;

	@Autowired
	private Cluster cluster;

	@Test
	public void shouldOverrideVersion() {
		assertThat(OUTPUT.toString()).contains("Cassandra version: 2.2.12");
		assertThat(this.cluster.getClusterName()).isEqualTo("My cluster");
		assertThat(this.cluster.connect().execute("SELECT * FROM  test.roles").wasApplied()).isTrue();
		assertThat(this.cassandraFactory.getArtifactFactory()).isSameAs(this.remoteArtifactFactory);
		assertThat(ReflectionUtils.getField(this.cassandra, "cassandraFactory")).isSameAs(this.cassandraFactory);
		assertThat(ReflectionUtils.getField(this.cassandra, "clusterFactory")).isSameAs(this.clusterFactory);
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
