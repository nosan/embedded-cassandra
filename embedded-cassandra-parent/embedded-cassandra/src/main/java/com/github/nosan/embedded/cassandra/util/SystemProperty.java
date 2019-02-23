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

package com.github.nosan.embedded.cassandra.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Utility class for dealing with {@code System and Environment Properties}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.INTERNAL)
public final class SystemProperty implements Supplier<String> {

	private final String name;

	/**
	 * Creates a {@link SystemProperty}.
	 *
	 * @param name key to lookup.
	 */
	public SystemProperty(@Nonnull String name) {
		this.name = Objects.requireNonNull(name, "Name must not be null");
	}

	/**
	 * Returns a {@link System#getProperty(String)} or a {@link System#getenv(String)}.
	 *
	 * @return a value, or {@code null}
	 */
	@Override
	@Nullable
	public String get() {
		String value = getSystemProperty(this.name);
		if (value == null) {
			value = getEnvironmentProperty(this.name);
		}
		return value;
	}

	/**
	 * Returns a {@link System#getProperty(String)} or a {@link System#getenv(String)}.
	 *
	 * @return a nonnull-value, or throw a {@code NullPointerException}
	 */
	@Nonnull
	public String getRequired() {
		return Objects.requireNonNull(get(), () -> String.format("Both System and Environment Properties" +
				" are not present for a key (%s)", this.name));
	}

	private static String getSystemProperty(String key) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
		}
		return System.getProperty(key);
	}

	private static String getEnvironmentProperty(String key) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getenv(key));
		}
		return System.getenv(key);
	}
}
