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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileLock}.
 *
 * @author Dmytro Nosan
 */
class FileLockTests {

	@Test
	void shouldLockUsingFile(@TempDir Path temporaryFolder) throws Exception {
		Path fileLock = temporaryFolder.resolve(String.format("%s.lock", UUID.randomUUID()));
		Path file = temporaryFolder.resolve("file.txt");
		Files.copy(new ByteArrayInputStream("0".getBytes()), file);

		List<Process> processes = new ArrayList<>();
		Map<Process, ProcessReader> readers = new LinkedHashMap<>();
		for (int i = 0; i < 3; i++) {
			processes.add(runProcess(fileLock, file));
		}
		for (Process process : processes) {
			readers.put(process, new ProcessReader(process));
		}
		for (Process process : processes) {
			assertThat(process.waitFor()).describedAs(String.format("Process exit code %s is not valid. %n%s",
					process.exitValue(), readers.get(process))).isZero();
		}
		assertThat(new String(Files.readAllBytes(file))).describedAs(getDescription(readers)).isEqualTo("3");
	}

	private static Process runProcess(Path fileLock, Path file) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		Path home = Paths.get(System.getProperty("java.home"));
		if (Files.exists(home.resolve("bin/java"))) {
			builder.command(home.resolve("bin/java").toString());
		}
		else {
			builder.command(home.resolve("bin/java.exe").toString());
		}
		builder.command().add("-cp");
		builder.command().add(System.getProperty("java.class.path"));
		builder.command().add(FileLockSuite.class.getCanonicalName());
		builder.command().add(fileLock.toAbsolutePath().toString());
		builder.command().add(file.toAbsolutePath().toString());
		return builder.start();

	}

	private static String getDescription(Map<Process, ProcessReader> readers) {
		return readers.entrySet().stream()
				.map(entry -> String.format("[%s] %s", entry.getKey(), entry.getValue()))
				.collect(Collectors.joining(System.lineSeparator()));
	}

	private static final class ProcessReader extends Thread {

		private final Process process;

		private final StringBuilder output = new StringBuilder();

		ProcessReader(Process process) {
			this.process = process;
			start();
		}

		@Override
		public void run() {
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(this.process.getInputStream()))) {
				bufferedReader.lines().forEach(line -> this.output.append(line).append(System.lineSeparator()));
			}
			catch (IOException ex) {
				StringWriter writer = new StringWriter();
				ex.printStackTrace(new PrintWriter(writer));
				this.output.append(writer);
			}
		}

		@Override
		public String toString() {
			return this.output.toString();
		}

	}

}
