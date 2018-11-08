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

import java.nio.file.Paths;
import java.time.Duration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandraContextCustomizer}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@LocalCassandra(version = "2.2.13", configurationFile = "classpath:/cassandra.yaml",
		logbackFile = "classpath:/logback-test.xml",
		rackFile = "classpath:/rack.properties",
		workingDirectory = "target/cassandra", jvmOptions = {"-Dtest.property=property"},
		topologyFile = "classpath:/topology.properties", startupTimeout = 0)
public class LocalCassandraAnnotationTests {

	@Autowired
	private LocalCassandraFactory factory;

	@Autowired
	private RemoteArtifactFactory artifactFactory;


	@Test
	public void shouldRegisterLocalFactoryBean() {
		assertThat(this.factory.getArtifactFactory()).isEqualTo(this.artifactFactory);
		assertThat(this.factory.getVersion()).isEqualTo(Version.parse("2.2.13"));
		assertThat(this.factory.getWorkingDirectory()).isEqualTo(Paths.get("target/cassandra"));
		assertThat(this.factory.getStartupTimeout()).isEqualTo(Duration.ofMillis(0));
		assertThat(this.factory.getLogbackFile()).isEqualTo(ClassLoader.getSystemResource("logback-test.xml"));
		assertThat(this.factory.getTopologyFile()).isEqualTo(ClassLoader.getSystemResource("topology.properties"));
		assertThat(this.factory.getRackFile()).isEqualTo(ClassLoader.getSystemResource("rack.properties"));
		assertThat(this.factory.getConfigurationFile()).isEqualTo(ClassLoader.getSystemResource("cassandra.yaml"));
		assertThat(this.factory.getJvmOptions()).containsExactly("-Dtest.property=property");

	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public RemoteArtifactFactory remoteArtifactFactory() {
			return new RemoteArtifactFactory();
		}
	}


}
