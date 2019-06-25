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

package com.github.nosan.embedded.cassandra.local;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link WorkingDirectoryCustomizer} to replace all {@code 0} ports in a {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class ConfigurationFileRandomPortCustomizer implements WorkingDirectoryCustomizer {

	private static final String PORT = "native_transport_port";

	private static final String SSL_PORT = "native_transport_port_ssl";

	private static final String RPC_PORT = "rpc_port";

	private static final String STORAGE_PORT = "storage_port";

	private static final String SSL_STORAGE_PORT = "ssl_storage_port";

	private final Supplier<Integer> portSupplier;

	ConfigurationFileRandomPortCustomizer(Supplier<Integer> portSupplier) {
		this.portSupplier = portSupplier;
	}

	@Override
	public void customize(Path workingDirectory, Version version) throws IOException {
		Path file = workingDirectory.resolve("conf/cassandra.yaml");
		Yaml yaml = new Yaml();
		Map<Object, Object> properties = load(yaml, file);
		randomizePort(PORT, properties);
		randomizePort(SSL_PORT, properties);
		randomizePort(RPC_PORT, properties);
		randomizePort(STORAGE_PORT, properties);
		randomizePort(SSL_STORAGE_PORT, properties);
		dump(yaml, file, properties);
	}

	private Map<Object, Object> load(Yaml yaml, Path file) throws IOException {
		try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
			Map<?, ?> values = yaml.loadAs(is, Map.class);
			return (values != null) ? new LinkedHashMap<>(values) : new LinkedHashMap<>(0);
		}
	}

	private void dump(Yaml yaml, Path file, Map<Object, Object> properties) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			yaml.dump(properties, writer);
		}
	}

	private Optional<Integer> getInteger(String name, Map<Object, Object> properties) {
		return getString(name, properties).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private Optional<String> getString(String name, Map<Object, Object> properties) {
		return Optional.ofNullable(properties.get(name)).map(Object::toString);
	}

	private void randomizePort(String name, Map<Object, Object> properties) {
		Integer port = getInteger(name, properties).orElse(null);
		if (port != null && port == 0) {
			properties.put(name, this.portSupplier.get());
		}
	}

}
