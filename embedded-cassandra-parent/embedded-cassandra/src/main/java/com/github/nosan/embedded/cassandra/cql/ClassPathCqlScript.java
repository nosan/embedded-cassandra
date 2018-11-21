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

package com.github.nosan.embedded.cassandra.cql;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.util.ClassUtils;


/**
 * {@link CqlScript} implementation for class path resources. Uses a
 * given {@link ClassLoader} or a given {@link Class} for loading resources.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public final class ClassPathCqlScript extends AbstractCqlResourceScript {

	@Nonnull
	private final String location;

	@Nullable
	private final ClassLoader classLoader;

	@Nullable
	private final Class<?> contextClass;

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param location the absolute path within the class path
	 */
	public ClassPathCqlScript(@Nonnull String location) {
		this(location, null, null, null);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param location the absolute path within the class path
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathCqlScript(@Nonnull String location, @Nullable Charset encoding) {
		this(location, null, null, encoding);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param location the absolute path within the class path
	 * @param classLoader the class loader to load the resource with.
	 */
	public ClassPathCqlScript(@Nonnull String location, @Nullable ClassLoader classLoader) {
		this(location, null, classLoader, null);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param location the absolute path within the class path
	 * @param classLoader the class loader to load the resource with.
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathCqlScript(@Nonnull String location, @Nullable ClassLoader classLoader, @Nullable Charset encoding) {
		this(location, null, classLoader, encoding);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for Class usage.
	 *
	 * @param location the absolute path within the class path
	 * @param contextClass the class to load the resource with.
	 */
	public ClassPathCqlScript(@Nonnull String location, @Nullable Class<?> contextClass) {
		this(location, contextClass, null, null);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for Class usage.
	 *
	 * @param location the absolute path within the class path
	 * @param contextClass the class to load the resource with.
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathCqlScript(@Nonnull String location, @Nullable Class<?> contextClass, @Nullable Charset encoding) {
		this(location, contextClass, null, encoding);
	}

	private ClassPathCqlScript(@Nonnull String location, @Nullable Class<?> contextClass,
			@Nullable ClassLoader classLoader, @Nullable Charset encoding) {
		super(encoding);
		Objects.requireNonNull(location, "Location must not be null");
		this.location = normalize(location, contextClass);
		this.classLoader = (classLoader != null) ? classLoader : ClassUtils.getClassLoader();
		this.contextClass = contextClass;
	}


	@Nonnull
	@Override
	protected InputStream getInputStream() throws FileNotFoundException {
		InputStream stream;
		if (this.contextClass != null) {
			stream = this.contextClass.getResourceAsStream(this.location);
		}
		else if (this.classLoader != null) {
			stream = this.classLoader.getResourceAsStream(this.location);
		}
		else {
			stream = ClassLoader.getSystemResourceAsStream(this.location);
		}
		if (stream == null) {
			throw new FileNotFoundException(
					String.format("(%s) doesn't exist", this.location));
		}
		return stream;
	}

	@Override
	public int hashCode() {
		URL url = this.getURL();
		if (url != null) {
			return Objects.hash(url, getEncoding());
		}
		return Objects.hash(this.location, this.classLoader, this.contextClass, getEncoding());
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		ClassPathCqlScript that = (ClassPathCqlScript) other;
		URL url = getURL();
		if (url != null) {
			return Objects.equals(url, that.getURL()) &&
					Objects.equals(getEncoding(), that.getEncoding());
		}
		return Objects.equals(this.location, that.location) &&
				Objects.equals(this.classLoader, that.classLoader) &&
				Objects.equals(this.contextClass, that.contextClass) &&
				Objects.equals(getEncoding(), that.getEncoding());
	}


	@Override
	@Nonnull
	public String toString() {
		if (this.contextClass == null) {
			return this.location;
		}
		if (this.location.startsWith("/")) {
			return this.location.substring(1);
		}
		String p = this.contextClass.getPackage().getName().replaceAll("\\.", "/");
		return (p + "/" + this.location);
	}


	@Nullable
	private URL getURL() {
		if (this.contextClass != null) {
			return this.contextClass.getResource(this.location);
		}
		if (this.classLoader != null) {
			return this.classLoader.getResource(this.location);
		}
		return ClassLoader.getSystemResource(this.location);
	}


	private static String normalize(String location, Class<?> contextClass) {
		location = location.replaceAll("\\\\", "/").replaceAll("/+", "/");
		if (contextClass == null) {
			while (location.startsWith("/")) {
				location = location.substring(1);
			}
		}
		return location;
	}

}
