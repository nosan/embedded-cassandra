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

import java.util.Objects;

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
import com.github.nosan.embedded.cassandra.lang.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.DefaultClusterFactory;
import com.github.nosan.embedded.cassandra.test.TestCassandra;
import com.github.nosan.embedded.cassandra.test.support.OutputRule;
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
	public static final OutputRule output = new OutputRule();

	@Autowired
	@Nullable
	private TestCassandra cassandra;

	@Autowired
	@Nullable
	private RemoteArtifactFactory remoteArtifactFactory;

	@Autowired
	@Nullable
	private LocalCassandraFactory cassandraFactory;

	@Autowired
	@Nullable
	private ClusterFactory clusterFactory;

	@Autowired
	@Nullable
	private Cluster cluster;

	@Test
	public void shouldOverrideVersion() {
		assertThat(output.toString()).contains("Cassandra version: 2.2.12");
		assertThat(Objects.requireNonNull(this.cluster).getClusterName()).isEqualTo("My cluster");
		assertThat(this.cluster.connect().execute("SELECT * FROM  test.roles").wasApplied()).isTrue();
		assertThat(Objects.requireNonNull(this.cassandraFactory).getArtifactFactory())
				.isSameAs(this.remoteArtifactFactory);
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

				@Override
				protected Cluster.Builder configure(Cluster.Builder builder, Settings settings) {
					return builder.withClusterName("My cluster");
				}

			};
		}

	}

}
