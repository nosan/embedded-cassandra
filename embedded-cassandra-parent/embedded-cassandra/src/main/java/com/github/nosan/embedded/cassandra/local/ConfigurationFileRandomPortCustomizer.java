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

	@Override
	public void customize(Path workingDirectory, Version version) throws IOException {
		Path file = workingDirectory.resolve("conf/cassandra.yaml");
		Map<Object, Object> properties = getProperties(file);
		try (PortSupplierDelegate supplier = new PortSupplierDelegate()) {
			setPort("native_transport_port", properties, supplier);
			setPort("native_transport_port_ssl", properties, supplier);
			setPort("rpc_port", properties, supplier);
			setPort("storage_port", properties, supplier);
			setPort("ssl_storage_port", properties, supplier);
			if (supplier.isCall()) {
				try (BufferedWriter writer = Files.newBufferedWriter(file)) {
					new Yaml().dump(properties, writer);
				}
			}
		}
	}

	private static Optional<Integer> getInteger(String name, Map<Object, Object> properties) {
		return getString(name, properties).filter(StringUtils::hasText).map(Integer::parseInt);
	}

	private static Optional<String> getString(String name, Map<Object, Object> properties) {
		return Optional.ofNullable(properties.get(name)).map(Object::toString);
	}

	private static void setPort(String name, Map<Object, Object> properties, PortSupplierDelegate supplier) {
		getInteger(name, properties).filter(port -> port == 0)
				.ifPresent(port -> properties.put(name, supplier.get()));
	}

	private static Map<Object, Object> getProperties(Path file) throws IOException {
		try (InputStream is = Files.newInputStream(file)) {
			Map<?, ?> values = new Yaml().loadAs(is, Map.class);
			return (values != null) ? new LinkedHashMap<>(values) : new LinkedHashMap<>();
		}
	}

	private static final class PortSupplierDelegate implements Supplier<Integer>, AutoCloseable {

		private final PortSupplier supplier;

		private boolean call = false;

		PortSupplierDelegate() {
			this.supplier = new PortSupplier(NetworkUtils.getLocalhost());
		}

		@Override
		public Integer get() {
			this.call = true;
			return this.supplier.get();
		}

		@Override
		public void close() {
			this.supplier.close();
		}

		boolean isCall() {
			return this.call;
		}

	}

}
