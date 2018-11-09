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
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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


		Cassandra cassandra = factory.create();
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(factory.getVersion());
		assertThat(ReflectionUtils.getField(cassandra, "artifactFactory"))
				.isEqualTo(artifactFactory);
		assertThat(ReflectionUtils.getField(ReflectionUtils.getField(cassandra, "directory"),
				"rootDirectory")).isEqualTo(workingDirectory);
		assertThat(ReflectionUtils.getField(ReflectionUtils.getField(cassandra, "localProcess"), "jvmOptions"))
				.isEqualTo(factory.getJvmOptions());
		assertThat(ReflectionUtils.getField(ReflectionUtils.getField(cassandra, "localProcess"), "startupTimeout"))
				.isEqualTo(factory.getStartupTimeout());
		assertThat(ReflectionUtils.getField(ReflectionUtils.getField(cassandra, "localProcess"), "javaHome"))
				.isEqualTo(factory.getJavaHome());
		assertThat(ReflectionUtils.getField(ReflectionUtils.getField(cassandra, "localProcess"), "version"))
				.isEqualTo(factory.getVersion());
		List<DirectoryInitializer> initializers = (List<DirectoryInitializer>) ReflectionUtils.getField(cassandra,
				"initializers");
		assertThat(initializers).hasSize(5);
		assertThat(initializers).matches(new Matcher("logbackFile", logbackFile.toUri().toURL(),
				LogbackFileInitializer.class));
		assertThat(initializers).matches(new Matcher("configurationFile", configurationFile.toUri().toURL(),
				ConfigurationFileInitializer.class));
		assertThat(initializers).matches(new Matcher("rackFile", rackFile.toUri().toURL(),
				RackFileInitializer.class));
		assertThat(initializers).matches(new Matcher("topologyFile", topologyFile.toUri().toURL(),
				TopologyFileInitializer.class));
		assertThat(initializers).last().isInstanceOfAny(PortReplacerInitializer.class);

	}

	@Test
	public void createDefaultLocalCassandra() {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		Cassandra cassandra = factory.create();
		assertThat(ReflectionUtils.getField(cassandra, "version")).isEqualTo(new Version(3, 11, 3));
		assertThat(ReflectionUtils.getField(cassandra, "artifactFactory"))
				.isInstanceOf(RemoteArtifactFactory.class);

	}

	private static final class Matcher implements Predicate<List<? extends DirectoryInitializer>> {

		@Nonnull
		private final String name;

		@Nullable
		private final Object value;

		@Nonnull
		private final Class<? extends DirectoryInitializer> initializer;

		private Matcher(@Nonnull String name, @Nullable Object value,
				@Nonnull Class<? extends DirectoryInitializer> initializer) {
			this.name = name;
			this.value = value;
			this.initializer = initializer;
		}


		@Override
		public boolean test(List<? extends DirectoryInitializer> directoryInitializers) {
			for (DirectoryInitializer directoryInitializer : directoryInitializers) {
				if (directoryInitializer.getClass().isAssignableFrom(this.initializer)) {
					assertThat(ReflectionUtils.getField(directoryInitializer, this.name)).isEqualTo(this.value);
					return true;
				}
			}
			return false;
		}
	}

}
