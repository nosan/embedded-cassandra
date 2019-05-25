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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Cassandra} startup parameters.
 *
 * @author Dmytro Nosan
 * @since 2.0.1
 */
class JvmParameters {

	private static final String JMX_LOCAL_PORT = "-Dcassandra.jmx.local.port";

	private static final String STORAGE_PORT = "-Dcassandra.storage_port";

	private static final String SSL_STORAGE_PORT = "-Dcassandra.ssl_storage_port";

	private static final String NATIVE_TRANSPORT_PORT = "-Dcassandra.native_transport_port";

	private static final String RPC_PORT = "-Dcassandra.rpc_port";

	private static final String JMX_REMOTE_PORT = "-Dcassandra.jmx.remote.port";

	private static final String JMX_REMOTE_RMI_PORT = "-Dcom.sun.management.jmxremote.port";

	private static final String PROPERTY_SEPARATOR = "=";

	private final Supplier<Integer> portSupplier;

	private final JvmOptions jvmOptions;

	private final Ports ports;

	JvmParameters(JvmOptions jvmOptions, Ports ports, Supplier<Integer> portSupplier) {
		this.jvmOptions = jvmOptions;
		this.portSupplier = portSupplier;
		this.ports = ports;
	}

	/**
	 * Returns a new {@code Cassandra} startup parameters that should be associated with the Apache Cassandra.
	 *
	 * @return {@code Cassandra} startup parameters.
	 */
	List<String> getParameters() {
		Ports ports = this.ports;
		JvmOptions jvmOptions = this.jvmOptions;
		Map<String, String> systemProperties = new LinkedHashMap<>(jvmOptions.getSystemProperties());

		List<String> parameters = new ArrayList<>();

		addProperty(JMX_LOCAL_PORT, ports.getJmxLocalPort(), systemProperties);
		addProperty(STORAGE_PORT, ports.getStoragePort(), systemProperties);
		addProperty(SSL_STORAGE_PORT, ports.getSslStoragePort(), systemProperties);
		addProperty(NATIVE_TRANSPORT_PORT, ports.getPort(), systemProperties);
		addProperty(RPC_PORT, ports.getRpcPort(), systemProperties);

		setPort(NATIVE_TRANSPORT_PORT, systemProperties);
		setPort(RPC_PORT, systemProperties);
		setPort(STORAGE_PORT, systemProperties);
		setPort(SSL_STORAGE_PORT, systemProperties);
		setPort(JMX_LOCAL_PORT, systemProperties);
		setPort(JMX_REMOTE_PORT, systemProperties);
		setPort(JMX_REMOTE_RMI_PORT, systemProperties);

		for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (value == null) {
				parameters.add(name);
			}
			else {
				parameters.add(name + PROPERTY_SEPARATOR + value);
			}
		}
		parameters.addAll(jvmOptions.getOptions());
		return Collections.unmodifiableList(parameters);
	}

	private void addProperty(String name, @Nullable Object value, Map<String, String> jvmOptions) {
		if (value != null) {
			jvmOptions.put(name, value.toString());
		}
	}

	private void setPort(String name, Map<String, String> jvmOptions) {
		Integer port = getInteger(name, jvmOptions).orElse(null);
		if (port != null && port == 0) {
			jvmOptions.put(name, Integer.toString(this.portSupplier.get()));
		}
	}

	private Optional<Integer> getInteger(String name, Map<String, String> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private Optional<String> getString(String name, Map<String, String> source) {
		return Optional.ofNullable(source.get(name)).map(Objects::toString);
	}

}
