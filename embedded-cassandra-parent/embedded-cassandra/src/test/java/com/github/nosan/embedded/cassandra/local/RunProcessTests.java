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

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutputExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RunProcess}.
 *
 * @author Dmytro Nosan
 */
@DisabledOnOs(OS.WINDOWS)
@ExtendWith(CaptureOutputExtension.class)
class RunProcessTests {

	@Test
	void shouldRunProcess(@TempDir Path temporaryFolder) throws Exception {
		StringBuilder output = new StringBuilder();
		int exit = runProcess(temporaryFolder, "bash", "-c", command("echo", "$RUN_PROCESS_TEST"))
				.runAndWait(Thread::new, output::append);
		assertThat(output.toString()).isEqualTo("TEST");
		assertThat(exit).isEqualTo(0);
	}

	@Test
	void shouldNotRunProcess(@TempDir Path temporaryFolder, CaptureOutput output) throws Exception {
		int exit = runProcess(temporaryFolder, "invalid_command")
				.runAndWait(Thread::new, line -> {
				});
		assertThat(exit).isEqualTo(-1);
		assertThat(output.toString()).contains("Can not execute 'invalid_command'");
	}

	private RunProcess runProcess(Path temporaryFolder, String... arguments) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(temporaryFolder.toFile());
		processBuilder.environment().put("RUN_PROCESS_TEST", "TEST");
		processBuilder.command(arguments);
		return new RunProcess(processBuilder);
	}

	private String command(String... arguments) {
		return String.join(" ", arguments);
	}

}
