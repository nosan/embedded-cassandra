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

package com.github.nosan.embedded.cassandra.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility class for dealing with system properties. <b>Only for internal purposes.</b>
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public abstract class SystemUtils {

	private static final Logger log = LoggerFactory.getLogger(SystemUtils.class);

	/**
	 * Returns {@code true} if this is Windows.
	 *
	 * @return {@code true} if this is Windows, otherwise {@code false}
	 */
	public static boolean isWindows() {
		String os = getValue("os.name");
		if (StringUtils.hasText(os)) {
			return os.toLowerCase(Locale.ENGLISH).contains("windows");
		}
		return File.separatorChar == '\\';
	}

	/**
	 * Returns the temporary directory.
	 *
	 * @return a directory (java.io.tmpdir)
	 */
	public static Optional<Path> getTmpDirectory() {
		return getProperty("java.io.tmpdir").map(Paths::get);
	}

	/**
	 * Returns the user home directory.
	 *
	 * @return a directory (user.home)
	 */
	public static Optional<Path> getUserHomeDirectory() {
		return getProperty("user.home").map(Paths::get);
	}

	/**
	 * Returns the user directory.
	 *
	 * @return a directory (user.dir)
	 */
	public static Optional<Path> getUserDirectory() {
		return getProperty("user.dir").map(Paths::get);
	}

	/**
	 * Returns the java home directory.
	 *
	 * @return a directory (java.home)
	 */
	public static Optional<Path> getJavaHomeDirectory() {
		return getProperty("java.home").map(Paths::get);
	}

	/**
	 * Returns a {@link System#getProperty(String)} or a {@link System#getenv(String)}.
	 *
	 * @param name the name of the system/env property
	 * @return a value, or {@code empty}
	 */
	public static Optional<String> getProperty(String name) {
		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("Name must not be null or empty");
		}
		return Optional.ofNullable(getValue(name));
	}

	@Nullable
	private static String getValue(String name) {
		try {
			String value = System.getProperty(name);
			if (value == null) {
				value = System.getenv(name);
			}
			return value;
		}
		catch (SecurityException ex) {
			log.error(String.format("Can not get a system or environment property by a name: '%s'", name), ex);
			return null;
		}
	}

}
