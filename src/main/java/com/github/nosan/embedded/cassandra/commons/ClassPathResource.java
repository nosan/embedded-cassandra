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

package com.github.nosan.embedded.cassandra.commons;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link Resource} implementation for class path resources.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class ClassPathResource implements Resource {

	private final String name;

	private final ClassLoader classLoader;

	/**
	 * Constructs a new {@link ClassPathResource} with the specified resource name.
	 *
	 * @param name the resource name
	 */
	public ClassPathResource(String name) {
		this(name, null);
	}

	/**
	 * Constructs a new {@link ClassPathResource} with the specified resource name and class loader.
	 *
	 * @param name the resource name
	 * @param classLoader class loader used to load a resource
	 */
	public ClassPathResource(String name, ClassLoader classLoader) {
		Objects.requireNonNull(name, "Name must not be null");
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		this.name = clean(name);
		this.classLoader = (classLoader != null) ? classLoader : getClass().getClassLoader();
	}

	@Override
	public Optional<String> getFileName() {
		return Optional.of(getFilename()).filter(StringUtils::hasText);
	}

	@Override
	public boolean exists() {
		return getURL() != null;
	}

	@Override
	public boolean isWritable() {
		try {
			getOutputStream().close();
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public boolean isReadable() {
		try {
			getInputStream().close();
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		URLConnection connection = toURL().openConnection();
		connection.setDoInput(true);
		return connection.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		URLConnection connection = toURL().openConnection();
		connection.setDoOutput(true);
		return connection.getOutputStream();
	}

	@Override
	public URL toURL() throws FileNotFoundException {
		URL url = getURL();
		if (url == null) {
			throw new FileNotFoundException(
					String.format("Classpath resource with a name '%s' does not exist", this.name));
		}
		return url;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		ClassPathResource that = (ClassPathResource) other;
		return this.name.equals(that.name) && Objects.equals(this.classLoader, that.classLoader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.classLoader);
	}

	@Override
	public String toString() {
		return "ClassPathResource{" + "name='" + this.name + '\'' + '}';
	}

	private URL getURL() {
		ClassLoader cl = this.classLoader;
		return (cl != null) ? cl.getResource(this.name) : ClassLoader.getSystemResource(this.name);
	}

	private String getFilename() {
		String name = this.name;
		int index = name.lastIndexOf('/');
		return (index != -1) ? name.substring(index + 1) : name;
	}

	private static String clean(String name) {
		String path = name.trim().replace('\\', '/');
		return path.startsWith("/") ? path.substring(1) : path;
	}

}
