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
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.Artifact;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandraFactory}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("unchecked")
public class LocalCassandraFactoryTests {

	@Test
	public void createConfigureLocalCassandra() throws Exception {

		Version version = new Version(3, 11, 5);
		Path workingDirectory = Paths.get(UUID.randomUUID().toString());
		Path javaDirectory = Paths.get(UUID.randomUUID().toString());
		Path logbackFile = Paths.get("logback.xml");
		Path configurationFile = Paths.get("cassandra.yaml");
		Path rackFile = Paths.get("rack.properties");
		Path topologyFile = Paths.get("topology.properties");
		int jmxPort = 8000;
		ArtifactFactory artifactFactory = new ArtifactFactory() {
			@Nonnull
			@Override
			public Artifact create(@Nonnull Version version) {
				return null;
			}
		};

		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setVersion(version);
		factory.setWorkingDirectory(workingDirectory);
		factory.setLogbackFile(logbackFile.toUri().toURL());
		factory.setConfigurationFile(configurationFile.toUri().toURL());
		factory.setArtifactFactory(artifactFactory);
		factory.setTopologyFile(topologyFile.toUri().toURL());
		factory.setRackFile(rackFile.toUri().toURL());
		factory.getJvmOptions().add("arg1");
		factory.setStartupTimeout(Duration.ofMinutes(1));
		factory.setJavaHome(javaDirectory);
		factory.setJmxPort(jmxPort);


		Cassandra cassandra = factory.create();
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(cassandra, "artifactFactory")).isEqualTo(artifactFactory);

		Object processFactory = ReflectionUtils.getField(cassandra, "processFactory");

		assertThat(ReflectionUtils.getField(processFactory, "jvmOptions")).isEqualTo(Collections.singletonList("arg1"));
		assertThat(ReflectionUtils.getField(processFactory, "startupTimeout")).isEqualTo(Duration.ofMinutes(1));
		assertThat(ReflectionUtils.getField(processFactory, "javaHome")).isEqualTo(javaDirectory);
		assertThat(ReflectionUtils.getField(processFactory, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(processFactory, "jmxPort")).isEqualTo(jmxPort);

		Object directoryFactory = ReflectionUtils.getField(cassandra, "directoryFactory");

		assertThat(ReflectionUtils.getField(directoryFactory, "logbackFile")).isEqualTo(logbackFile.toUri().toURL());
		assertThat(ReflectionUtils.getField(directoryFactory, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(directoryFactory, "directory")).isEqualTo(workingDirectory);
		assertThat(ReflectionUtils.getField(directoryFactory, "configurationFile"))
				.isEqualTo(configurationFile.toUri().toURL());
		assertThat(ReflectionUtils.getField(directoryFactory, "rackFile")).isEqualTo(rackFile.toUri().toURL());
		assertThat(ReflectionUtils.getField(directoryFactory, "topologyFile")).isEqualTo(topologyFile.toUri().toURL());

	}

	@Test
	public void createDefaultLocalCassandra() {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		Cassandra cassandra = factory.create();
		Object processFactory = ReflectionUtils.getField(cassandra, "processFactory");
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(new Version(3, 11, 3));
		assertThat(ReflectionUtils.getField(processFactory, "version")).isEqualTo(new Version(3, 11, 3));
		assertThat(ReflectionUtils.getField(processFactory, "startupTimeout")).isEqualTo(Duration.ofSeconds(30));
		assertThat(ReflectionUtils.getField(processFactory, "jmxPort")).isEqualTo(7199);
		assertThat(ReflectionUtils.getField(cassandra, "artifactFactory"))
				.isInstanceOf(RemoteArtifactFactory.class);

	}

}
