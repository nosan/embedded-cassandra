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

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RunProcess}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("ConstantConditions")
class RunProcessTests {

	@Nullable
	private Path temporaryFolder;

	@Nullable
	private StringBuilder output;

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder) {
		this.temporaryFolder = temporaryFolder;
		this.output = new StringBuilder();
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void shouldRunAndWaitUnix() throws Exception {
		Process process = runProcess("bash", "-c", command("echo", "$RUN_PROCESS_TEST"));
		Thread.sleep(500); //waits for output...
		assertThat(process.waitFor()).isEqualTo(0);
		assertThat(this.output.toString()).isEqualTo("TEST");
	}

	private Process runProcess(String... arguments) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(this.temporaryFolder.toFile());
		processBuilder.environment().put("RUN_PROCESS_TEST", "TEST");
		processBuilder.command(arguments);
		return new RunProcess(processBuilder, Thread::new).run(this.output::append);
	}

	private String command(String... arguments) {
		return String.join(" ", arguments);
	}

}
