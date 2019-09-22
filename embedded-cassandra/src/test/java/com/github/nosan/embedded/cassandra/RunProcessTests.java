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

package com.github.nosan.embedded.cassandra;

import java.nio.file.Path;
import java.util.Arrays;

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
class RunProcessTests {

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void shouldRunProcessUnix(@TempDir Path temporaryFolder) throws Exception {
		StringBuilder output = new StringBuilder();
		int exit = runProcess(temporaryFolder, "bash", "-c", command("echo", "$RUN_PROCESS_TEST")).run(output::append);
		assertThat(output.toString()).isEqualTo("TEST");
		assertThat(exit).isEqualTo(0);
	}

	private RunProcess runProcess(Path temporaryFolder, String... arguments) {
		RunProcess runProcess = new RunProcess(temporaryFolder);
		runProcess.getArguments().addAll(Arrays.asList(arguments));
		runProcess.putEnvironment("RUN_PROCESS_TEST", "TEST");
		return runProcess;
	}

	private String command(String... arguments) {
		return String.join(" ", arguments);
	}

}
