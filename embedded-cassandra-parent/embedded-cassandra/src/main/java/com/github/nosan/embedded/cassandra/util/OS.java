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

import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Enumeration of operating systems.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
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


	@Nonnull
	private static final OS CURRENT = detect();

	/**
	 * Whether current {@link OS} is Windows or not.
	 *
	 * @return {@code true} if {@link OS} is Windows
	 */
	public static boolean isWindows() {
		return CURRENT == OS.WINDOWS;
	}

	/**
	 * Whether current {@link OS} is Linux or not.
	 *
	 * @return {@code true} if {@link OS} is Linux
	 */
	public static boolean isLinux() {
		return CURRENT == OS.LINUX;
	}

	/**
	 * Whether current {@link OS} is Mac or not.
	 *
	 * @return {@code true} if {@link OS} is Mac
	 */
	public static boolean isMac() {
		return CURRENT == OS.MAC;
	}

	/**
	 * Whether current {@link OS} is Solaris or not.
	 *
	 * @return {@code true} if {@link OS} is Solaris
	 */
	public static boolean isSolaris() {
		return CURRENT == OS.SOLARIS;
	}

	/**
	 * Whether current {@link OS} is unknown or not.
	 *
	 * @return {@code true} if {@link OS} is unknown
	 */
	public static boolean isOther() {
		return CURRENT == OS.OTHER;
	}

	@Nonnull
	private static OS detect() {
		String name = new SystemProperty("os.name").get()
				.toLowerCase(Locale.ENGLISH);

		if (name.contains("linux")) {
			return LINUX;
		}
		if (name.contains("mac")) {
			return MAC;
		}
		if (name.contains("solaris")) {
			return SOLARIS;
		}
		if (name.contains("win")) {
			return WINDOWS;
		}
		return OTHER;
	}
}
