/*
 * Copyright 2018-2018 the original author or authors.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.PortUtils;

/**
 * {@link DirectoryCustomizer} to replace all {@code 0} ports in a {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class PortReplacerCustomizer implements DirectoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(PortReplacerCustomizer.class);

	@Nonnull
	private final Version version;

	PortReplacerCustomizer(@Nonnull Version version) {
		this.version = version;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void customize(@Nonnull Path directory) throws IOException {
		Yaml yaml = new Yaml();
		Path target = directory.resolve("conf/cassandra.yaml");
		Version version = this.version;
		Map<String, Object> source = new LinkedHashMap<>();
		try (InputStream is = Files.newInputStream(target)) {
			Optional.ofNullable(yaml.loadAs(is, Map.class)).ifPresent(source::putAll);
		}
		MapSettings settings = new MapSettings(source, version);
		setPort(source, "native_transport_port", settings::getPort, settings::getRealAddress);
		setPort(source, "native_transport_port_ssl", settings::getSslPort, settings::getRealAddress);
		setPort(source, "rpc_port", settings::getRpcPort, settings::getRealAddress);
		setPort(source, "storage_port", settings::getStoragePort, settings::getRealListenAddress);
		setPort(source, "ssl_storage_port", settings::getSslStoragePort, settings::getRealListenAddress);
		try (BufferedWriter writer = Files.newBufferedWriter(target)) {
			yaml.dump(source, writer);
		}
	}

	private static void setPort(Map<String, Object> source, String property, Supplier<Integer> portSupplier,
			Supplier<InetAddress> addressSupplier) {
		if (source.containsKey(property)) {
			Integer port = portSupplier.get();
			if (port != null && port == 0) {
				InetAddress address = addressSupplier.get();
				port = PortUtils.getPort(address);
				if (log.isDebugEnabled()) {
					log.debug("Replace {}: 0 -> {}  ", property, port);
				}
				source.put(property, port);
			}
		}
	}

}
