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

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RunProcess}.
 *
 * @author Dmytro Nosan
 */
@DisabledOnOs(OS.WINDOWS)
class RunProcessTests {

	@Test
	void shouldRunUnix(@TempDir Path temporaryFolder) throws Exception {
		Process process = runProcess(temporaryFolder, "bash", "-c", command("echo", "$RUN_PROCESS_TEST")).run();
		StringBuilder output = new StringBuilder();
		ProcessUtils.read(process, output::append);
		assertThat(process.waitFor()).isEqualTo(0);
		assertThat(output.toString()).isEqualTo("TEST");
	}

	@Test
	void shouldRunUnixWait(@TempDir Path temporaryFolder) throws IOException, InterruptedException {
		int exit = runProcess(temporaryFolder, "echo", "Hello World").runAndWait();
		assertThat(exit).isZero();
	}

	private RunProcess runProcess(Path temporaryFolder, String... arguments) throws IOException {
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
