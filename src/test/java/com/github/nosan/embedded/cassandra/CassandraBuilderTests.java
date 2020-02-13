/*
 * Copyright 2020 the original author or authors.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraBuilder}.
 *
 * @author Dmytro Nosan
 */
class CassandraBuilderTests {

	private final CassandraBuilder builder = new CassandraBuilder();

	@Test
	void name() {
		CassandraBuilder cassandra = this.builder.name("cassandra");
		assertThat(cassandra).hasFieldOrPropertyWithValue("name", "cassandra");
	}

	@Test
	void version() {
		Cassandra cassandra = this.builder.version("3.11.9").build();
		assertThat(cassandra).hasFieldOrPropertyWithValue("version", Version.parse("3.11.9"));
	}

	@Test
	void registerShutdownHook() {
		Cassandra cassandra = this.builder.registerShutdownHook(false).build();
		assertThat(cassandra).hasFieldOrPropertyWithValue("registerShutdownHook", false);
	}

	@Test
	void startupTimeout() {
		Cassandra cassandra = this.builder.startupTimeout(Duration.ofMinutes(1)).build();
		assertThat(cassandra).hasFieldOrPropertyWithValue("startupTimeout", Duration.ofMinutes(1));
	}

	@Test
	void workingDirectory(@TempDir Path workingDirectory) {
		Cassandra cassandra = this.builder.workingDirectory(() -> workingDirectory).build();
		assertThat(cassandra).hasFieldOrPropertyWithValue("workingDirectory", workingDirectory);
	}

	@Test
	void workingDirectoryInitializer() {
		WorkingDirectoryInitializer workingDirectoryInitializer = (workingDirectory, version) -> {
		};
		Cassandra cassandra = this.builder.workingDirectoryInitializer(workingDirectoryInitializer).build();
		assertThat(cassandra).hasFieldOrPropertyWithValue("workingDirectoryInitializer", workingDirectoryInitializer);
	}

	@Test
	void workingDirectoryDestroyer() {
		WorkingDirectoryDestroyer workingDirectoryDestroyer = (workingDirectory, version) -> {
		};
		Cassandra cassandra = this.builder.workingDirectoryDestroyer(workingDirectoryDestroyer).build();
		assertThat(cassandra).hasFieldOrPropertyWithValue("workingDirectoryDestroyer", workingDirectoryDestroyer);
	}

	@Test
	void workingDirectoryCustomizers() {
		WorkingDirectoryCustomizer w1 = WorkingDirectoryCustomizer
				.copy(new ClassPathResource("text.txt"), "conf/text.txt");
		WorkingDirectoryCustomizer w2 = WorkingDirectoryCustomizer
				.copy(new ClassPathResource("empty.txt"), "conf/empty.txt");
		assertThat(this.builder.workingDirectoryCustomizers(w1).build())
				.hasFieldOrPropertyWithValue("workingDirectoryCustomizers", Collections.singleton(w1));
		assertThat(this.builder.workingDirectoryCustomizers(w2).build())
				.hasFieldOrPropertyWithValue("workingDirectoryCustomizers", Collections.singleton(w2));
		assertThat(this.builder.addWorkingDirectoryCustomizers(w1).build())
				.hasFieldOrPropertyWithValue("workingDirectoryCustomizers", new LinkedHashSet<>(Arrays.asList(w2, w1)));

	}

	@Test
	void logger() {
		Logger logger = Logger.get("TEST");
		assertThat(this.builder.logger(logger).build()).hasFieldOrPropertyWithValue("logger", logger);
	}

	@Test
	void environmentVariables() {
		Path javaHome = Paths.get(System.getProperty("java.home"));
		assertThat(this.builder.addEnvironmentVariable("JAVA_HOME", javaHome).build())
				.hasFieldOrPropertyWithValue("databaseFactory.environmentVariables",
						mapOf("JAVA_HOME", javaHome));
		assertThat(this.builder.addEnvironmentVariables(mapOf("EXTRA_CLASSPATH", "lib.jar")).build())
				.hasFieldOrPropertyWithValue("databaseFactory.environmentVariables",
						mapOf("JAVA_HOME", javaHome, "EXTRA_CLASSPATH", "lib.jar"));
		assertThat(this.builder.environmentVariables(mapOf("EXTRA_CLASSPATH", "lib.jar")).build())
				.hasFieldOrPropertyWithValue("databaseFactory.environmentVariables",
						mapOf("EXTRA_CLASSPATH", "lib.jar"));
	}

	@Test
	void systemProperties() {
		ClassPathResource resource = new ClassPathResource("test.txt");
		assertThat(this.builder.addSystemProperty("cassandra.rpc_port", 9160).build())
				.hasFieldOrPropertyWithValue("databaseFactory.systemProperties", mapOf("cassandra.rpc_port", 9160));
		assertThat(this.builder.addSystemProperties(mapOf("cassandra.config", resource)).build())
				.hasFieldOrPropertyWithValue("databaseFactory.systemProperties",
						mapOf("cassandra.rpc_port", 9160, "cassandra.config", resource));
		assertThat(this.builder.systemProperties(mapOf("cassandra.config", resource))
				.build()).hasFieldOrPropertyWithValue("databaseFactory.systemProperties",
				mapOf("cassandra.config", resource));
	}

	@Test
	void jvmOptions() {
		assertThat(this.builder.jvmOptions("-Xmx512m").build())
				.hasFieldOrPropertyWithValue("databaseFactory.jvmOptions",
						Collections.singleton("-Xmx512m"));

		assertThat(this.builder.addJvmOptions("-Xmx1024m").build())
				.hasFieldOrPropertyWithValue("databaseFactory.jvmOptions",
						new LinkedHashSet<>(Arrays.asList("-Xmx512m", "-Xmx1024m")));

		assertThat(this.builder.jvmOptions("-Xmx1024m").build())
				.hasFieldOrPropertyWithValue("databaseFactory.jvmOptions",
						Collections.singleton("-Xmx1024m"));
	}

	@Test
	void configProperties() {
		assertThat(this.builder.addConfigProperty("rpc_port", 9160).build())
				.hasFieldOrPropertyWithValue("databaseFactory.configProperties", mapOf("rpc_port", 9160));
		assertThat(this.builder
				.addConfigProperties(mapOf("start_rpc", true)).build())
				.hasFieldOrPropertyWithValue("databaseFactory.configProperties",
						mapOf("rpc_port", 9160, "start_rpc", true));
		assertThat(this.builder.configProperties(mapOf("start_rpc", true)).build())
				.hasFieldOrPropertyWithValue("databaseFactory.configProperties", mapOf("start_rpc", true));
	}

	@Test
	void configure() {
		this.builder.configure(builder -> builder.name("super"));
		assertThat(this.builder.build()).hasFieldOrPropertyWithValue("name", "super");
	}

	private static <K, V> Map<K, V> mapOf(K k1, V v1) {
		Map<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		return map;
	}

	private static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
		Map<K, V> map = new LinkedHashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		return map;
	}

}
