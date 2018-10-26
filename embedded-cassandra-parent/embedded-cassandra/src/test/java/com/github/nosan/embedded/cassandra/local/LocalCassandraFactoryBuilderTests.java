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

package com.github.nosan.embedded.cassandra.local;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandraFactoryBuilder}.
 *
 * @author Dmytro Nosan
 */
public class LocalCassandraFactoryBuilderTests {


	@Test
	public void buildFactory() throws Exception {
		String[] jvmOptions = {"1", "2"};
		RemoteArtifactFactory artifactFactory = new RemoteArtifactFactory();
		Path config = Paths.get("config");
		Path logback = Paths.get("logback");
		Path rack = Paths.get("rack");
		Path topology = Paths.get("topology");
		Path workingDirectory = Paths.get(UUID.randomUUID().toString());
		Version version = new Version(3, 11, 0);

		LocalCassandraFactory factory = new LocalCassandraFactoryBuilder()
				.setJvmOptions(jvmOptions)
				.setArtifactFactory(artifactFactory)
				.setConfigurationFile(config)
				.setLogbackFile(logback)
				.setVersion(version)
				.setRackFile(rack)
				.setTopologyFile(topology)
				.setWorkingDirectory(workingDirectory)
				.setStartupTimeout(Duration.ofMinutes(1))
				.build();

		assertThat(factory.getJvmOptions()).containsExactly(jvmOptions);
		assertThat(factory.getArtifactFactory()).isEqualTo(artifactFactory);
		assertThat(factory.getConfigurationFile()).isEqualTo(config.toUri().toURL());
		assertThat(factory.getLogbackFile()).isEqualTo(logback.toUri().toURL());
		assertThat(factory.getVersion()).isEqualTo(version);
		assertThat(factory.getRackFile()).isEqualTo(rack.toUri().toURL());
		assertThat(factory.getTopologyFile()).isEqualTo(topology.toUri().toURL());
		assertThat(factory.getWorkingDirectory()).isEqualTo(workingDirectory);
		assertThat(factory.getStartupTimeout()).isEqualTo(Duration.ofMinutes(1));
	}
}
