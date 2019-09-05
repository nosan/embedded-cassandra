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

package com.github.nosan.embedded.cassandra.commons.io;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Objects;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * {@link Resource} implementation for class path resources.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class ClassPathResource implements Resource {

	private final String path;

	@Nullable
	private final ClassLoader classLoader;

	/**
	 * Constructs a new {@link ClassPathResource} with the specified resource name.
	 *
	 * @param path the resource name
	 */
	public ClassPathResource(String path) {
		this(path, null);
	}

	/**
	 * Constructs a new {@link ClassPathResource} with the specified resource name and class loader.
	 *
	 * @param path the resource name
	 * @param classLoader class loader used to load a resource
	 */
	public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
		if (!StringUtils.hasText(path)) {
			throw new IllegalArgumentException("'name' must not be null or empty");
		}
		this.path = cleanPath(path);
		this.classLoader = (classLoader != null) ? classLoader : getClass().getClassLoader();
	}

	/**
	 * Returns the path for this resource.
	 *
	 * @return the path within classpath
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Returns the ClassLoader used to load this resource.
	 *
	 * @return the class loader, or {@code null}
	 */
	@Nullable
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Override
	public URL toURL() throws FileNotFoundException {
		URL url = getResource();
		if (url == null) {
			throw new FileNotFoundException(String.format("ClassPathResource '%s' does not exist", this.path));
		}
		return url;
	}

	@Override
	public String getFileName() {
		String name = this.path;
		int index = name.lastIndexOf('/');
		return (index != -1) ? name.substring(index + 1) : name;
	}

	@Override
	public boolean exists() {
		return getResource() != null;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		ClassPathResource that = (ClassPathResource) other;
		return this.path.equals(that.path) && Objects.equals(this.classLoader, that.classLoader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.path, this.classLoader);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ClassPathResource.class.getSimpleName() + "[", "]").add(
				"path='" + this.path + "'").toString();
	}

	@Nullable
	private URL getResource() {
		URL url;
		String name = this.path;
		if (this.classLoader != null) {
			url = this.classLoader.getResource(name);
		}
		else {
			url = ClassLoader.getSystemResource(name);
		}
		return url;
	}

	private static String cleanPath(String name) {
		String path = name.replace('\\', '/').replaceAll("/+", "/").trim();
		return path.startsWith("/") ? path.substring(1) : path;
	}

}
