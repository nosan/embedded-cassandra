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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileLock}.
 *
 * @author Dmytro Nosan
 */
class FileLockTests {

	private static final Logger log = LoggerFactory.getLogger(FileLockTests.class);

	@Test
	void shouldLockUsingFile(@TempDir Path temporaryFolder) throws Exception {
		long start = System.currentTimeMillis();
		Path fileLock = temporaryFolder.resolve(String.format("%s.lock", UUID.randomUUID()));
		List<Process> processes = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			processes.add(fork(fileLock));
		}
		for (Process process : processes) {
			assertThat(process.waitFor()).isZero();
		}
		long elapsed = System.currentTimeMillis() - start;
		assertThat(elapsed).describedAs("Seems like 'FileLock' does not work correctly.").isGreaterThan(1500)
				.isLessThan(3000);

	}

	private static Process fork(Path fileLock) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		Path home = Paths.get(new SystemProperty("java.home").getRequired());
		if (Files.exists(home.resolve("bin/java"))) {
			builder.command(home.resolve("bin/java").toString());
		}
		else {
			builder.command(home.resolve("bin/java.exe").toString());
		}
		builder.command().add("-cp");
		builder.command().add(new SystemProperty("java.class.path").getRequired());
		builder.command().add(FileLockSuite.class.getCanonicalName());
		builder.command().add(fileLock.toAbsolutePath().toString());
		Process process = builder.start();
		new Thread(() -> {
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {
				bufferedReader.lines().forEach(log::info);
			}
			catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}).start();
		return process;

	}

}
