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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractNode}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("NullableProblems")
class AbstractNodeTests {

	private final Map<String, Object> properties = new LinkedHashMap<>();

	private final List<String> jvmOptions = new ArrayList<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private Path workingDirectory;

	@BeforeEach
	void initWorkingDirectory(@TempDir Path workDir) throws Exception {
		Files.createDirectories(workDir.resolve("conf"));
		try (InputStream is = new ClassPathResource("cassandra.yaml").getInputStream()) {
			Files.copy(is, workDir.resolve("conf/cassandra.yaml"));
		}
		this.workingDirectory = workDir;
	}

	@Test
	@SuppressWarnings("unchecked")
	void doStartWithConfigFileViaProperties() throws Exception {
		this.systemProperties.put("cassandra.config", new ClassPathResource("cassandra-random.yaml").toURL());
		run(process -> {
			Map<String, String> systemProperties = getSystemProperties(process);
			Yaml yaml = new Yaml();
			try (InputStream inputStream = new URL(systemProperties.get("cassandra.config")).openStream()) {
				Map<String, Object> properties = yaml.loadAs(inputStream, Map.class);
				assertThat(properties).doesNotContainEntry("native_transport_port_ssl", 0);
				assertThat(properties).doesNotContainEntry("rpc_port", 0);
				assertThat(properties).doesNotContainEntry("storage_port", 0);
				assertThat(properties).doesNotContainEntry("ssl_storage_port", 0);
				assertThat(properties).doesNotContainEntry("native_transport_port", 0);
			}
		});
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
		run(process -> {
			Map<String, String> systemProperties = getSystemProperties(process);
			assertThat(systemProperties).doesNotContainEntry("cassandra.native_transport_port", "0");
			assertThat(systemProperties).doesNotContainEntry("cassandra.rpc_port", "0");
			assertThat(systemProperties).doesNotContainEntry("cassandra.storage_port", "0");
			assertThat(systemProperties).doesNotContainEntry("cassandra.ssl_storage_port", "0");
			assertThat(systemProperties).doesNotContainEntry("cassandra.jmx.local.port", "0");
			assertThat(systemProperties).doesNotContainEntry("cassandra.jmx.remote.port", "0");
			Yaml yaml = new Yaml();
			try (InputStream is = new URL(systemProperties.get("cassandra.config")).openStream()) {
				Map<String, Object> properties = yaml.loadAs(is, Map.class);
				assertThat(properties).doesNotContainEntry("native_transport_port_ssl", 0);
			}
		});
	}

	@Test
	void doStartWithJvmOptions() throws Exception {
		this.jvmOptions.add("-Xmx512m");
		run(process -> assertThat(getJvmOptions(process)).containsExactly("-Xmx512m"));
	}

	@Test
	void doStartWithEnvVariables() throws Exception {
		this.environmentVariables.put("KEY", "VALUE");
		run(process -> assertThat(process.getEnvironment()).containsEntry("KEY", "VALUE"));
	}

	private void run(RunProcessConsumer consumer) throws IOException, InterruptedException {
		new AbstractNode(this.workingDirectory, this.properties, this.jvmOptions, this.systemProperties,
				this.environmentVariables) {

			@Nullable
			@Override
			protected NodeProcess doStart(RunProcess runProcess) throws IOException {
				consumer.accept(runProcess);
				return null;
			}

		}.start();
	}

	private static List<String> getJvmOptions(RunProcess process) {
		List<String> jvmOptions = new ArrayList<>();
		String[] tokens = process.getEnvironment().get("JVM_EXTRA_OPTS").toString().split(" ");
		for (String token : tokens) {
			if (!token.contains("-D")) {
				jvmOptions.add(token);
			}
		}
		return jvmOptions;
	}

	private static Map<String, String> getSystemProperties(RunProcess process) {
		Map<String, String> systemProperties = new LinkedHashMap<>();
		String[] tokens = process.getEnvironment().get("JVM_EXTRA_OPTS").toString().split(" ");
		for (String token : tokens) {
			if (token.contains("-D")) {
				systemProperties.put(token.split("=")[0].substring("-D".length()), token.split("=")[1]);
			}
		}
		return systemProperties;
	}

	private interface RunProcessConsumer {

		void accept(RunProcess process) throws IOException;

	}

}
