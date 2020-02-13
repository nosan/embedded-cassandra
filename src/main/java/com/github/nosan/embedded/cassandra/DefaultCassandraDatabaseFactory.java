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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.commons.FileSystemResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.commons.StringUtils;
import com.github.nosan.embedded.cassandra.commons.UrlResource;

class DefaultCassandraDatabaseFactory implements CassandraDatabaseFactory {

	private final String name;

	private final Version version;

	private final Map<String, Object> environmentVariables;

	private final Map<String, Object> configProperties;

	private final Map<String, Object> systemProperties;

	private final Set<String> jvmOptions;

	DefaultCassandraDatabaseFactory(String name, Version version, Map<String, Object> environmentVariables,
			Map<String, Object> configProperties, Map<String, Object> systemProperties, Set<String> jvmOptions) {
		this.name = name;
		this.version = version;
		this.environmentVariables = environmentVariables;
		this.configProperties = configProperties;
		this.systemProperties = systemProperties;
		this.jvmOptions = jvmOptions;
	}

	@Override
	public CassandraDatabase create(Path workingDirectory) throws Exception {
		Version version = this.version;
		//configure system props
		Map<String, String> systemProperties = new LinkedHashMap<>();
		this.systemProperties.forEach((name, value) -> systemProperties.put(name, Objects.toString(value, null)));
		configureSystemProperties(systemProperties);
		//configure config properties
		Resource configFile = getConfigFile(workingDirectory, systemProperties.get("cassandra.config"));
		Map<String, Object> configProperties = loadProperties(configFile);
		setProperties(this.configProperties, null, configProperties);
		configureConfigProperties(configProperties);
		//since Cassandra 4.0 seed has a format IP:port
		if (version.getMajor() >= 4) {
			configureSeeds(configProperties, systemProperties);
		}
		//create new config file
		Path newConfigFile = Files.createTempFile(workingDirectory.resolve("conf"), "",
				"-" + configFile.getFileName().orElse("cassandra.yaml"));
		writeProperties(configProperties, newConfigFile);
		systemProperties.put("cassandra.config", newConfigFile.toUri().toString());

		//prepare jvm extra opts
		List<String> jvmExtraOpts = new ArrayList<>(this.jvmOptions);
		systemProperties.forEach((name, value) -> {
			if (value == null) {
				jvmExtraOpts.add("-D" + name.trim());
			}
			else {
				jvmExtraOpts.add("-D" + name.trim() + "=" + value.trim());
			}
		});

		//configure envs
		Map<String, String> environmentVariables = new LinkedHashMap<>();
		this.environmentVariables.forEach((name, value) -> environmentVariables.put(name,
				Objects.toString(value, "").trim()));
		environmentVariables.merge("JVM_EXTRA_OPTS", String.join(" ", jvmExtraOpts), (s1, s2) -> s1 + " " + s2);
		//set java home
		if (!environmentVariables.containsKey("JAVA_HOME")) {
			Optional.ofNullable(System.getProperty("java.home"))
					.filter(StringUtils::hasText).ifPresent(path -> environmentVariables.put("JAVA_HOME", path));
		}
		//create database
		if (isWindows()) {
			Path pidFile = Files.createTempFile(workingDirectory.resolve("bin"), "", "-cassandra.pid");
			return new WindowsCassandraDatabase(this.name, version, workingDirectory, environmentVariables,
					configProperties, systemProperties,
					this.jvmOptions, pidFile);
		}
		return new UnixCassandraDatabase(this.name, version, workingDirectory, environmentVariables,
				configProperties, systemProperties,
				this.jvmOptions);
	}

	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.toLowerCase(Locale.ENGLISH).startsWith("windows");
	}

	private static void writeProperties(Map<String, Object> properties, Path configFile) throws IOException {
		try (Writer writer = Files.newBufferedWriter(configFile)) {
			DumperOptions dumperOptions = new DumperOptions();
			dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			new org.yaml.snakeyaml.Yaml(dumperOptions).dump(properties, writer);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> loadProperties(Resource file) throws IOException {
		try (InputStream is = file.getInputStream()) {
			Map<String, Object> properties = new Yaml().loadAs(is, Map.class);
			if (properties == null) {
				return new LinkedHashMap<>(0);
			}
			return properties;
		}
	}

	private static Resource getConfigFile(Path workingDirectory, String url) {
		if (url != null) {
			try {
				return new UrlResource(new URL(url));
			}
			catch (MalformedURLException ex) {
				return new FileSystemResource(Paths.get(url));
			}
		}
		return new FileSystemResource(workingDirectory.resolve("conf/cassandra.yaml"));
	}

	private static void configureSystemProperties(Map<String, String> systemProperties) throws IOException {
		setPort("cassandra.native_transport_port", systemProperties);
		setPort("cassandra.storage_port", systemProperties);
		setPort("cassandra.ssl_storage_port", systemProperties);
		setPort("cassandra.rpc_port", systemProperties);
		setPort("cassandra.jmx.remote.port", systemProperties);
		setPort("cassandra.jmx.local.port", systemProperties);
		setPort("com.sun.management.jmxremote.rmi.port", systemProperties);
	}

	private static void configureConfigProperties(Map<String, Object> configProperties) throws IOException {
		setPort("native_transport_port", configProperties);
		setPort("storage_port", configProperties);
		setPort("ssl_storage_port", configProperties);
		setPort("rpc_port", configProperties);
		setPort("native_transport_port_ssl", configProperties);
	}

	private static void setPort(String name, Map<String, ? super String> target) throws IOException {
		if (Objects.toString(target.get(name), "").equals("0")) {
			try (ServerSocket ss = new ServerSocket(0)) {
				target.put(name, Integer.toString(ss.getLocalPort()));
			}
		}
	}

	private static void setProperties(Map<String, Object> source, String parentName, Map<String, Object> target) {
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			setProperty(entry.getKey(), parentName, entry.getKey(), entry.getValue(), target);
		}
	}

	@SuppressWarnings("unchecked")
	private static void setProperty(String fullName, String parentName, String name, Object value,
			Map<String, Object> target) {
		int index = name.indexOf('.');
		if (index != -1) {
			String rootName = name.substring(0, index);
			if (target.get(rootName) instanceof Map) {
				Map<String, Object> rootValue = (Map<String, Object>) target.get(rootName);
				target.put(rootName, rootValue);
				setProperty(fullName, rootName, name.substring(index + 1), value, rootValue);
			}
			else if (target.get(rootName) == null) {
				Map<String, Object> rootValueMap = new LinkedHashMap<>();
				target.put(rootName, rootValueMap);
				setProperty(fullName, rootName, name.substring(index + 1), value, rootValueMap);
			}
			else {
				throw new IllegalArgumentException(String.format("Config property: '%s: %s'"
								+ " cannot be set. Property: '%s.%s' has a type: '%s'"
								+ " and it cannot have nested properties.", fullName, value, parentName, rootName,
						target.get(rootName).getClass().getCanonicalName()));
			}
			return;
		}
		if (target.get(name) instanceof Map && value instanceof Map) {
			Map<String, Object> rootValue = (Map<String, Object>) target.get(name);
			target.put(name, rootValue);
			setProperties(((Map<String, Object>) value), name, rootValue);
		}
		else {
			target.put(name, value);
		}
	}

	private static void configureSeeds(Map<String, Object> configProperties, Map<String, String> systemProperties) {
		String storagePort = Optional.ofNullable(systemProperties.get("cassandra.storage_port"))
				.orElseGet(() -> Objects.toString(configProperties.get("storage_port"), "7000"));
		for (Map<String, Object> seedProvider : getSeedProvider(configProperties)) {
			for (Map<String, Object> parameter : getParameters(seedProvider)) {
				List<String> seeds = getSeeds(parameter);
				if (!seeds.isEmpty()) {
					seeds.replaceAll(s -> s.replaceAll("\\b:0\\b$", String.format(":%s", storagePort)));
					parameter.put("seeds", String.join(",", seeds));
				}
			}
		}
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
		return Optional.ofNullable(parameter.get("seeds"))
				.map(String::valueOf)
				.map(seeds -> Arrays.stream(seeds.split(",")).filter(StringUtils::hasText).map(String::trim)
						.collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

}
