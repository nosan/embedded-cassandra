/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.yaml.snakeyaml.DumperOptions;

/**
 * Utility class to work with different YAML libraries. The Yaml library will be automatically detected based on the
 * classpath
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
final class Yaml {

	private static final String JACKSON_CLASS = "com.fasterxml.jackson.dataformat.yaml.YAMLFactory";

	private static final String SNAKEYAML_CLASS = "org.yaml.snakeyaml.Yaml";

	private static final YamlFactory YAML_FACTORY;

	static {
		ClassLoader classLoader = Yaml.class.getClassLoader();
		YamlFactory yamlFactory;
		if (isPresent(SNAKEYAML_CLASS, classLoader)) {
			yamlFactory = new SnakeYamlFactory();
		}
		else if (isPresent(JACKSON_CLASS, classLoader)) {
			yamlFactory = new JacksonYamlFactory();
		}
		else {
			yamlFactory = new NoYamlImplFactory();
		}
		YAML_FACTORY = yamlFactory;
	}

	private Yaml() {
	}

	/**
	 * Parse YAML document in a stream and produce the corresponding object.
	 *
	 * @param <T> the type of object
	 * @param is data to load from
	 * @param clazz type of the object to be created
	 * @return parsed object
	 * @throws IOException an I/O error occurs or could not parse YAML document
	 */
	static <T> T read(InputStream is, Class<T> clazz) throws IOException {
		Objects.requireNonNull(is, "InputStream must not be null");
		Objects.requireNonNull(clazz, "Class  must not be null");

		try {
			return YAML_FACTORY.read(is, clazz);
		}
		catch (Exception ex) {
			if (ex instanceof IOException) {
				throw ex;
			}
			throw new IOException(ex);
		}
	}

	/**
	 * Writes a Java object into a stream.
	 *
	 * @param value Java object to be serialized to YAML
	 * @param os stream to write to
	 * @throws IOException an I/O error occurs or could not serialize YAML document
	 */
	static void write(Object value, OutputStream os) throws IOException {
		Objects.requireNonNull(value, "Value must not be null");
		Objects.requireNonNull(os, "OutputStream must not be null");
		try {
			YAML_FACTORY.write(value, os);
		}
		catch (Exception ex) {
			if (ex instanceof IOException) {
				throw ex;
			}
			throw new IOException(ex);
		}
	}

	private static boolean isPresent(String clazz, ClassLoader classLoader) {
		try {
			Class.forName(clazz, false, classLoader);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

	private interface YamlFactory {

		<T> T read(InputStream is, Class<T> clazz) throws IOException;

		void write(Object value, OutputStream os) throws IOException;

	}

	private static final class JacksonYamlFactory implements YamlFactory {

		private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
				.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
				.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));

		@Override
		public <T> T read(InputStream is, Class<T> clazz) throws IOException {
			return this.mapper.readValue(is, clazz);
		}

		@Override
		public void write(Object value, OutputStream os) throws IOException {
			this.mapper.writeValue(os, value);
		}

	}

	private static final class SnakeYamlFactory implements YamlFactory {

		@Override
		public <T> T read(InputStream is, Class<T> clazz) {
			return new org.yaml.snakeyaml.Yaml().loadAs(is, clazz);
		}

		@Override
		public void write(Object value, OutputStream os) {
			DumperOptions dumperOptions = new DumperOptions();
			dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			new org.yaml.snakeyaml.Yaml(dumperOptions).dump(value, new OutputStreamWriter(os));
		}

	}

	private static final class NoYamlImplFactory implements YamlFactory {

		@Override
		public <T> T read(InputStream is, Class<T> clazz) {
			return throwException();
		}

		@Override
		public void write(Object value, OutputStream os) {
			throwException();
		}

		private <T> T throwException() {
			throw new UnsupportedOperationException(String.format("Both '%s' and '%s' are not present on classpath."
							+ "%nPlease add one of these dependencies:%n%s%n%s%n",
					SNAKEYAML_CLASS, JACKSON_CLASS, getSnakeyamlDependency(), getJacksonDependency()));
		}

		private static String getSnakeyamlDependency() {
			return String.format("<dependency>%n"
					+ "\t\t<groupId>org.yaml</groupId>%n"
					+ "\t\t<artifactId>snakeyaml</artifactId>%n"
					+ "\t\t<version>${snakeyaml.version}</version>%n"
					+ "</dependency>");
		}

		private static String getJacksonDependency() {
			return String.format("<dependency>%n"
					+ "\t\t<groupId>com.fasterxml.jackson.dataformat</groupId>%n"
					+ "\t\t<artifactId>jackson-dataformat-yaml</artifactId>%n"
					+ "\t\t<version>${jackson-dataformat-yaml.version}</version>%n"
					+ "</dependency>");
		}

	}

}
