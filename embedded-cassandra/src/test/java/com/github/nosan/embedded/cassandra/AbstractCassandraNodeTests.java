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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractCassandraNode}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("NullableProblems")
class AbstractCassandraNodeTests {

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
		start(process -> {
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
		start(process -> {
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
		start(process -> assertThat(getJvmOptions(process)).containsExactly("-Xmx512m"));
	}

	@Test
	void doStartWithEnvVariables() throws Exception {
		this.environmentVariables.put("KEY", "VALUE");
		start(process -> assertThat(process.getEnvironment()).containsEntry("KEY", "VALUE"));
	}

	private void start(RunProcessConsumer consumer) throws IOException, InterruptedException {
		MockProcess mockProcess = new MockProcess();
		AbstractCassandraNode node = new AbstractCassandraNode(this.workingDirectory, this.properties, this.jvmOptions,
				this.systemProperties,
				this.environmentVariables) {

			@Override
			protected Process doStart(RunProcess runProcess) throws IOException {
				consumer.accept(runProcess);
				return mockProcess;
			}

			@Override
			void doStop(Process process, long pid) throws IOException, InterruptedException {
				assertThat(process).isEqualTo(mockProcess);
				assertThat(pid).isEqualTo(mockProcess.pid);
			}

		};
		node.start();
		assertThat(node.getProcess()).isEqualTo(mockProcess);
		assertThat(node.isAlive()).isEqualTo(mockProcess.isAlive());
		assertThat(node.toString()).contains(":" + mockProcess.pid);
		node.stop();
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

	private static final class MockProcess extends Process {

		private static final ByteArrayOutputStream OUTPUTSTREAM = new ByteArrayOutputStream();

		private static final ByteArrayInputStream INPUTSTREAM = new ByteArrayInputStream(new byte[0]);

		private final long pid = 100;

		@Override
		public OutputStream getOutputStream() {
			return OUTPUTSTREAM;
		}

		@Override
		public InputStream getInputStream() {
			return INPUTSTREAM;
		}

		@Override
		public InputStream getErrorStream() {
			return INPUTSTREAM;
		}

		@Override
		public int waitFor() {
			return 0;
		}

		@Override
		public int exitValue() {
			return 0;
		}

		@Override
		public void destroy() {

		}

	}

}
