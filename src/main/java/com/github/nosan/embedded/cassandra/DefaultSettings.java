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

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DefaultSettings implements Settings {

	private final String name;

	private final Version version;

	private final InetAddress address;

	private final boolean nativeTransportEnabled;

	private final boolean rpcTransportEnabled;

	private final Integer port;

	private final Integer rpcPort;

	private final Integer sslPort;

	private final Path configurationFile;

	private final Path workingDirectory;

	private final Set<String> jvmOptions;

	private final Map<String, String> systemProperties;

	private final Map<String, String> environmentVariables;

	private final Map<String, Object> configProperties;

	DefaultSettings(String name, Version version, InetAddress address, boolean nativeTransportEnabled,
			Integer port, Integer sslPort, boolean rpcTransportEnabled, Integer rpcPort,
			Path configurationFile, Path workingDirectory, Set<String> jvmOptions,
			Map<String, String> systemProperties, Map<String, String> environmentVariables,
			Map<String, Object> configProperties) {
		this.name = name;
		this.version = version;
		this.nativeTransportEnabled = nativeTransportEnabled;
		this.rpcTransportEnabled = rpcTransportEnabled;
		this.rpcPort = rpcPort;
		this.configurationFile = configurationFile;
		this.environmentVariables = readOnly(environmentVariables);
		this.systemProperties = readOnly(systemProperties);
		this.jvmOptions = readOnly(jvmOptions);
		this.configProperties = readOnly(configProperties);
		this.address = address;
		this.port = port;
		this.sslPort = sslPort;
		this.workingDirectory = workingDirectory;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	@Override
	public boolean isNativeTransportEnabled() {
		return this.nativeTransportEnabled;
	}

	@Override
	public boolean isRpcTransportEnabled() {
		return this.rpcTransportEnabled;
	}

	@Override
	public InetAddress getAddress() {
		return this.address;
	}

	@Override
	public Integer getPort() {
		if (this.port == null) {
			return getSslPort();
		}
		return this.port;
	}

	@Override
	public Integer getRpcPort() {
		return this.rpcPort;
	}

	@Override
	public Path getConfigurationFile() {
		return this.configurationFile;
	}

	@Override
	public Integer getSslPort() {
		return this.sslPort;
	}

	@Override
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	@Override
	public Set<String> getJvmOptions() {
		return this.jvmOptions;
	}

	@Override
	public Map<String, String> getSystemProperties() {
		return this.systemProperties;
	}

	@Override
	public Map<String, String> getEnvironmentVariables() {
		return this.environmentVariables;
	}

	@Override
	public Map<String, Object> getConfigProperties() {
		return this.configProperties;
	}

	@SuppressWarnings("unchecked")
	private static <T> T readOnly(T object) {
		if (object instanceof Map<?, ?>) {
			Map<Object, Object> result = new LinkedHashMap<>();
			((Map<Object, Object>) object).forEach((name, value) -> result.put(name, readOnly(value)));
			return (T) Collections.unmodifiableMap(result);
		}
		if (object instanceof List<?>) {
			List<Object> result = new ArrayList<>();
			((List<?>) object).forEach(each -> result.add(readOnly(each)));
			return (T) Collections.unmodifiableList(result);
		}
		if (object instanceof Set<?>) {
			Set<Object> result = new LinkedHashSet<>();
			((Set<?>) object).forEach(each -> result.add(readOnly(each)));
			return (T) Collections.unmodifiableSet(result);
		}
		return object;
	}

}
