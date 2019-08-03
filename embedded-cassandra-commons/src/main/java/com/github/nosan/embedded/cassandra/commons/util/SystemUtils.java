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

package com.github.nosan.embedded.cassandra.commons.util;

import java.util.Locale;

/**
 * Utility classes for {@code java.lang.System}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class SystemUtils {

	private SystemUtils() {
	}

	/**
	 * Returns {@code true} if this is Windows.
	 *
	 * @return {@code true} if Windows otherwise {@code false}.
	 */
	public static boolean isWindows() {
		String name = System.getProperty("os.name");
		if (name == null) {
			throw new IllegalStateException("'os.name' is not defined. Please set 'os.name' system property");
		}
		return name.toLowerCase(Locale.ENGLISH).contains("windows");
	}

}
