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

package com.github.nosan.embedded.cassandra.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Utility class for dealing with {@code System#Properties}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.INTERNAL)
public final class SystemProperty implements Supplier<String> {

	private final String key;

	/**
	 * Creates  a {@code System.property} supplier.
	 *
	 * @param key key to lookup.
	 */
	public SystemProperty(@Nonnull String key) {
		this.key = Objects.requireNonNull(key, "Key must not be null");
	}

	/**
	 * Returns a {@link System#getProperty(String)}.
	 *
	 * @return a value
	 */
	@Override
	@Nonnull
	public String get() {
		String value = getProperty(this.key);
		return Objects.requireNonNull(value, String.format("Property value for key (%s) is null", this.key));
	}

	/**
	 * Returns a {@link System#getProperty(String)} or the given default value.
	 *
	 * @param other default value
	 * @return a value
	 */
	@Nonnull
	public String or(@Nonnull String other) {
		Objects.requireNonNull(other, "Value must not be null");
		String value = getProperty(this.key);
		return (value != null) ? value : other;
	}

	private static String getProperty(@Nonnull String key) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
		}
		return System.getProperty(key);
	}
}
