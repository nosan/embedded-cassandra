/*
 * Copyright 2018-2020 the original author or authors.
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.artifact.DefaultDistribution;
import com.github.nosan.embedded.cassandra.commons.io.FileSystemResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraBuilder}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("all")
class EmbeddedCassandraBuilderTests {

	private final EmbeddedCassandraBuilder builder = new EmbeddedCassandraBuilder();

	@Test
	void testName() {
		this.builder.withName("myname");
		Cassandra cassandra = this.builder.create();
		assertThat(cassandra.getName()).isEqualTo("myname");
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("name", "myname");
	}

	@Test
	void testWorkingDir(@TempDir Path temporaryFolder) {
		this.builder.withWorkingDirectory(temporaryFolder);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("workingDirectory", temporaryFolder);
	}

	@Test
	void testArtifact(@TempDir Path temporaryFolder) {
		final Version version = Version.of("3.11.6");
		Artifact artifact = () -> new DefaultDistribution(version, temporaryFolder);
		this.builder.withArtifact(artifact);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(cassandra).hasFieldOrPropertyWithValue("version", version);
		assertThat(database).hasFieldOrPropertyWithValue("version", version).hasFieldOrPropertyWithValue("directory",
				temporaryFolder);
	}

	@Test
	void testJavaHome(@TempDir Path temporaryFolder) {
		this.builder.withJavaHome(temporaryFolder);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("environmentVariables",
				Collections.singletonMap("JAVA_HOME", temporaryFolder));

	}

	@Test
	void testLogger() {
		Logger mylogger = LoggerFactory.getLogger("mylogger");
		this.builder.withLogger(mylogger);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("logger", mylogger);
	}

	@Test
	void testDaemon() {
		this.builder.withDaemon(true);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("daemon", true);
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void testRootAllowed() {
		this.builder.withRootAllowed(true);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("rootAllowed", true);
	}

	@Test
	void getJvmOptions() {
		Cassandra cassandra = this.builder.withJvmOptions("-Xmx512m").create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("jvmOptions", Collections.singletonList("-Xmx512m"));
	}

	@Test
	void getSystemProperties() {
		Cassandra cassandra = this.builder.withSystemProperty("key", "value").create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("systemProperties", Collections.singletonMap("key", "value"));
	}

	@Test
	void getEnvironmentVariables() {
		Cassandra cassandra = this.builder.withEnvironmentVariable("key", "value").create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		Map<String, Object> environmentVariables = (Map<String, Object>) ReflectionTestUtils.getField(node,
				"environmentVariables");
		assertThat(environmentVariables).containsEntry("key", "value");
	}

	@Test
	void getProperties() {
		Cassandra cassandra = this.builder.withConfigProperty("key", "value").create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("properties", Collections.singletonMap("key", "value"));
	}

	@Test
	void testTimeout() {
		this.builder.withTimeout(Duration.ofSeconds(60));
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("timeout", Duration.ofSeconds(60));
	}

	@Test
	void testPort() {
		this.builder.withPort(9042);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("systemProperties",
				Collections.singletonMap("cassandra.native_transport_port", 9042));
	}

	@Test
	void testSslPort() {
		this.builder.withSslPort(9042);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("properties",
				Collections.singletonMap("native_transport_port_ssl", 9042));
	}

	@Test
	void testRpcPort() {
		this.builder.withRpcPort(9160);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("systemProperties",
				Collections.singletonMap("cassandra.rpc_port", 9160));
	}

	@Test
	void testConfig(@TempDir Path temporaryFolder) throws IOException {
		Path file = Files.createTempFile(temporaryFolder, "", "");
		FileSystemResource config = new FileSystemResource(file);
		this.builder.withConfig(config);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("config", config);
	}

	@Test
	void testTopologyConfig(@TempDir Path temporaryFolder) throws IOException {
		Path file = Files.createTempFile(temporaryFolder, "", "");
		FileSystemResource config = new FileSystemResource(file);
		this.builder.withTopologyConfig(config);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("topologyConfig", config);
	}

	@Test
	void testRackConfig(@TempDir Path temporaryFolder) throws IOException {
		Path file = Files.createTempFile(temporaryFolder, "", "");
		FileSystemResource config = new FileSystemResource(file);
		this.builder.withRackConfig(config);
		Cassandra cassandra = this.builder.create();
		Object database = ReflectionTestUtils.getField(cassandra, "database");
		assertThat(database).hasFieldOrPropertyWithValue("rackConfig", config);
	}

	@Test
	void testStoragePort() {
		this.builder.withStoragePort(7000);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("systemProperties",
				Collections.singletonMap("cassandra.storage_port", 7000));
	}

	@Test
	void testStoragePortSsl() {
		this.builder.withSslStoragePort(7001);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("systemProperties",
				Collections.singletonMap("cassandra.ssl_storage_port", 7001));
	}

	@Test
	void testJmxLocal() {
		this.builder.withJmxLocalPort(7199);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("systemProperties",
				Collections.singletonMap("cassandra.jmx.local.port", 7199));
	}

	@Test
	void testAddress() throws UnknownHostException {
		InetAddress localhost = InetAddress.getByName("localhost");
		this.builder.withAddress(localhost);
		Cassandra cassandra = this.builder.create();
		Object node = ReflectionTestUtils.getField(ReflectionTestUtils.getField(cassandra, "database"), "node");
		assertThat(node).hasFieldOrPropertyWithValue("properties",
				Collections.singletonMap("rpc_address", localhost.getHostAddress()));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testShutdownHook() throws Exception {
		this.builder.withRegisterShutdownHook(true);
		this.builder.withName("myname");
		Cassandra cassandra = this.builder.create();
		Map<Thread, Thread> hooks = (Map<Thread, Thread>) ReflectionTestUtils.getField(
				Class.forName("java.lang.ApplicationShutdownHooks"), "hooks");
		assertThat(hooks.keySet()).anyMatch(thread -> thread.getName().equals("myname-sh"));
	}

}
