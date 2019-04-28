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
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.assertj.core.description.Description;
import org.junit.jupiter.api.RepeatedTest;
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

	@RepeatedTest(10)
	void shouldSynchronizeCodeViaFile(@TempDir Path folder) throws Exception {
		Path fileLock = folder.resolve("file.txt.lock");
		Path file = folder.resolve("file.txt");
		writeToFile(file, 0);
		List<Process> processes = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			processes.add(runProcess(folder, fileLock, file));
		}
		for (Process process : processes) {
			int exit = process.waitFor();
			assertThat(exit).describedAs(new Description() {

				@Override
				public String value() {
					return String.format("%nProcess exit code %s. %n%s", exit, readFromProcess(process));
				}

			}).isZero();
		}
		assertThat(Long.parseLong(readFromFile(file))).describedAs(new Description() {

			@Override
			public String value() {
				return String.format("%n%s", readFromProcesses(processes));
			}

		}).isEqualTo(5);
	}

	private static Process runProcess(Path folder, Path lockFile, Path file) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		builder.redirectErrorStream(true);
		builder.directory(folder.toFile());
		Path home = Paths.get(System.getProperty("java.home"));
		if (Files.exists(home.resolve("bin/java"))) {
			builder.command(home.resolve("bin/java").toString());
		}
		else {
			builder.command(home.resolve("bin/java.exe").toString());
		}
		builder.command().add("-cp");
		builder.command().add(System.getProperty("java.class.path"));
		builder.command().add(Suite.class.getTypeName());
		builder.command().add(lockFile.toAbsolutePath().toString());
		builder.command().add(file.toAbsolutePath().toString());
		return builder.start();
	}

	private static void writeToFile(Path file, Object value) throws IOException {
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file,
				StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
			writer.print(value);
		}
	}

	private static String readFromFile(Path file) throws IOException {
		try (InputStream inputStream = Files.newInputStream(file)) {
			return new String(IOUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
		}
	}

	private static String readFromProcesses(List<Process> processes) {
		StringBuilder builder = new StringBuilder();
		for (Process process : processes) {
			builder.append(process).append(String.format("%n")).append(readFromProcess(process))
					.append(String.format("%n%n%n"));
		}
		return builder.toString();
	}

	private static String readFromProcess(Process process) {
		StringBuilder builder = new StringBuilder();
		ProcessUtils.read(process, line -> builder.append(line).append(System.lineSeparator()));
		return builder.toString();
	}

	static final class Suite {

		private static final Logger log = LoggerFactory.getLogger(Suite.class);

		public static void main(String[] args) throws Exception {
			Path lockFile = Paths.get(args[0]);
			try (FileLock fileLock = new FileLock(lockFile)) {
				fileLock.lock();
				Path file = Paths.get(args[1]);
				log.info("Current count : {}", Long.parseLong(readFromFile(file)));
				writeToFile(file, Long.parseLong(readFromFile(file)) + 1);
				log.info("New count : {}", Long.parseLong(readFromFile(file)));
			}
		}

	}

}
