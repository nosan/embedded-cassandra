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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SystemProperty}.
 *
 * @author Dmytro Nosan
 */
public class SystemPropertyTests {

	@Rule
	public final ExpectedException throwable = ExpectedException.none();

	@Test
	public void shouldGet() {
		try {
			System.setProperty("test", "value");
			assertThat(new SystemProperty("test").get()).isEqualTo("value");
		}
		finally {
			System.clearProperty("test");
		}
	}

	@Test
	public void shouldNotGet() {
		this.throwable.expect(NullPointerException.class);
		this.throwable.expectMessage("Property value for key (test) is null");
		new SystemProperty("test").get();
	}

	@Test
	public void shouldGetDefaultValue() {
		String defaultValue = "defaultValue";
		String value = new SystemProperty("test").or(defaultValue);
		assertThat(value).isEqualTo(defaultValue);
	}
}
