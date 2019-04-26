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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Cassandra} native JVM system parameters.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class JvmOptions {

	private static final String JMX_LOCAL_PORT = "-Dcassandra.jmx.local.port";

	private static final String JMX_REMOTE_PORT = "-Dcassandra.jmx.remote.port";

	private static final String JMX_REMOTE_RMI_PORT = "-Dcom.sun.management.jmxremote.port";

	private static final String STORAGE_PORT = "-Dcassandra.storage_port";

	private static final String SSL_STORAGE_PORT = "-Dcassandra.ssl_storage_port";

	private static final String NATIVE_TRANSPORT_PORT = "-Dcassandra.native_transport_port";

	private static final String RPC_PORT = "-Dcassandra.rpc_port";

	private static final String PROPERTY_SEPARATOR = "=";

	private static final String PROPERTY_PREFIX = "-D";

	private static final String SPACE = " ";

	private final Map<String, String> jvmOptions;

	JvmOptions(Ports ports, List<String> jvmOptions) {
		this.jvmOptions = getJvmOptions(ports, jvmOptions);
	}

	@Override
	public String toString() {
		List<String> result = new ArrayList<>();
		for (Map.Entry<String, String> entry : this.jvmOptions.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (value == null) {
				result.add(name);
			}
			else {
				result.add(name + PROPERTY_SEPARATOR + value);
			}
		}
		return String.join(SPACE, result);
	}

	private static Map<String, String> getJvmOptions(Ports ports, List<String> options) {
		Map<String, String> jvmOptions = getJvmOptions(options);

		addOption(JMX_LOCAL_PORT, ports.getJmxLocalPort(), jvmOptions);
		addOption(STORAGE_PORT, ports.getStoragePort(), jvmOptions);
		addOption(SSL_STORAGE_PORT, ports.getSslStoragePort(), jvmOptions);
		addOption(NATIVE_TRANSPORT_PORT, ports.getPort(), jvmOptions);
		addOption(RPC_PORT, ports.getRpcPort(), jvmOptions);

		try (PortSupplier supplier = new PortSupplier(NetworkUtils.getLocalhost())) {
			setPort(NATIVE_TRANSPORT_PORT, jvmOptions, supplier);
			setPort(RPC_PORT, jvmOptions, supplier);
			setPort(STORAGE_PORT, jvmOptions, supplier);
			setPort(SSL_STORAGE_PORT, jvmOptions, supplier);
			setPort(JMX_LOCAL_PORT, jvmOptions, supplier);
			setPort(JMX_REMOTE_PORT, jvmOptions, supplier);
			setPort(JMX_REMOTE_RMI_PORT, jvmOptions, supplier);
		}
		return jvmOptions;
	}

	private static Map<String, String> getJvmOptions(List<String> jvmOptions) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String option : jvmOptions) {
			String trimmed = option.trim();
			if (StringUtils.hasText(trimmed)) {
				if (trimmed.startsWith(PROPERTY_PREFIX) && trimmed.contains(PROPERTY_SEPARATOR)) {
					int index = trimmed.indexOf(PROPERTY_SEPARATOR);
					result.put(trimmed.substring(0, index), trimmed.substring(index + 1));
				}
				else {
					result.put(trimmed, null);
				}
			}
		}
		return result;
	}

	private static void addOption(String name, @Nullable Object value, Map<String, String> jvmOptions) {
		if (value != null) {
			jvmOptions.put(name, value.toString());
		}
	}

	private static void setPort(String name, Map<String, String> jvmOptions, PortSupplier supplier) {
		getInteger(name, jvmOptions).filter(port -> port == 0)
				.ifPresent(port -> jvmOptions.put(name, Integer.toString(supplier.get())));
	}

	private static Optional<Integer> getInteger(String name, Map<String, String> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<String> getString(String name, Map<String, String> source) {
		return Optional.ofNullable(source.get(name)).map(Objects::toString);
	}

}
