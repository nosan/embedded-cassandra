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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
	public void customize(@Nonnull Path directory) throws IOException {
		Yaml yaml = new Yaml();
		Path configurationFile = directory.resolve("conf/cassandra.yaml");

		Map<Object, Object> originalSource = new LinkedHashMap<>(load(yaml, configurationFile));
		Map<Object, Object> newSource = new LinkedHashMap<>(originalSource);

		replace(newSource, this.version);

		if (newSource.equals(originalSource)) {
			return;
		}

		try (BufferedWriter writer = Files.newBufferedWriter(configurationFile)) {
			yaml.dump(newSource, writer);
		}
	}


	private static void setPort(Map<Object, Object> source, String property, Supplier<Integer> portSupplier,
			Supplier<InetAddress> addressSupplier) {

		if (!source.containsKey(property)) {
			return;
		}

		Integer originalPort = portSupplier.get();
		if (originalPort == null || originalPort != 0) {
			return;
		}

		InetAddress address = addressSupplier.get();
		int newPort = PortUtils.getPort(address);

		if (log.isDebugEnabled()) {
			log.debug("Replace {}: {} as {}: {}", property, originalPort, property, newPort);
		}

		source.put(property, newPort);
	}

	private static void replace(Map<Object, Object> newSource, Version version) {
		MapSettings settings = new MapSettings(newSource, version);
		setPort(newSource, "native_transport_port", settings::getPort, settings::getRealAddress);
		setPort(newSource, "native_transport_port_ssl", settings::getSslPort, settings::getRealAddress);
		setPort(newSource, "rpc_port", settings::getRpcPort, settings::getRealAddress);
		setPort(newSource, "storage_port", settings::getStoragePort, settings::getRealListenAddress);
		setPort(newSource, "ssl_storage_port", settings::getSslStoragePort, settings::getRealListenAddress);
	}


	private static Map<?, ?> load(Yaml yaml, Path source) {
		try (InputStream is = Files.newInputStream(source)) {
			Map<?, ?> value = yaml.loadAs(is, Map.class);
			return (value != null) ? value : Collections.emptyMap();
		}
		catch (IOException ex) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Could not read properties from (%s)", source), ex);
			}
			return Collections.emptyMap();
		}
	}

}
