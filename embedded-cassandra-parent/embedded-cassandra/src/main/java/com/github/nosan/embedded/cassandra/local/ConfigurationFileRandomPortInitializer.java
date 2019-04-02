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
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Initializer} to replace all {@code 0} ports in a {@code cassandra.yaml}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class ConfigurationFileRandomPortInitializer extends AbstractFileInitializer {

	ConfigurationFileRandomPortInitializer() {
		super((workDir, version) -> workDir.resolve("conf/cassandra.yaml"));
	}

	@Override
	protected void initialize(Path file, Path workingDirectory, Version version) throws IOException {
		if (Files.exists(file)) {
			Yaml yaml = new Yaml();
			Map<Object, Object> originalSource = new LinkedHashMap<>(load(yaml, file));
			Map<Object, Object> newSource = new LinkedHashMap<>(originalSource);

			NodeSettings settings = new NodeSettings(version, newSource);
			setPort("native_transport_port", newSource, settings::getRealAddress);
			setPort("native_transport_port_ssl", newSource, settings::getRealAddress);
			setPort("rpc_port", newSource, settings::getRealAddress);
			setPort("storage_port", newSource, settings::getRealListenAddress);
			setPort("ssl_storage_port", newSource, settings::getRealListenAddress);

			if (!newSource.equals(originalSource)) {
				try (BufferedWriter writer = Files.newBufferedWriter(file)) {
					yaml.dump(newSource, writer);
				}
			}
		}
	}

	private static Optional<Integer> getInteger(String name, Map<?, ?> source) {
		return getString(name, source).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<String> getString(String name, Map<?, ?> source) {
		return Optional.ofNullable(source.get(name)).map(String::valueOf);
	}

	private void setPort(String name, Map<Object, Object> source, Supplier<InetAddress> addressSupplier) {
		getInteger(name, source).filter(port -> port == 0).ifPresent(port -> {
			InetAddress address = addressSupplier.get();
			int newPort = PortUtils.getPort(address);
			if (this.log.isDebugEnabled()) {
				this.log.debug("Replace {}: {} as {}: {}", name, port, name, newPort);
			}
			source.put(name, newPort);
		});
	}

	private Map<?, ?> load(Yaml yaml, Path source) {
		try (InputStream is = Files.newInputStream(source)) {
			Map<?, ?> values = yaml.loadAs(is, Map.class);
			return (values != null) ? values : Collections.emptyMap();
		}
		catch (IOException ex) {
			if (this.log.isDebugEnabled()) {
				this.log.error(String.format("Could not read properties from '%s'", source), ex);
			}
			return Collections.emptyMap();
		}
	}

}
