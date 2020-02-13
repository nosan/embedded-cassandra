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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

		//configure sys props
		Map<String, String> sysProps = new LinkedHashMap<>();
		this.systemProperties.forEach((name, value) -> sysProps.put(name, Objects.toString(getValue(value), null)));
		configureSystemProperties(sysProps);

		//configure config props
		Resource oldConfigFile = getConfigFile(workingDirectory, sysProps.get("cassandra.config"));
		Map<String, Object> oldConfProps = getConfigProperties(oldConfigFile);
		Map<String, Object> newConfProps = new LinkedHashMap<>(oldConfProps);
		for (Map.Entry<String, Object> entry : this.configProperties.entrySet()) {
			setProperty(entry.getKey(), null, entry.getKey(), entry.getValue(), newConfProps);
		}
		configureConfigProperties(newConfProps);
		if (version.getMajor() >= 4) {
			configureSeeds(oldConfProps, newConfProps, sysProps);
		}

		//create new config file
		Path newConfigFile = Files.createTempFile(workingDirectory.resolve("conf"), "",
				"-" + oldConfigFile.getFileName().orElse("cassandra.yaml"));
		try (OutputStream os = Files.newOutputStream(newConfigFile)) {
			Yaml.write(newConfProps, os);
		}
		sysProps.put("cassandra.config", newConfigFile.toUri().toString());

		//prepare jvm extra opts
		List<String> jvmExtraOpts = new ArrayList<>(this.jvmOptions);
		sysProps.forEach((name, value) -> {
			if (value == null) {
				jvmExtraOpts.add("-D" + name);
			}
			else {
				jvmExtraOpts.add("-D" + name + "=" + value);
			}
		});

		//configure envs
		Map<String, String> envVars = new LinkedHashMap<>();
		this.environmentVariables.forEach((name, value) -> envVars.put(name, Objects.toString(getValue(value), "")));
		envVars.merge("JVM_EXTRA_OPTS", String.join(" ", jvmExtraOpts), (s1, s2) -> s1 + " " + s2);
		//set java home
		if (!envVars.containsKey("JAVA_HOME")) {
			Optional.ofNullable(System.getProperty("java.home"))
					.filter(StringUtils::hasText).ifPresent(path -> envVars.put("JAVA_HOME", path));
		}
		//create pid file
		Path pidFile = Files.createTempFile(workingDirectory.resolve("bin"), "", "-cassandra.pid");

		//create database
		if (isWindows()) {
			return new WindowsCassandraDatabase(this.name, version, workingDirectory, envVars, newConfProps, sysProps,
					this.jvmOptions, pidFile);
		}
		return new UnixCassandraDatabase(this.name, version, workingDirectory, envVars, newConfProps, sysProps,
				this.jvmOptions);
	}

	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.toLowerCase(Locale.ENGLISH).startsWith("windows");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getConfigProperties(Resource file) throws IOException {
		try (InputStream inputStream = file.getInputStream()) {
			Map<String, Object> configProperties = Yaml.read(inputStream, Map.class);
			return (configProperties != null) ? new LinkedHashMap<>(configProperties) : new LinkedHashMap<>(0);
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

	@SuppressWarnings("unchecked")
	private static void configureSeeds(Map<String, Object> oldConfProps, Map<String, Object> newConfProps,
			Map<String, String> sysProps) {
		String oldStoragePort = Objects.toString(oldConfProps.get("storage_port"), "7000");
		String newStoragePort = Optional.ofNullable(sysProps.get("cassandra.storage_port"))
				.orElseGet(() -> Objects.toString(newConfProps.get("storage_port"), oldStoragePort));
		String oldSslStoragePort = Objects.toString(oldConfProps.get("ssl_storage_port"), "7001");
		String newSslStoragePort = Optional.ofNullable(sysProps.get("cassandra.ssl_storage_port"))
				.orElseGet(() -> Objects.toString(newConfProps.get("ssl_storage_port"), oldSslStoragePort));
		if (oldStoragePort.equals(newStoragePort) && oldSslStoragePort.equals(newSslStoragePort)) {
			return;
		}
		List<Map<String, Object>> seedProvider = (List<Map<String, Object>>) newConfProps.get("seed_provider");
		if (seedProvider != null) {
			seedProvider.stream().map(each -> (List<Map<String, Object>>) each.get("parameters"))
					.filter(Objects::nonNull)
					.forEach(parameters -> parameters.stream()
							.filter(parameter -> Objects.nonNull(parameter.get("seeds")))
							.forEach(parameter -> {
								String seeds = String.valueOf(parameter.get("seeds"));
								if (!oldStoragePort.equals(newStoragePort)) {
									seeds = seeds.replaceAll(":" + oldStoragePort, ":" + newStoragePort);
								}
								if (!oldSslStoragePort.equals(newSslStoragePort)) {
									seeds = seeds.replaceAll(":" + oldSslStoragePort, ":" + newSslStoragePort);
								}
								parameter.put("seeds", seeds);
							}));
		}
	}

	private static void setPort(String name, Map<String, ? super String> target) throws IOException {
		if (Objects.toString(target.get(name), "").equals("0")) {
			try (ServerSocket ss = new ServerSocket(0)) {
				target.put(name, Integer.toString(ss.getLocalPort()));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void setProperty(String fullName, String parentName, String name, Object value,
			Map<String, Object> target) {
		int index = name.indexOf('.');
		if (index != -1) {
			String rootName = name.substring(0, index);
			Object rootValue = Optional.ofNullable(target.get(rootName)).orElseGet(LinkedHashMap::new);
			if (rootValue instanceof Map) {
				rootValue = new LinkedHashMap<>(((Map<?, ?>) rootValue));
				target.put(rootName, rootValue);
				setProperty(fullName, rootName, name.substring(index + 1), value, ((Map<String, Object>) rootValue));
			}
			else {
				throw new IllegalArgumentException(String.format("Config property: '%s: %s'"
								+ " cannot be set. Property: '%s.%s' has a type: '%s'"
								+ " and it cannot have nested properties.", fullName, value, parentName, rootName,
						rootValue.getClass().getCanonicalName()));
			}
		}
		else {
			target.put(name, getValue(value));
		}
	}

	private static Object getValue(Object value) {
		if (value instanceof Map) {
			Map<String, Object> map = new LinkedHashMap<>();
			((Map<?, ?>) value).forEach((key, val) -> map.put(Objects.toString(key), getValue(val)));
			return map;
		}
		if (value instanceof Collection) {
			List<Object> list = new ArrayList<>();
			((Collection<?>) value).forEach(each -> list.add(getValue(each)));
			return list;
		}
		if (value instanceof Resource) {
			try {
				return Paths.get(((Resource) value).toURI()).toString();
			}
			catch (Exception ex) {
				//ignore
			}
			try {
				return ((Resource) value).toURI().toString();
			}
			catch (IOException ex) {
				//ignore
			}
			try {
				return ((Resource) value).toURL().toString();
			}
			catch (IOException ex) {
				//ignore
			}
		}
		if (value instanceof Path) {
			return value.toString();
		}
		if (value instanceof File) {
			return value.toString();
		}
		return value;
	}

}
