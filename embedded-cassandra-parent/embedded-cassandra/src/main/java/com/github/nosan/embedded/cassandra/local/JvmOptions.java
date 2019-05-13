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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * {@link Cassandra} JVM properties.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class JvmOptions {

	private static final String PROPERTY_SEPARATOR = "=";

	private static final String PROPERTY_PREFIX = "-D";

	private final Map<String, String> systemProperties;

	private final List<String> options;

	JvmOptions(List<String> jvmOptions) {
		this.systemProperties = Collections.unmodifiableMap(parseSystemProperties(jvmOptions));
		this.options = Collections.unmodifiableList(parseOptions(jvmOptions));
	}

	/**
	 * Returns {@code JVM} system properties.
	 *
	 * @return {@code JVM} system properties
	 */
	Map<String, String> getSystemProperties() {
		return this.systemProperties;
	}

	/**
	 * Returns {@code JVM} options.
	 *
	 * @return {@code JVM} options
	 */
	List<String> getOptions() {
		return this.options;
	}

	private static List<String> parseOptions(List<String> jvmOptions) {
		List<String> result = new ArrayList<>();
		process(jvmOptions, source -> !source.startsWith(PROPERTY_PREFIX), result::add);
		return result;
	}

	private static Map<String, String> parseSystemProperties(List<String> jvmOptions) {
		Map<String, String> result = new LinkedHashMap<>();
		process(jvmOptions, source -> source.startsWith(PROPERTY_PREFIX), source -> {
			int index = source.indexOf(PROPERTY_SEPARATOR);
			if (index != -1) {
				result.put(source.substring(0, index).trim(), source.substring(index + 1).trim());
			}
			else {
				result.put(source, null);
			}

		});
		return result;
	}

	private static void process(List<String> jvmOptions, Predicate<String> filter, Consumer<String> consumer) {
		for (String jvmOption : jvmOptions) {
			String source = jvmOption.trim();
			if (StringUtils.hasText(source) && filter.test(source)) {
				consumer.accept(source);
			}
		}
	}

}
