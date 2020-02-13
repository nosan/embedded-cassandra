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

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.StringJoiner;

class DefaultSettings implements Settings {

	private static final String[] SENSITIVE_KEYS = new String[]{"client_encryption_options",
			"server_encryption_options", "encryption_options", "transparent_data_encryption_options"};

	private final String name;

	private final Version version;

	private final InetAddress address;

	private final int port;

	private final int rpcPort;

	private final Integer sslPort;

	private final Path workingDirectory;

	private final Set<String> jvmOptions;

	private final Map<String, String> systemProperties;

	private final Map<String, String> environmentVariables;

	private final Map<String, Object> configProperties;

	DefaultSettings(String name, Version version, InetAddress address, int port, int rpcPort, Integer sslPort,
			Path workingDirectory, Set<String> jvmOptions, Map<String, String> systemProperties,
			Map<String, String> environmentVariables, Map<String, Object> configProperties) {
		this.name = name;
		this.version = version;
		this.rpcPort = rpcPort;
		this.environmentVariables = Collections.unmodifiableMap(new LinkedHashMap<>(environmentVariables));
		this.systemProperties = Collections.unmodifiableMap(new LinkedHashMap<>(systemProperties));
		this.jvmOptions = Collections.unmodifiableSet(new LinkedHashSet<>(jvmOptions));
		this.configProperties = Collections.unmodifiableMap(new LinkedHashMap<>(configProperties));
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
	public InetAddress getAddress() {
		return this.address;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public int getRpcPort() {
		return this.rpcPort;
	}

	@Override
	public OptionalInt getSslPort() {
		return (this.sslPort != null) ? OptionalInt.of(this.sslPort) : OptionalInt.empty();
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

	@Override
	public String toString() {
		return new StringJoiner(", ", DefaultSettings.class.getSimpleName() + "[", "]")
				.add("name='" + this.name + "'")
				.add("version=" + this.version)
				.add("address=" + this.address)
				.add("port=" + this.port)
				.add("rpcPort=" + this.rpcPort)
				.add("sslPort=" + this.sslPort)
				.add("workingDirectory=" + this.workingDirectory)
				.add("jvmOptions=" + this.jvmOptions)
				.add("systemProperties=" + this.systemProperties)
				.add("environmentVariables=" + this.environmentVariables)
				.add("configProperties=" + hideSensitiveProperties(this.configProperties))
				.toString();
	}

	private static Map<String, Object> hideSensitiveProperties(Map<String, Object> configProperties) {
		Map<String, Object> props = new LinkedHashMap<>(configProperties);
		for (String sensitiveKey : SENSITIVE_KEYS) {
			if (props.containsKey(sensitiveKey)) {
				props.put(sensitiveKey, "<REDACTED>");
			}
		}
		return props;
	}

}
