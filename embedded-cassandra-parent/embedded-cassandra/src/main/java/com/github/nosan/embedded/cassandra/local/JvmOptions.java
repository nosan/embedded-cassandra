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

package com.github.nosan.embedded.cassandra.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for working with JVM Options.
 *
 * @author Dmytro Nosan
 * @since 1.4.2
 */
class JvmOptions {

	private final List<String> jvmOptions;

	private final Map<String, String> systemProperties;

	/**
	 * Creates a new {@link JvmOptions}.
	 *
	 * @param jvmOptions the jvm options
	 */
	JvmOptions(List<String> jvmOptions) {
		this.jvmOptions = Collections.unmodifiableList(parseJvmOptions(jvmOptions));
		this.systemProperties = Collections.unmodifiableMap(parseSystemProperties(this.jvmOptions));
	}

	/**
	 * Return the jvm options.
	 *
	 * @return the jvm options
	 */
	List<String> getJvmOptions() {
		return this.jvmOptions;
	}

	/**
	 * Return the system properties.
	 *
	 * @return the system properties
	 */
	Map<String, String> getSystemProperties() {
		return this.systemProperties;
	}

	private static Map<String, String> parseSystemProperties(List<String> jvmOptions) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String jvmOption : jvmOptions) {
			if (jvmOption.startsWith("-D")) {
				int index = jvmOption.indexOf("=");
				if (index != -1) {
					result.put(jvmOption.substring(2, index), jvmOption.substring(index + 1));
				}
			}
		}
		return result;
	}

	private static List<String> parseJvmOptions(List<String> jvmOptions) {
		List<String> result = new ArrayList<>();
		for (String jvmOption : jvmOptions) {
			result.add(jvmOption.replaceAll("[\\t\\n\\s]*", ""));
		}
		return result;
	}

}
