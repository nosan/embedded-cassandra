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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.commons.io.FileSystemResource;
import com.github.nosan.embedded.cassandra.commons.io.Resource;
import com.github.nosan.embedded.cassandra.commons.io.UrlResource;

/**
 * Abstract {@link Node} that configures startup parameters before start.
 *
 * @author Dmytro Nosan
 */
abstract class AbstractNode implements Node {

	private static final String JVM_EXTRA_OPTS = "JVM_EXTRA_OPTS";

	private final Path workingDirectory;

	private final Map<String, Object> properties;

	private final Map<String, Object> systemProperties;

	private final Map<String, Object> environmentVariables;

	private final List<String> jvmOptions;

	AbstractNode(Path workingDirectory, Map<String, Object> properties, List<String> jvmOptions,
			Map<String, Object> systemProperties, Map<String, Object> environmentVariables) {
		this.workingDirectory = workingDirectory;
		this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
		this.systemProperties = Collections.unmodifiableMap(new LinkedHashMap<>(systemProperties));
		this.environmentVariables = Collections.unmodifiableMap(new LinkedHashMap<>(environmentVariables));
	}

	@Override
	public final NodeProcess start() throws IOException, InterruptedException {
		RunProcess runProcess = new RunProcess(this.workingDirectory);
		Map<String, Object> properties = loadProperties();
		properties.putAll(this.properties);
		Map<String, Object> systemProperties = new LinkedHashMap<>(this.systemProperties);
		configureSystemProperties(systemProperties);
		configureProperties(properties);
		Path configFile = Files.createTempFile(this.workingDirectory.resolve("conf"), "", "-cassandra.yaml");
		dumpProperties(properties, configFile);
		systemProperties.put("cassandra.config", configFile.toUri().toString());
		List<String> jvmOptions = new ArrayList<>(this.jvmOptions);
		for (Map.Entry<String, Object> entry : systemProperties.entrySet()) {
			jvmOptions.add(String.format("-D%s=%s", entry.getKey(), entry.getValue()));
		}
		runProcess.getEnvironment().putAll(this.environmentVariables);

		runProcess.putEnvironment(JVM_EXTRA_OPTS, String.join(" ", jvmOptions));
		return doStart(runProcess);
	}

	/**
	 * Starts {@code Cassandra's} node.
	 *
	 * @param runProcess configured process
	 * @return a new {@link NodeProcess}
	 * @throws IOException if the {@code Cassandra's} node cannot be started
	 * @throws InterruptedException if the {@code Cassandra's} node has been interrupted.
	 */
	protected abstract NodeProcess doStart(RunProcess runProcess) throws IOException, InterruptedException;

	private Map<String, Object> loadProperties() throws IOException {
		try (InputStream is = getConfig().getInputStream()) {
			Yaml yaml = new Yaml();
			Map<String, Object> properties = yaml.load(is);
			return (properties != null) ? new LinkedHashMap<>(properties) : new LinkedHashMap<>(0);
		}
	}

	private Resource getConfig() throws IOException {
		Object url = this.systemProperties.get("cassandra.config");
		if (url != null) {
			return new UrlResource(new URL(url.toString()));
		}
		return new FileSystemResource(this.workingDirectory.resolve("conf/cassandra.yaml"));
	}

	private void dumpProperties(Map<String, Object> properties, Path file) throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(file)) {
			Yaml yaml = new Yaml();
			yaml.dump(properties, bw);
		}
	}

	private void configureProperties(Map<String, Object> properties) throws IOException {
		configurePort(properties, "native_transport_port");
		configurePort(properties, "native_transport_port_ssl");
		configurePort(properties, "rpc_port");
		configurePort(properties, "storage_port");
		configurePort(properties, "ssl_storage_port");
	}

	private void configureSystemProperties(Map<String, Object> systemProperties) throws IOException {
		configurePort(systemProperties, "cassandra.jmx.remote.port");
		configurePort(systemProperties, "cassandra.jmx.local.port");
		configurePort(systemProperties, "cassandra.native_transport_port");
		configurePort(systemProperties, "cassandra.rpc_port");
		configurePort(systemProperties, "cassandra.storage_port");
		configurePort(systemProperties, "cassandra.ssl_storage_port");
	}

	private void configurePort(Map<String, Object> properties, String name) throws IOException {
		if (!Objects.toString(properties.get(name), "").trim().equals("0")) {
			return;
		}
		try (ServerSocket ss = new ServerSocket(0)) {
			properties.put(name, ss.getLocalPort());
		}
	}

}
