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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link WorkingDirectoryCustomizer} to replace all {@code 0} ports in a {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class ConfigurationFileRandomPortCustomizer implements WorkingDirectoryCustomizer {

	private static final Set<Integer> CASSANDRA_PORTS = Collections.unmodifiableSet(new LinkedHashSet<>(
			Arrays.asList(7000, 7001, 7199, 9042, 9142, 9160)));

	@Override
	public void customize(Path workingDirectory, Version version) throws IOException {
		Path file = workingDirectory.resolve("conf/cassandra.yaml");
		Map<Object, Object> properties = getProperties(file);
		Set<Integer> skipPorts = new HashSet<>();
		setPort("native_transport_port", properties, skipPorts);
		setPort("native_transport_port_ssl", properties, skipPorts);
		setPort("rpc_port", properties, skipPorts);
		setPort("storage_port", properties, skipPorts);
		setPort("ssl_storage_port", properties, skipPorts);
		if (!skipPorts.isEmpty()) {
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				new Yaml().dump(properties, writer);
			}
		}
	}

	private static Optional<Integer> getInteger(String name, Map<Object, Object> properties) {
		return getString(name, properties).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<String> getString(String name, Map<Object, Object> properties) {
		return Optional.ofNullable(properties.get(name)).map(Object::toString);
	}

	private static void setPort(String name, Map<Object, Object> properties, Set<Integer> skipPorts) {
		getInteger(name, properties).filter(port -> port == 0).ifPresent(port -> {
			int newPort = PortUtils.getPort(integer -> skipPorts.contains(integer)
					|| CASSANDRA_PORTS.contains(integer));
			skipPorts.add(newPort);
			properties.put(name, newPort);
		});
	}

	private static Map<Object, Object> getProperties(Path file) throws IOException {
		try (InputStream is = Files.newInputStream(file)) {
			Map<?, ?> values = new Yaml().loadAs(is, Map.class);
			return (values != null) ? new LinkedHashMap<>(values) : new LinkedHashMap<>();
		}
	}

}
