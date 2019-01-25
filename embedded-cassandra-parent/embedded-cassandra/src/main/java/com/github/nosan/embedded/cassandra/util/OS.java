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

import java.util.Locale;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Enumeration of operating systems.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.INTERNAL)
public enum OS {

	/**
	 * Linux-based operating system.
	 */
	LINUX,

	/**
	 * Apple Macintosh operating system (e.g., macOS).
	 */
	MAC,

	/**
	 * Oracle Solaris operating system.
	 */
	SOLARIS,

	/**
	 * Microsoft Windows operating system.
	 */
	WINDOWS,

	/**
	 * An operating system other than {@link #LINUX}, {@link #MAC},
	 * {@link #SOLARIS}, or {@link #WINDOWS}.
	 */
	OTHER;

	private static final OS CURRENT = detect();

	/**
	 * Determines the current OS based on {@code System.getProperty(os.name)}.
	 *
	 * @return the current OS.
	 */
	@Nonnull
	public static OS get() {
		return CURRENT;
	}

	@Nonnull
	private static OS detect() {
		String name = new SystemProperty("os.name").getRequired()
				.toLowerCase(Locale.ENGLISH);
		if (name.contains("linux")) {
			return LINUX;
		}
		if (name.contains("windows")) {
			return WINDOWS;
		}
		if (name.contains("solaris") || name.contains("sunos")) {
			return SOLARIS;
		}
		if (name.contains("mac")) {
			return MAC;
		}
		return OTHER;
	}
}
