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

package com.github.nosan.embedded.cassandra.cql;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.ClassUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link CqlScript} implementation for class path resources. Uses a
 * given {@link ClassLoader} or a given {@link Class} for loading resources.
 *
 * @author Dmytro Nosan
 * @see CqlScript#classpath(String...)
 * @see CqlScript#classpath(Class, String...)
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.STABLE)
public final class ClassPathCqlScript extends AbstractCqlResourceScript {

	private static final String WINDOWS = "\\\\";

	@Nonnull
	private final String name;

	@Nullable
	private final ClassLoader classLoader;

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param name the absolute path within the class path
	 */
	public ClassPathCqlScript(@Nonnull String name) {
		this(name, ClassUtils.getClassLoader(), null);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param name the absolute path within the class path
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathCqlScript(@Nonnull String name, @Nullable Charset encoding) {
		this(name, ClassUtils.getClassLoader(), encoding);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param name the absolute path within the class path
	 * @param classLoader the class loader to load the resource with.
	 */
	public ClassPathCqlScript(@Nonnull String name, @Nullable ClassLoader classLoader) {
		this(name, classLoader, null);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for Class usage.
	 *
	 * @param name the absolute path within the class path
	 * @param contextClass the class to load the resource with.
	 */
	public ClassPathCqlScript(@Nonnull String name, @Nullable Class<?> contextClass) {
		this(name, contextClass, null);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for Class usage.
	 *
	 * @param name the absolute path within the class path
	 * @param contextClass the class to load the resource with.
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathCqlScript(@Nonnull String name, @Nullable Class<?> contextClass, @Nullable Charset encoding) {
		super(encoding);
		Objects.requireNonNull(name, "Location must not be null");
		this.name = cleanLocation(name, contextClass);
		this.classLoader = getClassLoader(contextClass);
	}

	/**
	 * Create a new {@link ClassPathCqlScript} for ClassLoader usage.
	 *
	 * @param name the absolute path within the class path
	 * @param classLoader the class loader to load the resource with.
	 * @param encoding the encoding to use for reading from the resource
	 */
	public ClassPathCqlScript(@Nonnull String name, @Nullable ClassLoader classLoader, @Nullable Charset encoding) {
		super(encoding);
		Objects.requireNonNull(name, "Location must not be null");
		this.name = cleanLocation(name, null);
		this.classLoader = getClassLoader(classLoader);
	}

	@Nonnull
	@Override
	protected InputStream getInputStream() throws FileNotFoundException {
		InputStream stream;
		String location = this.name;
		if (this.classLoader != null) {
			stream = this.classLoader.getResourceAsStream(location);
		}
		else {
			stream = ClassLoader.getSystemResourceAsStream(location);
		}
		if (stream == null) {
			throw new FileNotFoundException(String.format("(%s) doesn't exist", location));
		}
		return stream;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.classLoader, getEncoding());
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
		return Objects.equals(this.name, that.name) &&
				Objects.equals(this.classLoader, that.classLoader) &&
				Objects.equals(getEncoding(), that.getEncoding());
	}

	@Override
	@Nonnull
	public String toString() {
		return this.name;
	}

	private static String cleanLocation(String location, Class<?> contextClass) {
		String name = location.replaceAll(WINDOWS, "/").replaceAll("/+", "/");
		if (contextClass != null && !name.startsWith("/")) {
			String packageName = ClassUtils.getPackageName(contextClass);
			if (StringUtils.hasText(packageName)) {
				name = String.format("%s/%s", packageName.replace('.', '/'), location);
			}
		}
		if (name.startsWith("/")) {
			name = name.substring(1);
		}
		return name;
	}

	private static ClassLoader getClassLoader(Class<?> contextClass) {
		return getClassLoader((contextClass != null) ? contextClass.getClassLoader() : null);
	}

	private static ClassLoader getClassLoader(ClassLoader classLoader) {
		return (classLoader != null) ? classLoader : ClassUtils.getClassLoader();
	}
}
