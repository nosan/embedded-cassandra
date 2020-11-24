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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
		this.environmentVariables = Collections.unmodifiableMap(environmentVariables);
		this.configProperties = Collections.unmodifiableMap(configProperties);
		this.systemProperties = Collections.unmodifiableMap(systemProperties);
		this.jvmOptions = Collections.unmodifiableSet(jvmOptions);
	}

	@Override
	public CassandraDatabase create(Path workingDirectory) throws Exception {
		Version version = this.version;
		Resource configFile = getConfigFile(workingDirectory, this.systemProperties.get("cassandra.config"));
		//configure system props
		Map<String, String> systemProperties = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : this.systemProperties.entrySet()) {
			systemProperties.put(entry.getKey(), Objects.toString(getValue(entry.getValue()), ""));
		}
		configureSystemProperties(systemProperties);
		//configure config properties
		Map<String, Object> configProperties = loadProperties(configFile);
		setProperties(null, this.configProperties, configProperties);
		configureConfigProperties(configProperties);
		//Since Cassandra 4.X.X has a format IP:PORT, 0 port must be replaced with the storage_port.
		if (version.getMajor() >= 4) {
			configureSeeds(configProperties, systemProperties);
		}
		//create new config file
		Path newConfigFile = Files.createTempFile(workingDirectory.resolve("conf"), "",
				"-" + configFile.getFileName().orElse("cassandra.yaml")).toAbsolutePath();
		writeProperties(configProperties, newConfigFile);
		systemProperties.put("cassandra.config", newConfigFile.toUri().toString());
		//prepare jvm extra opts
		List<String> jvmExtraOpts = new ArrayList<>(this.jvmOptions);
		systemProperties.forEach((name, value) -> {
			if (value.equals("")) {
				jvmExtraOpts.add("-D" + name);
			}
			else {
				jvmExtraOpts.add("-D" + name + "=" + value);
			}
		});
		//configure envs
		Map<String, String> environmentVariables = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : this.environmentVariables.entrySet()) {
			environmentVariables.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
		}
		environmentVariables.merge("JVM_EXTRA_OPTS", String.join(" ", jvmExtraOpts), (s1, s2) -> s1 + " " + s2);
		//set java home
		if (!environmentVariables.containsKey("JAVA_HOME")) {
			Optional.ofNullable(System.getProperty("java.home"))
					.filter(StringUtils::hasText).ifPresent(path -> environmentVariables.put("JAVA_HOME", path));
		}
		//avoid gc.log error
		Files.createDirectories(workingDirectory.resolve("logs"));
		//creates for data
		Files.createDirectories(workingDirectory.resolve("data"));
		//create database
		if (isWindows()) {
			Path pidFile = Files.createTempFile(workingDirectory.resolve("bin"), "", "-cassandra.pid");
			return new WindowsCassandraDatabase(this.name, version, newConfigFile, workingDirectory,
					environmentVariables, configProperties, systemProperties, this.jvmOptions, pidFile);
		}
		return new UnixCassandraDatabase(this.name, version, newConfigFile, workingDirectory, environmentVariables,
				configProperties, systemProperties, this.jvmOptions);
	}

	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.toLowerCase(Locale.ENGLISH).startsWith("windows");
	}

	private static void writeProperties(Map<String, Object> properties, Path configFile) throws IOException {
		try (Writer writer = Files.newBufferedWriter(configFile)) {
			DumperOptions dumperOptions = new DumperOptions();
			dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			new Yaml(dumperOptions).dump(properties, writer);
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

	private static Resource getConfigFile(Path workingDirectory, Object url) throws MalformedURLException {
		if (url instanceof Resource) {
			return ((Resource) url);
		}
		if (url instanceof Path) {
			return new FileSystemResource(((Path) url).toAbsolutePath());
		}
		if (url instanceof File) {
			return new FileSystemResource(((File) url));
		}
		if (url instanceof URL) {
			return new UrlResource(((URL) url));
		}
		if (url instanceof URI) {
			return new UrlResource(((URI) url).toURL());
		}
		if (url != null) {
			return new UrlResource(new URL(url.toString()));
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

	private static void setProperties(String parentName, Map<String, Object> source, Map<String, Object> target)
			throws IOException {
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			String fullName = (parentName != null) ? parentName + "." + entry.getKey() : entry.getKey();
			setProperty(fullName, parentName, entry.getKey(), getValue(entry.getValue()), target);
		}
	}

	@SuppressWarnings("unchecked")
	private static void setProperty(String fullName, String parentName, String name, Object value,
			Map<String, Object> target) throws IOException {
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
			setProperties(name, ((Map<String, Object>) value), rootValue);
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
					seeds.replaceAll(seed -> {
						int index = seed.indexOf(':');
						//IPV6, [IP]:port
						if (index != -1 && seed.indexOf(':', index + 1) != -1) {
							return seed.replaceAll("]:0\\b(\\s*)$", String.format("]:%s$1", storagePort));
						}
						//IPV4, IP:port
						if (index != -1) {
							return seed.replaceAll(":0\\b(\\s*)$", String.format(":%s$1", storagePort));
						}
						return seed;
					});
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
		return Optional.ofNullable(parameter)
				.map(p -> p.get("seeds"))
				.map(String::valueOf)
				.map(seeds -> Arrays.stream(seeds.split(",")).collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

	private static Object getValue(Object object) throws IOException {
		if (object instanceof Path) {
			return ((Path) object).normalize().toAbsolutePath().toString();
		}
		if (object instanceof File) {
			return ((File) object).toPath().normalize().toAbsolutePath().toString();
		}
		if (object instanceof URI) {
			return object.toString();
		}
		if (object instanceof URL) {
			return object.toString();
		}
		if (object instanceof Resource) {
			try {
				return getValue(Paths.get(((Resource) object).toURI()));
			}
			catch (Exception ex) {
				//ignore
			}
			try {
				return getValue(((Resource) object).toURI());
			}
			catch (IOException ex) {
				//ignore
			}
			return getValue(((Resource) object).toURL());
		}
		if (object instanceof InetAddress) {
			return ((InetAddress) object).getHostName();
		}
		if (object instanceof Map<?, ?>) {
			Map<Object, Object> result = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
				result.put(entry.getKey(), getValue(entry.getValue()));
			}
			return result;
		}
		if (object instanceof Collection<?>) {
			List<Object> result = new ArrayList<>();
			for (Object o : ((Collection<?>) object)) {
				result.add(getValue(o));
			}
			return result;
		}
		if (object instanceof Object[]) {
			List<Object> result = new ArrayList<>();
			for (Object o : ((Object[]) object)) {
				result.add(getValue(o));
			}
			return result;

		}
		return object;
	}

}
