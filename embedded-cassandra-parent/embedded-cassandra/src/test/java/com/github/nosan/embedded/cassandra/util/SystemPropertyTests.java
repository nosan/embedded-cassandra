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

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SystemProperty}.
 *
 * @author Dmytro Nosan
 */
public class SystemPropertyTests {

	@Test
	public void shouldGetSystemProperty() {
		Properties properties = System.getProperties();
		assertThat(properties).isNotEmpty();
		Object key = properties.keySet().iterator().next();
		Object value = properties.get(key);
		assertThat(value).isNotNull();
		assertThat(new SystemProperty(String.valueOf(key)).getRequired()).isEqualTo(value);
	}

	@Test
	public void shouldGetEnvironmentProperty() {
		Map<String, String> environment = System.getenv();
		assertThat(environment).isNotEmpty();
		String key = environment.keySet().iterator().next();
		String value = environment.get(key);
		assertThat(value).isNotNull();
		assertThat(new SystemProperty(key).getRequired()).isEqualTo(value);
	}

	@Test
	public void shouldThrowException() {
		String name = UUID.randomUUID().toString();
		assertThatThrownBy(() -> new SystemProperty(name).getRequired()).isInstanceOf(NullPointerException.class)
				.hasStackTraceContaining("Both System and Environment Properties " +
						"are not present for a key (" + name + ")");
	}

	@Test
	public void shouldReturnNull() {
		assertThat(new SystemProperty(UUID.randomUUID().toString()).get()).isNull();
	}

}
