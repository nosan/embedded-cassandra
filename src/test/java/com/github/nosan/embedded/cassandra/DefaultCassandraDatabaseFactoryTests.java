/*
 * Copyright 2020-2021 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.commons.UrlResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultCassandraDatabaseFactory}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("unchecked")
class DefaultCassandraDatabaseFactoryTests {

	private final Map<String, Object> environmentVariables = new LinkedHashMap<>();

	private final Map<String, Object> configProperties = new LinkedHashMap<>();

	private final Map<String, Object> systemProperties = new LinkedHashMap<>();

	private final Set<String> jvmOptions = new LinkedHashSet<>();

	@BeforeEach
	void prepareWorkingDirectory(@TempDir Path workingDirectory) throws IOException {
		Files.createDirectories(workingDirectory.resolve("bin"));
		Files.createDirectories(workingDirectory.resolve("conf"));
		try (InputStream is = new ClassPathResource("cassandra-4.0.6.yaml").getInputStream()) {
			Files.copy(is, workingDirectory.resolve("conf/cassandra.yaml"));
		}
	}

	@Test
	void configureJavaHomeEnvVariable(@TempDir Path workingDirectory) throws Exception {
		this.environmentVariables.put("JAVA_HOME", "/home");

		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		assertThat(environmentVariables.get("JAVA_HOME")).isEqualTo("/home");
	}

	@Test
	void configureEnvVariables(@TempDir Path workingDirectory) throws Exception {
		this.jvmOptions.add("-Xmx512m");
		this.systemProperties.put("namevalue", "value");
		this.environmentVariables.put("JVM_EXTRA_OPTS", "-Xms1024m");

		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		assertThat(environmentVariables.get("JAVA_HOME")).isEqualTo(System.getProperty("java.home"));
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS")).isEqualTo("-Xms1024m -Xmx512m -Dnamevalue=value"
				+ " -Dcassandra.config=%s", database.getConfigurationFile().toUri());
	}

	@Test
	void configSystemProperties(@TempDir Path workingDirectory) throws Exception {
		this.systemProperties.put("nameonly", null);
		this.systemProperties.put("namevalue", "value");

		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> environmentVariables = database.getEnvironmentVariables();

		assertThat(database.getJvmOptions()).containsExactlyElementsOf(this.jvmOptions);
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS")).isEqualTo("-Dnameonly -Dnamevalue=value"
				+ " -Dcassandra.config=%s", database.getConfigurationFile().toUri());

	}

	@Test
	void configJvmOptions(@TempDir Path workingDirectory) throws Exception {
		this.jvmOptions.add("-Xmx512m");

		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> environmentVariables = database.getEnvironmentVariables();

		assertThat(database.getJvmOptions()).containsExactlyElementsOf(this.jvmOptions);
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS")).isEqualTo("-Xmx512m"
				+ " -Dcassandra.config=%s", database.getConfigurationFile().toUri());

	}

	@Test
	void setConfigFileResource(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.systemProperties.put("cassandra.config", resource);
		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		try (InputStream inputStream = resource.getInputStream()) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		try (InputStream inputStream = Files.newInputStream(database.getConfigurationFile())) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		assertThat(systemProperties.get("cassandra.config"))
				.isEqualTo(database.getConfigurationFile().toUri().toString());
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS"))
				.contains("-Dcassandra.config=" + database.getConfigurationFile().toUri());

	}

	@Test
	void setConfigFileUri(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.systemProperties.put("cassandra.config", resource.toURI());
		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		try (InputStream inputStream = resource.getInputStream()) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		try (InputStream inputStream = Files.newInputStream(database.getConfigurationFile())) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		assertThat(systemProperties.get("cassandra.config"))
				.isEqualTo(database.getConfigurationFile().toUri().toString());
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS"))
				.contains("-Dcassandra.config=" + database.getConfigurationFile().toUri());
	}

	@Test
	void setConfigFileUrl(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.systemProperties.put("cassandra.config", resource.toURL());
		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		try (InputStream inputStream = resource.getInputStream()) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		try (InputStream inputStream = Files.newInputStream(database.getConfigurationFile())) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		assertThat(systemProperties.get("cassandra.config"))
				.isEqualTo(database.getConfigurationFile().toUri().toString());
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS"))
				.contains("-Dcassandra.config=" + database.getConfigurationFile().toUri());
	}

	@Test
	void setConfigFileString(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.systemProperties.put("cassandra.config", resource.toURL().toString());
		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		try (InputStream inputStream = resource.getInputStream()) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		try (InputStream inputStream = Files.newInputStream(database.getConfigurationFile())) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		assertThat(systemProperties.get("cassandra.config"))
				.isEqualTo(database.getConfigurationFile().toUri().toString());
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS"))
				.contains("-Dcassandra.config=" + database.getConfigurationFile().toUri());
	}

	@Test
	void setConfigFilePath(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.systemProperties.put("cassandra.config", Paths.get(resource.toURI()));
		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		try (InputStream inputStream = resource.getInputStream()) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		try (InputStream inputStream = Files.newInputStream(database.getConfigurationFile())) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		assertThat(systemProperties.get("cassandra.config"))
				.isEqualTo(database.getConfigurationFile().toUri().toString());
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS"))
				.contains("-Dcassandra.config=" + database.getConfigurationFile().toUri());
	}

	@Test
	void setConfigFileFile(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.systemProperties.put("cassandra.config", new File(resource.toURI()));
		CassandraDatabase database = create(Version.parse("4.0.1"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		Map<String, String> environmentVariables = database.getEnvironmentVariables();
		try (InputStream inputStream = resource.getInputStream()) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		try (InputStream inputStream = Files.newInputStream(database.getConfigurationFile())) {
			assertThat(new Yaml().loadAs(inputStream, Map.class)).containsAllEntriesOf(database.getConfigProperties());
		}
		assertThat(systemProperties.get("cassandra.config"))
				.isEqualTo(database.getConfigurationFile().toUri().toString());
		assertThat(environmentVariables.get("JVM_EXTRA_OPTS"))
				.contains("-Dcassandra.config=" + database.getConfigurationFile().toUri());
	}

	@Test
	void setSystemPropertiesReplaceRandomPorts(@TempDir Path workingDirectory) throws Exception {
		this.systemProperties.put("cassandra.native_transport_port", 0);
		this.systemProperties.put("cassandra.rpc_port", 0);
		this.systemProperties.put("cassandra.storage_port", 0);
		this.systemProperties.put("cassandra.ssl_storage_port", 0);
		this.systemProperties.put("cassandra.jmx.local.port", 0);
		this.systemProperties.put("cassandra.jmx.remote.port", 0);
		this.systemProperties.put("com.sun.management.jmxremote.rmi.port", 0);
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, String> systemProperties = database.getSystemProperties();
		assertThat(systemProperties.get("cassandra.native_transport_port")).isNotEqualTo("0");
		assertThat(systemProperties.get("cassandra.rpc_port")).isNotEqualTo("0");
		assertThat(systemProperties.get("cassandra.storage_port")).isNotEqualTo("0");
		assertThat(systemProperties.get("cassandra.ssl_storage_port")).isNotEqualTo("0");
		assertThat(systemProperties.get("cassandra.jmx.local.port")).isNotEqualTo("0");
		assertThat(systemProperties.get("cassandra.jmx.remote.port")).isNotEqualTo("0");
		assertThat(systemProperties.get("com.sun.management.jmxremote.rmi.port")).isNotEqualTo("0");
	}

	@Test
	void setConfigPropertiesReplaceRandomPorts(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("native_transport_port", 0);
		this.configProperties.put("rpc_port", 0);
		this.configProperties.put("storage_port", 0);
		this.configProperties.put("ssl_storage_port", 0);
		this.configProperties.put("native_transport_port_ssl", 0);
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("native_transport_port")).isNotEqualTo(0);
		assertThat(configProperties.get("rpc_port")).isNotEqualTo(0);
		assertThat(configProperties.get("storage_port")).isNotEqualTo(0);
		assertThat(configProperties.get("ssl_storage_port")).isNotEqualTo(0);
		assertThat(configProperties.get("native_transport_port_ssl")).isNotEqualTo(0);
	}

	@Test
	void configureSeedsConfigStoragePort(@TempDir Path workingDirectory) throws Exception {
		Map<String, Object> seeds = new LinkedHashMap<>();
		seeds.put("class_name", "org.apache.cassandra.locator.SimpleSeedProvider");
		seeds.put("parameters", Collections.singletonList(Collections.singletonMap("seeds",
				"localhost,127.0.0.1:8080,127.0.0.1:0,[::1]:8080,::1:0,::1,[::1]:0")));
		this.configProperties.put("storage_port", 7010);
		this.configProperties.put("seed_provider", Collections.singletonList(
				Collections.unmodifiableMap(seeds)));

		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);

		Map<String, Object> configProperties = database.getConfigProperties();
		List<Map<String, Object>> seedProvider = getSeedProvider(configProperties);
		assertThat(seedProvider).hasSize(1);
		List<Map<String, Object>> parameters = getParameters(seedProvider.get(0));
		assertThat(parameters).hasSize(1);
		List<String> actualSeeds = getSeeds(parameters.get(0));
		assertThat(actualSeeds)
				.containsExactly("localhost", "127.0.0.1:8080", "127.0.0.1:7010", "[::1]:8080", "::1:0", "::1",
						"[::1]:7010");
	}

	@Test
	void configureSeedsSystemStoragePort(@TempDir Path workingDirectory) throws Exception {
		Map<String, Object> seeds = new LinkedHashMap<>();
		seeds.put("class_name", "org.apache.cassandra.locator.SimpleSeedProvider");
		seeds.put("parameters", Collections.singletonList(Collections.singletonMap("seeds",
				"localhost,127.0.0.1:8080,127.0.0.1:0,[::1]:8080,::1:0,::1,[::1]:0")));
		this.systemProperties.put("cassandra.storage_port", 7009);
		this.configProperties.put("seed_provider", Collections.singletonList(
				Collections.unmodifiableMap(seeds)));

		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);

		Map<String, Object> configProperties = database.getConfigProperties();
		List<Map<String, Object>> seedProvider = getSeedProvider(configProperties);
		assertThat(seedProvider).hasSize(1);
		List<Map<String, Object>> parameters = getParameters(seedProvider.get(0));
		assertThat(parameters).hasSize(1);
		List<String> actualSeeds = getSeeds(parameters.get(0));
		assertThat(actualSeeds)
				.containsExactly("localhost", "127.0.0.1:8080", "127.0.0.1:7009", "[::1]:8080", "::1:0", "::1",
						"[::1]:7009");
	}

	@Test
	void configureSeeds(@TempDir Path workingDirectory) throws Exception {
		Map<String, Object> seeds = new LinkedHashMap<>();
		seeds.put("class_name", "org.apache.cassandra.locator.SimpleSeedProvider");
		seeds.put("parameters", Collections.singletonList(Collections.singletonMap("seeds",
				"localhost,127.0.0.1:8080,127.0.0.1:0,[::1]:8080,::1:0,::1,[::1]:0")));
		this.configProperties.put("seed_provider", Collections.singletonList(
				Collections.unmodifiableMap(seeds)));

		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);

		Map<String, Object> configProperties = database.getConfigProperties();
		List<Map<String, Object>> seedProvider = getSeedProvider(configProperties);
		assertThat(seedProvider).hasSize(1);
		List<Map<String, Object>> parameters = getParameters(seedProvider.get(0));
		assertThat(parameters).hasSize(1);
		List<String> actualSeeds = getSeeds(parameters.get(0));
		assertThat(actualSeeds)
				.containsExactly("localhost", "127.0.0.1:8080", "127.0.0.1:7000", "[::1]:8080", "::1:0", "::1",
						"[::1]:7000");
	}

	@Test
	void setConfigProperties(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("native_transport_port", 9142);
		this.configProperties.put("client_encryption_options.enabled", true);
		this.configProperties.put("server_encryption_options", Collections.singletonMap("internode_encryption", "all"));
		this.configProperties.put("test.enabled", "A");
		this.configProperties.put("test\\.enabled", "B");
		this.configProperties.put("test\\\\.enabled", "C");
		this.configProperties.put("test\\\\\\.enabled", "D");
		this.configProperties.put("test\\\\\\\\.enabled", "F");
		this.configProperties.put("test\\\\\\\\\\.enabled", "E");
		this.configProperties.put("test.nested.enabled", "AA");
		this.configProperties.put("test\\.nested.enabled", "BB");
		this.configProperties.put("test\\.nested\\.enabled", "CC");

		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("native_transport_port")).isEqualTo(9142);
		assertThat(((Map<String, Object>) configProperties.get("client_encryption_options")))
				.contains(entry("enabled", true));
		assertThat(((Map<String, Object>) configProperties.get("server_encryption_options")))
				.contains(entry("internode_encryption", "all"));
		assertThat(((Map<String, Object>) configProperties.get("test")))
				.contains(entry("enabled", "A"));
		assertThat((configProperties.get("test.enabled"))).isEqualTo("B");
		assertThat(((Map<String, Object>) configProperties.get("test\\")))
				.contains(entry("enabled", "C"));
		assertThat((configProperties.get("test\\.enabled"))).isEqualTo("D");
		assertThat(((Map<String, Object>) configProperties.get("test\\\\")))
				.contains(entry("enabled", "F"));
		assertThat((configProperties.get("test\\\\.enabled"))).isEqualTo("E");
		assertThat(((Map<String, Object>) configProperties.get("test")))
				.contains(entry("nested", Collections.singletonMap("enabled", "AA")));
		assertThat(((Map<String, Object>) configProperties.get("test.nested")))
				.contains(entry("enabled", "BB"));
		assertThat(configProperties.get("test.nested.enabled")).isEqualTo("CC");
	}

	@Test
	void setConfigPropertiesEmptyName(@TempDir Path workingDirectory) {
		this.configProperties.put("", true);
		assertThatThrownBy(() -> create(Version.parse("4.0"), workingDirectory))
				.hasStackTraceContaining("Config property must not be empty");
	}

	@Test
	void setConfigPropertiesSubPropertyEmpty(@TempDir Path workingDirectory) {
		this.configProperties.put("a.", true);
		assertThatThrownBy(() -> create(Version.parse("4.0"), workingDirectory))
				.hasStackTraceContaining("Config property must not be empty");
	}

	@Test
	void setConfigPropertiesSubPropertyEmpty1(@TempDir Path workingDirectory) {
		this.configProperties.put(".", true);
		assertThatThrownBy(() -> create(Version.parse("4.0"), workingDirectory))
				.hasStackTraceContaining("Config property: '.' is invalid");
	}

	@Test
	void setConfigPropertiesCannotHaveNestedProperties(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("client_encryption_options.enabled.nested", true);
		assertThatThrownBy(() -> create(Version.parse("4.0"), workingDirectory))
				.hasStackTraceContaining("and it cannot have nested properties");
	}

	@Test
	void setConfigPropertyPath(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("test", workingDirectory);
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(workingDirectory.toString());
	}

	@Test
	void setConfigPropertyFile(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("test", workingDirectory.toFile());
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(workingDirectory.toFile().toString());
	}

	@Test
	void setConfigPropertyUri(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("test", workingDirectory.toUri());
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(workingDirectory.toUri().toString());
	}

	@Test
	void setConfigPropertyUrl(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("test", workingDirectory.toUri().toURL());
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(workingDirectory.toUri().toURL().toString());
	}

	@Test
	void setConfigPropertyClassPathResource(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.configProperties.put("test", resource);
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test"))
				.isEqualTo(Paths.get(resource.toURI()).toString());
	}

	@Test
	void setConfigPropertyArrayClassPathResource(@TempDir Path workingDirectory) throws Exception {
		ClassPathResource resource = new ClassPathResource("cassandra-4.0.6.yaml");
		this.configProperties.put("test", new ClassPathResource[]{resource});
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(((Collection<Object>) configProperties.get("test")))
				.containsExactly(Paths.get(resource.toURI()).toString());
	}

	@Test
	void setConfigPropertyUrlResource(@TempDir Path workingDirectory) throws Exception {
		URL url = new URL("http://localhost:8080/cassandra.yaml");
		Resource resource = mock(Resource.class);
		when(resource.toURI()).thenThrow(IOException.class);
		when(resource.toURL()).thenReturn(url);
		this.configProperties.put("test", resource);
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(url.toString());
	}

	@Test
	void setConfigPropertyUriResource(@TempDir Path workingDirectory) throws Exception {
		UrlResource resource = new UrlResource(new URL("http://localhost:8080/cassandra.yaml"));
		this.configProperties.put("test", resource);
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(resource.toURI().toString());
	}

	@Test
	void setConfigPropertyInetAddress(@TempDir Path workingDirectory) throws Exception {
		this.configProperties.put("test", InetAddress.getLoopbackAddress());
		CassandraDatabase database = create(Version.parse("4.0"), workingDirectory);
		Map<String, Object> configProperties = database.getConfigProperties();
		assertThat(configProperties.get("test")).isEqualTo(InetAddress.getLoopbackAddress().getHostName());
	}

	private CassandraDatabase create(Version version, Path workingDirectory) throws Exception {
		return new DefaultCassandraDatabaseFactory("test", version, this.environmentVariables,
				this.configProperties, this.systemProperties, this.jvmOptions).create(workingDirectory);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> getSeedProvider(Map<String, Object> configProperties) {
		List<Map<String, Object>> seedProvider = (List<Map<String, Object>>) configProperties.get("seed_provider");
		if (seedProvider == null) {
			return Collections.emptyList();
		}
		return seedProvider;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> getParameters(Map<String, Object> seedProvider) {
		List<Map<String, Object>> parameters = (List<Map<String, Object>>) seedProvider.get("parameters");
		if (parameters == null) {
			return Collections.emptyList();
		}
		return parameters;
	}

	private static List<String> getSeeds(Map<String, Object> parameter) {
		return Optional.ofNullable(parameter)
				.map(p -> p.get("seeds"))
				.map(String::valueOf)
				.map(seeds -> Arrays.stream(seeds.split(",")).collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

}
