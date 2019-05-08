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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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

	private final RandomPortSupplier portSupplier;

	ConfigurationFileRandomPortCustomizer(RandomPortSupplier portSupplier) {
		this.portSupplier = portSupplier;
	}

	@Override
	public void customize(Path workingDirectory, Version version) throws IOException {
		RandomPortSupplier portSupplier = this.portSupplier;
		Path file = workingDirectory.resolve("conf/cassandra.yaml");
		Map<Object, Object> oldProperties = readProperties(file);
		Map<Object, Object> newProperties = new LinkedHashMap<>(oldProperties);
		setPort(PORT, newProperties, portSupplier);
		setPort(SSL_PORT, newProperties, portSupplier);
		setPort(RPC_PORT, newProperties, portSupplier);
		setPort(STORAGE_PORT, newProperties, portSupplier);
		setPort(SSL_STORAGE_PORT, newProperties, portSupplier);
		if (!newProperties.equals(oldProperties)) {
			writeProperties(file, newProperties);
		}
	}

	private static Optional<Integer> getInteger(String name, Map<Object, Object> properties) {
		return getString(name, properties).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<String> getString(String name, Map<Object, Object> properties) {
		return Optional.ofNullable(properties.get(name)).map(Object::toString);
	}

	private static void setPort(String name, Map<Object, Object> properties, RandomPortSupplier portSupplier) {
		Integer port = getInteger(name, properties).orElse(null);
		if (port != null && port == 0) {
			properties.put(name, portSupplier.getPort());
		}
	}

	private static Map<Object, Object> readProperties(Path file) throws IOException {
		try (InputStream is = Files.newInputStream(file)) {
			Map<?, ?> values = new Yaml().loadAs(is, Map.class);
			return (values != null) ? new LinkedHashMap<>(values) : new LinkedHashMap<>(0);
		}
	}

	private static void writeProperties(Path file, Map<Object, Object> newProperties) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			new Yaml().dump(newProperties, writer);
		}
	}

}
