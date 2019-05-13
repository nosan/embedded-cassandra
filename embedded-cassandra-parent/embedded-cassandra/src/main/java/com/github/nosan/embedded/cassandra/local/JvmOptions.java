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

package com.github.nosan.embedded.cassandra.local;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Cassandra} JVM properties.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class JvmOptions implements Supplier<Map<String, String>> {

	private static final String PROPERTY_SEPARATOR = "=";

	private static final String PROPERTY_PREFIX = "-D";

	private final Map<String, String> jvmOptions;

	JvmOptions(List<String> jvmOptions) {
		this.jvmOptions = Collections.unmodifiableMap(parseJvmOptions(jvmOptions));
	}

	/**
	 * Returns a new {@code JVM} options that should be associated with the Apache Cassandra.
	 *
	 * @return {@code JVM} options
	 */
	@Override
	public Map<String, String> get() {
		return this.jvmOptions;
	}

	@Override
	public String toString() {
		return this.jvmOptions.toString();
	}

	private static Map<String, String> parseJvmOptions(List<String> jvmOptions) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String option : jvmOptions) {
			String trimmed = option.trim();
			if (StringUtils.hasText(trimmed)) {
				if (trimmed.startsWith(PROPERTY_PREFIX) && trimmed.contains(PROPERTY_SEPARATOR)) {
					int index = trimmed.indexOf(PROPERTY_SEPARATOR);
					result.put(trimmed.substring(0, index), trimmed.substring(index + 1));
				}
				else {
					result.put(trimmed, null);
				}
			}
		}
		return result;
	}

}
