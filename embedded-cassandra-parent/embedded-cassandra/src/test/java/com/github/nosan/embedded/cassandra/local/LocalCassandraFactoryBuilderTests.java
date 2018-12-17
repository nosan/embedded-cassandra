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
		Path commitLogArchiving = Paths.get("commitLog");
		Path workingDirectory = Paths.get(UUID.randomUUID().toString());
		Path javaDirectory = Paths.get(UUID.randomUUID().toString());
		Version version = new Version(3, 11, 0);
		int jmxPort = 8000;
		boolean allowRoot = true;
		boolean registerShutdownHook = false;

		LocalCassandraFactory factory = new LocalCassandraFactoryBuilder()
				.setJvmOptions(jvmOptions)
				.addJvmOptions("3")
				.setArtifactFactory(artifactFactory)
				.setConfigurationFile(config)
				.setLogbackFile(logback)
				.setCommitLogArchivingFile(commitLogArchiving)
				.setVersion(version)
				.setRackFile(rack)
				.setJavaHome(javaDirectory)
				.setTopologyFile(topology)
				.setWorkingDirectory(workingDirectory)
				.setStartupTimeout(Duration.ofMinutes(1))
				.setJmxPort(jmxPort)
				.setAllowRoot(allowRoot)
				.setRegisterShutdownHook(registerShutdownHook)
				.build();

		assertThat(factory.getJvmOptions()).containsExactly("1", "2", "3");
		assertThat(factory.getArtifactFactory()).isEqualTo(artifactFactory);
		assertThat(factory.getConfigurationFile()).isEqualTo(config.toUri().toURL());
		assertThat(factory.getLogbackFile()).isEqualTo(logback.toUri().toURL());
		assertThat(factory.getCommitLogArchivingFile()).isEqualTo(commitLogArchiving.toUri().toURL());
		assertThat(factory.getVersion()).isEqualTo(version);
		assertThat(factory.getRackFile()).isEqualTo(rack.toUri().toURL());
		assertThat(factory.getTopologyFile()).isEqualTo(topology.toUri().toURL());
		assertThat(factory.getWorkingDirectory()).isEqualTo(workingDirectory);
		assertThat(factory.getJavaHome()).isEqualTo(javaDirectory);
		assertThat(factory.getJmxPort()).isEqualTo(jmxPort);
		assertThat(factory.isAllowRoot()).isEqualTo(allowRoot);
		assertThat(factory.isRegisterShutdownHook()).isEqualTo(registerShutdownHook);
		assertThat(factory.getStartupTimeout()).isEqualTo(Duration.ofMinutes(1));
	}

	@Test
	public void defaultBuild() {
		LocalCassandraFactory factory = new LocalCassandraFactoryBuilder()
				.build();

		assertThat(factory.getJvmOptions()).isEmpty();
		assertThat(factory.getArtifactFactory()).isNull();
		assertThat(factory.getConfigurationFile()).isNull();
		assertThat(factory.getLogbackFile()).isNull();
		assertThat(factory.getVersion()).isNull();
		assertThat(factory.getRackFile()).isNull();
		assertThat(factory.getCommitLogArchivingFile()).isNull();
		assertThat(factory.getTopologyFile()).isNull();
		assertThat(factory.getWorkingDirectory()).isNull();
		assertThat(factory.getJavaHome()).isNull();
		assertThat(factory.getJmxPort()).isEqualTo(7199);
		assertThat(factory.isAllowRoot()).isFalse();
		assertThat(factory.isRegisterShutdownHook()).isTrue();
		assertThat(factory.getStartupTimeout()).isNull();

	}
}
