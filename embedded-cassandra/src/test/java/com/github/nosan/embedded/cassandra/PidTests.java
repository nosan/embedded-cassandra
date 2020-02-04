/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Pid}.
 *
 * @author Dmytro Nosan
 */
class PidTests {

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void constructProcessIdUnix() throws IOException {
		Process process = new ProcessBuilder("echo", "Hello world").start();
		assertThat(Pid.get(process)).isGreaterThan(0);
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	@DisabledOnJre(JRE.JAVA_8)
	void constructProcessIdWindowsJava9() throws IOException {
		Process process = new ProcessBuilder("echo", "Hello world").start();
		assertThat(Pid.get(process)).isGreaterThan(0);
	}

	@Test
	@EnabledOnJre(JRE.JAVA_8)
	@EnabledOnOs(OS.WINDOWS)
	void constructProcessIdWindowsJava8() throws IOException {
		Process process = new ProcessBuilder("echo", "Hello world").start();
		assertThat(Pid.get(process)).isEqualTo(-1);
	}

}
