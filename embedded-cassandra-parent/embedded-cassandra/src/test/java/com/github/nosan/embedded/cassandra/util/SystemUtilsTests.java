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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SystemUtils}.
 *
 * @author Dmytro Nosan
 */
class SystemUtilsTests {

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void shouldNotBeWindows() {
		assertThat(SystemUtils.isWindows()).isFalse();
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void shouldBeWindows() {
		assertThat(SystemUtils.isWindows()).isTrue();
	}

	@Test
	void getTmpDirectory() {
		assertDirectory(SystemUtils.getTmpDirectory(), "java.io.tmpdir");
	}

	@Test
	void getUserHomeDirectory() {
		assertDirectory(SystemUtils.getUserHomeDirectory(), "user.home");
	}

	@Test
	void getUserDirectory() {
		assertDirectory(SystemUtils.getUserDirectory(), "user.dir");
	}

	@Test
	void getJavaHomeDirectory() {
		assertDirectory(SystemUtils.getJavaHomeDirectory(), "java.home");
	}

	private static void assertDirectory(Optional<Path> path, String property) {
		assertThat(path).hasValue(Paths.get(System.getProperty(property)));
	}

}
