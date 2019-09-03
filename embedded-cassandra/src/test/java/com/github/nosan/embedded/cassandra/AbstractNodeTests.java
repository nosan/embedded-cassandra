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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.RunProcess;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractNode}.
 *
 * @author Dmytro Nosan
 */
class AbstractNodeTests {

	private final Map<String, Object> properties = new LinkedHashMap<>();

	private final List<String> jvmOptions = new ArrayList<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	@Nullable
	private Path workDir;

	@BeforeEach
	void initWorkDir(@TempDir Path workDir) throws Exception {
		Files.createDirectories(workDir.resolve("conf"));
		try (InputStream inputStream = getClass().getResource("/cassandra.yaml").openStream()) {
			Files.copy(inputStream, workDir.resolve("conf/cassandra.yaml"));
		}
		this.workDir = workDir;
	}

	@Test
	@SuppressWarnings("unchecked")
	void doStartWithConfigFile() throws Exception {
		new AssertNode(this.workDir, new ClassPathResource("cassandra-random.yaml"), this.properties, this.jvmOptions,
				this.systemProperties, this.environmentVariables, process -> {
			Map<String, String> systemProperties = getJvmExtraOptions(process);
			Yaml yaml = new Yaml();
			try (InputStream inputStream = new URL(systemProperties.get("-Dcassandra.config")).openStream()) {
				Map<String, Object> properties = yaml.loadAs(inputStream, Map.class);
				assertThat(properties).doesNotContainEntry("native_transport_port_ssl", 0);
				assertThat(properties).doesNotContainEntry("rpc_port", 0);
				assertThat(properties).doesNotContainEntry("storage_port", 0);
				assertThat(properties).doesNotContainEntry("ssl_storage_port", 0);
				assertThat(properties).doesNotContainEntry("native_transport_port", 0);
			}
		}).start();
	}

	@Test
	@SuppressWarnings("unchecked")
	void doStartWithConfigFileViaProperties() throws Exception {
		this.systemProperties.put("cassandra.config", new ClassPathResource("cassandra-random.yaml").toURL());
		new AssertNode(this.workDir, null, this.properties, this.jvmOptions, this.systemProperties,
				this.environmentVariables, process -> {
			Map<String, String> systemProperties = getJvmExtraOptions(process);
			Yaml yaml = new Yaml();
			try (InputStream inputStream = new URL(systemProperties.get("-Dcassandra.config")).openStream()) {
				Map<String, Object> properties = yaml.loadAs(inputStream, Map.class);
				assertThat(properties).doesNotContainEntry("native_transport_port_ssl", 0);
				assertThat(properties).doesNotContainEntry("rpc_port", 0);
				assertThat(properties).doesNotContainEntry("storage_port", 0);
				assertThat(properties).doesNotContainEntry("ssl_storage_port", 0);
				assertThat(properties).doesNotContainEntry("native_transport_port", 0);
			}
		}).start();
	}

	@Test
	@SuppressWarnings("unchecked")
	void doStartWithRandomPorts() throws Exception {
		this.systemProperties.put("cassandra.native_transport_port", 0);
		this.systemProperties.put("cassandra.rpc_port", 0);
		this.systemProperties.put("cassandra.storage_port", 0);
		this.systemProperties.put("cassandra.ssl_storage_port", 0);
		this.properties.put("native_transport_port_ssl", 0);
		this.systemProperties.put("cassandra.jmx.local.port", 0);
		this.systemProperties.put("cassandra.jmx.remote.port", 0);

		new AssertNode(this.workDir, null, this.properties, this.jvmOptions, this.systemProperties,
				this.environmentVariables, process -> {
			Map<String, String> systemProperties = getJvmExtraOptions(process);
			assertThat(systemProperties).doesNotContainEntry("-Dcassandra.native_transport_port", "0");
			assertThat(systemProperties).doesNotContainEntry("-Dcassandra.rpc_port", "0");
			assertThat(systemProperties).doesNotContainEntry("-Dcassandra.storage_port", "0");
			assertThat(systemProperties).doesNotContainEntry("-Dcassandra.ssl_storage_port", "0");
			assertThat(systemProperties).doesNotContainEntry("-Dcassandra.jmx.local.port", "0");
			assertThat(systemProperties).doesNotContainEntry("-Dcassandra.jmx.remote.port", "0");
			Yaml yaml = new Yaml();
			try (InputStream inputStream = new URL(systemProperties.get("-Dcassandra.config")).openStream()) {
				Map<String, Object> properties = yaml.loadAs(inputStream, Map.class);
				assertThat(properties).doesNotContainEntry("native_transport_port_ssl", 0);
			}
		}).start();
	}

	@Test
	void doStartWithJvmOptions() throws Exception {
		this.jvmOptions.add("-Xmx512m");
		new AssertNode(this.workDir, null, this.properties, this.jvmOptions, this.systemProperties,
				this.environmentVariables, process -> assertThat(process.getEnvironment().get("JVM_EXTRA_OPTS"))
				.asString().contains("-Xmx512m")).start();
	}

	@Test
	void doStartWithEnvVariables() throws Exception {
		this.environmentVariables.put("KEY", "VALUE");
		new AssertNode(this.workDir, null, this.properties, this.jvmOptions, this.systemProperties,
				this.environmentVariables,
				process -> assertThat(process.getEnvironment()).containsEntry("KEY", "VALUE")).start();
	}

	private Map<String, String> getJvmExtraOptions(RunProcess process) {
		String opts = (String) process.getEnvironment().get("JVM_EXTRA_OPTS");
		assertThat(opts).isNotNull();
		String[] tokens = opts.split(" ");
		Map<String, String> systemProperties = new LinkedHashMap<>();
		for (String token : tokens) {
			systemProperties.put(token.split("=")[0], token.split("=")[1]);
		}
		return systemProperties;
	}

	private interface RunProcessConsumer {

		void accept(RunProcess process) throws IOException;

	}

	private static final class AssertNode extends AbstractNode {

		private final RunProcessConsumer consumer;

		AssertNode(@Nullable Path workingDirectory, @Nullable Resource config, Map<String, Object> properties,
				List<String> jvmOptions, Map<String, Object> systemProperties, Map<String, Object> environmentVariables,
				RunProcessConsumer consumer) {
			super(Objects.requireNonNull(workingDirectory), config, properties, jvmOptions, systemProperties,
					environmentVariables);
			this.consumer = consumer;
		}

		@Override
		protected NodeProcess doStart(RunProcess runProcess) throws IOException {
			this.consumer.accept(runProcess);
			return null;
		}

	}

}
