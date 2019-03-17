/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.util.NetworkUtils;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Utility class for working with JVM Options.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
class JvmOptions implements Supplier<List<String>> {

	private static final Logger log = LoggerFactory.getLogger(JvmOptions.class);

	private final Map<String, String> jvmOptions;

	JvmOptions(List<String> jvmOptions, int jmxPort, Settings settings) {
		this.jvmOptions = Collections.unmodifiableMap(setPorts(parse(normalize(jvmOptions, jmxPort)), settings));
	}

	/**
	 * Return the jvm options.
	 *
	 * @return the jvm options
	 */
	@Override
	public List<String> get() {
		List<String> result = new ArrayList<>();
		for (Map.Entry<String, String> entry : this.jvmOptions.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (value == null) {
				result.add(name);
			}
			else {
				result.add(name + "=" + value);
			}
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * Return the value of {@code -Dcassandra.jmx.local.port}.
	 *
	 * @return the jmx local port
	 */
	Optional<Integer> getJmxLocalPort() {
		return getInteger("-Dcassandra.jmx.local.port", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.jmx.remote.port}.
	 *
	 * @return the jmx remote port
	 */
	Optional<Integer> getJmxRemotePort() {
		return getInteger("-Dcassandra.jmx.remote.port", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.storage_port}.
	 *
	 * @return the storage port
	 */
	Optional<Integer> getStoragePort() {
		return getInteger("-Dcassandra.storage_port", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.ssl_storage_port}.
	 *
	 * @return the ssl storage port
	 */
	Optional<Integer> getSslStoragePort() {
		return getInteger("-Dcassandra.ssl_storage_port", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.start_native_transport}.
	 *
	 * @return whether native transport is enabled or not
	 */
	Optional<Boolean> isStartNativeTransport() {
		return getBoolean("-Dcassandra.start_native_transport", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.native_transport_port}.
	 *
	 * @return the native transport port
	 */
	Optional<Integer> getPort() {
		return getInteger("-Dcassandra.native_transport_port", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.start_rpc}.
	 *
	 * @return whether rpc transport is enabled or not
	 */
	Optional<Boolean> isStartRpc() {
		return getBoolean("-Dcassandra.start_rpc", this.jvmOptions);
	}

	/**
	 * Return the value of {@code -Dcassandra.rpc_port}.
	 *
	 * @return the rpc port
	 */
	Optional<Integer> getRpcPort() {
		return getInteger("-Dcassandra.rpc_port", this.jvmOptions);
	}

	private static List<String> normalize(List<String> options, int jmxPort) {
		return Stream.concat(Stream.of("-Dcassandra.jmx.local.port=" + jmxPort), options.stream())
				.map(option -> option.replaceAll("\\s+", "")).collect(Collectors.toList());
	}

	private static Map<String, String> parse(List<String> options) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String option : options) {
			if (option.startsWith("-D")) {
				int index = option.indexOf("=");
				if (index != -1) {
					result.put(option.substring(0, index), option.substring(index + 1));
				}
				else {
					result.put(option, "");
				}
			}
			else {
				result.put(option, null);
			}
		}
		return result;
	}

	private static Map<String, String> setPorts(Map<String, String> options, Settings settings) {
		Map<String, String> jvmOptions = new LinkedHashMap<>(options);
		setPort("-Dcassandra.native_transport_port", jvmOptions, settings::getRealAddress);
		setPort("-Dcassandra.rpc_port", jvmOptions, settings::getRealAddress);
		setPort("-Dcassandra.storage_port", jvmOptions, settings::getRealListenAddress);
		setPort("-Dcassandra.ssl_storage_port", jvmOptions, settings::getRealListenAddress);
		setPort("-Dcassandra.jmx.local.port", jvmOptions, InetAddress::getLoopbackAddress);
		setPort("-Dcassandra.jmx.remote.port", jvmOptions, () -> getString("java.rmi.server.hostname", options)
				.map(NetworkUtils::getInetAddress)
				.orElseGet(InetAddress::getLoopbackAddress));
		return jvmOptions;
	}

	private static void setPort(String name, Map<String, String> source, Supplier<InetAddress> addressSupplier) {
		getInteger(name, source).ifPresent(originalPort -> {
			if (originalPort == 0) {
				InetAddress address = addressSupplier.get();
				int newPort = PortUtils.getPort(address);
				if (log.isDebugEnabled()) {
					log.debug("Replace {}: {} as {}: {}", name, originalPort, name, newPort);
				}
				source.put(name, String.valueOf(newPort));
			}
		});
	}

	private static Optional<Integer> getInteger(String name, Map<String, String> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<Boolean> getBoolean(String name, Map<String, String> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Boolean::parseBoolean);
	}

	private static Optional<String> getString(String name, Map<String, String> source) {
		return Optional.ofNullable(source.get(name)).map(String::valueOf);
	}

}
