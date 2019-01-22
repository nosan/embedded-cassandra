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

import java.net.URL;
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
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandraFactory}.
 *
 * @author Dmytro Nosan
 */
public class LocalCassandraFactoryTests {

	@Test
	public void createConfigureLocalCassandra() throws Exception {

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
		boolean allowRoot = true;
		ArtifactFactory artifactFactory = new ArtifactFactory() {
			@Nonnull
			@Override
			public Artifact create(@Nonnull Version version) {
				throw new UnsupportedOperationException();
			}
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
		factory.setAllowRoot(allowRoot);
		factory.setRegisterShutdownHook(false);
		factory.setCommitLogArchivingFile(commitLogArchivingFile);
		factory.setArtifactDirectory(artifactDirectory);

		Cassandra cassandra = factory.create();
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(cassandra, "registerShutdownHook")).isEqualTo(false);

		Object process = ReflectionUtils.getField(cassandra, "process");
		assertThat(ReflectionUtils.getField(process, "workingDirectory")).isEqualTo(workingDirectory);
		assertThat(ReflectionUtils.getField(process, "jvmOptions")).isEqualTo(Collections.singletonList("arg1"));
		assertThat(ReflectionUtils.getField(process, "startupTimeout")).isEqualTo(Duration.ofSeconds(30));
		assertThat(ReflectionUtils.getField(process, "javaHome")).isEqualTo(javaDirectory);
		assertThat(ReflectionUtils.getField(process, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(process, "jmxPort")).isEqualTo(jmxPort);
		assertThat(ReflectionUtils.getField(process, "allowRoot")).isEqualTo(allowRoot);

		Object initializer = ReflectionUtils.getField(cassandra, "initializer");
		assertThat(ReflectionUtils.getField(initializer, "workingDirectory")).isEqualTo(workingDirectory);
		assertThat(ReflectionUtils.getField(initializer, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(initializer, "logbackFile")).isEqualTo(logbackFile);
		assertThat(ReflectionUtils.getField(initializer, "artifactFactory")).isEqualTo(artifactFactory);
		assertThat(ReflectionUtils.getField(initializer, "artifactDirectory")).isEqualTo(artifactDirectory);
		assertThat(ReflectionUtils.getField(initializer, "configurationFile")).isEqualTo(configurationFile);
		assertThat(ReflectionUtils.getField(initializer, "rackFile")).isEqualTo(rackFile);
		assertThat(ReflectionUtils.getField(initializer, "topologyFile")).isEqualTo(topologyFile);
		assertThat(ReflectionUtils.getField(initializer, "commitLogArchivingFile")).isEqualTo(commitLogArchivingFile);
	}

	@Test
	public void createDefaultLocalCassandra() {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		Cassandra cassandra = factory.create();

		Version version = new Version(3, 11, 3);
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(cassandra, "registerShutdownHook")).isEqualTo(true);

		Object process = ReflectionUtils.getField(cassandra, "process");
		assertThat(ReflectionUtils.getField(process, "workingDirectory").toString()).
				startsWith(FileUtils.getTmpDirectory().resolve("embedded-cassandra/3.11.3").toString());
		assertThat(ReflectionUtils.getField(process, "jvmOptions")).isEqualTo(Collections.emptyList());
		assertThat(ReflectionUtils.getField(process, "startupTimeout")).isEqualTo(Duration.ofMinutes(1));
		assertThat(ReflectionUtils.getField(process, "javaHome")).isNull();
		assertThat(ReflectionUtils.getField(process, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(process, "jmxPort")).isEqualTo(7199);
		assertThat(ReflectionUtils.getField(process, "allowRoot")).isEqualTo(false);

		Object initializer = ReflectionUtils.getField(cassandra, "initializer");
		assertThat(ReflectionUtils.getField(initializer, "workingDirectory").toString())
				.startsWith(FileUtils.getTmpDirectory().resolve("embedded-cassandra/3.11.3").toString());
		assertThat(ReflectionUtils.getField(initializer, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(initializer, "logbackFile")).isNull();
		assertThat(ReflectionUtils.getField(initializer, "artifactFactory")).isInstanceOf(RemoteArtifactFactory.class);
		assertThat(ReflectionUtils.getField(initializer, "artifactDirectory")).
				isEqualTo(FileUtils.getTmpDirectory().resolve("embedded-cassandra/3.11.3/apache-cassandra-3.11.3"));
		assertThat(ReflectionUtils.getField(initializer, "configurationFile")).isNull();
		assertThat(ReflectionUtils.getField(initializer, "rackFile")).isNull();
		assertThat(ReflectionUtils.getField(initializer, "topologyFile")).isNull();
		assertThat(ReflectionUtils.getField(initializer, "commitLogArchivingFile")).isNull();
	}
}
