/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.artifact.ArtifactFactory;
import com.github.nosan.embedded.cassandra.local.artifact.RemoteArtifactFactory;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandraFactory}.
 *
 * @author Dmytro Nosan
 */
class LocalCassandraFactoryTests {

	@Test
	void createConfigureLocalCassandra() throws Exception {

		Version version = new Version(3, 11, 5);
		Path workingDirectory = Paths.get(UUID.randomUUID().toString());
		Path artifactDirectory = Paths.get(UUID.randomUUID().toString());
		Path javaDirectory = Paths.get(UUID.randomUUID().toString());
		URL logbackFile = Paths.get("logback.xml").toUri().toURL();
		URL configurationFile = Paths.get("cassandra.yaml").toUri().toURL();
		URL rackFile = Paths.get("rack.properties").toUri().toURL();
		URL topologyFile = Paths.get("topology.properties").toUri().toURL();
		URL commitLogArchivingFile = Paths.get("commit_log_archiving.properties").toUri().toURL();
		int jmxPort = 8000;
		ArtifactFactory artifactFactory = v -> {
			throw new UnsupportedOperationException();
		};
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setVersion(version);
		factory.setWorkingDirectory(workingDirectory);
		factory.setLogbackFile(logbackFile);
		factory.setConfigurationFile(configurationFile);
		factory.setArtifactFactory(artifactFactory);
		factory.setTopologyFile(topologyFile);
		factory.setRackFile(rackFile);
		factory.getJvmOptions().add("arg1");
		factory.setStartupTimeout(Duration.ofSeconds(30));
		factory.setJavaHome(javaDirectory);
		factory.setJmxPort(jmxPort);
		factory.setAllowRoot(true);
		factory.setRegisterShutdownHook(false);
		factory.setCommitLogArchivingFile(commitLogArchivingFile);
		factory.setArtifactDirectory(artifactDirectory);
		factory.setDeleteWorkingDirectory(true);

		Cassandra cassandra = factory.create();
		assertThat(ReflectionUtils.getField(cassandra, "registerShutdownHook")).isEqualTo(false);
		assertThat(ReflectionUtils.getField(cassandra, "jvmOptions")).isEqualTo(Collections.singletonList("arg1"));
		assertThat(ReflectionUtils.getField(cassandra, "startupTimeout")).isEqualTo(Duration.ofSeconds(30));
		assertThat(ReflectionUtils.getField(cassandra, "javaHome")).isEqualTo(javaDirectory);
		assertThat(ReflectionUtils.getField(cassandra, "jmxPort")).isEqualTo(jmxPort);
		assertThat(ReflectionUtils.getField(cassandra, "allowRoot")).isEqualTo(true);
		assertThat(ReflectionUtils.getField(cassandra, "workingDirectory")).isEqualTo(workingDirectory);
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(cassandra, "logbackFile")).isEqualTo(logbackFile);
		assertThat(ReflectionUtils.getField(cassandra, "artifactFactory")).isEqualTo(artifactFactory);
		assertThat(ReflectionUtils.getField(cassandra, "artifactDirectory")).isEqualTo(artifactDirectory);
		assertThat(ReflectionUtils.getField(cassandra, "configurationFile")).isEqualTo(configurationFile);
		assertThat(ReflectionUtils.getField(cassandra, "rackFile")).isEqualTo(rackFile);
		assertThat(ReflectionUtils.getField(cassandra, "topologyFile")).isEqualTo(topologyFile);
		assertThat(ReflectionUtils.getField(cassandra, "commitLogArchivingFile")).isEqualTo(commitLogArchivingFile);
		assertThat(ReflectionUtils.getField(cassandra, "deleteWorkingDirectory")).isEqualTo(true);
	}

	@Test
	void createDefaultLocalCassandra() {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		Cassandra cassandra = factory.create();

		Version version = new Version(3, 11, 4);
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(cassandra, "registerShutdownHook")).isEqualTo(true);
		assertThat(ReflectionUtils.getField(cassandra, "workingDirectory").toString()).
				startsWith(FileUtils.getTmpDirectory().resolve("embedded-cassandra/3.11.4").toString());
		assertThat(ReflectionUtils.getField(cassandra, "jvmOptions")).isEqualTo(Collections.emptyList());
		assertThat(ReflectionUtils.getField(cassandra, "startupTimeout")).isEqualTo(Duration.ofMinutes(1));
		assertThat(ReflectionUtils.getField(cassandra, "javaHome")).isNull();
		assertThat(ReflectionUtils.getField(cassandra, "jmxPort")).isEqualTo(7199);
		assertThat(ReflectionUtils.getField(cassandra, "allowRoot")).isEqualTo(false);
		assertThat(ReflectionUtils.getField(cassandra, "logbackFile")).isNull();
		assertThat(ReflectionUtils.getField(cassandra, "artifactFactory")).isInstanceOf(RemoteArtifactFactory.class);
		assertThat(ReflectionUtils.getField(cassandra, "artifactDirectory")).
				isEqualTo(FileUtils.getTmpDirectory().resolve("embedded-cassandra/3.11.4/apache-cassandra-3.11.4"));
		assertThat(ReflectionUtils.getField(cassandra, "configurationFile")).isNull();
		assertThat(ReflectionUtils.getField(cassandra, "rackFile")).isNull();
		assertThat(ReflectionUtils.getField(cassandra, "topologyFile")).isNull();
		assertThat(ReflectionUtils.getField(cassandra, "commitLogArchivingFile")).isNull();
		assertThat(ReflectionUtils.getField(cassandra, "deleteWorkingDirectory")).isEqualTo(false);
	}

}
