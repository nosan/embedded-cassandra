/*
 * Copyright 2020-2021 the original author or authors.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.commons.logging.Logger;

class WindowsCassandraDatabase extends AbstractCassandraDatabase {

	private static final Logger LOGGER = Logger.get(WindowsCassandraDatabase.class);

	private final Path pidFile;

	WindowsCassandraDatabase(String name, Version version, Path configurationFile, Path workingDirectory,
			Map<String, String> environmentVariables, Map<String, Object> configProperties,
			Map<String, String> systemProperties, Set<String> jvmOptions, Path pidFile) {
		super(name, version, configurationFile, workingDirectory, environmentVariables, configProperties,
				systemProperties, jvmOptions);
		this.pidFile = pidFile;
	}

	@Override
	protected Process doStart() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getWorkingDirectory().toFile());
		processBuilder.environment().putAll(getEnvironmentVariables());
		return startServer(processBuilder);
	}

	@Override
	protected void doStop(Process process) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getWorkingDirectory().toFile());
		processBuilder.environment().putAll(getEnvironmentVariables());
		if (hasStopFiles() && stopServer(processBuilder, false) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		long pid = process.getPid();
		if (pid > 0 && taskKill(processBuilder, pid, false) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		if (hasStopFiles() && stopServer(processBuilder, true) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		if (pid > 0 && taskKill(processBuilder, pid, true) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		process.destroy();
	}

	Process start(String name, ProcessBuilder processBuilder) throws IOException {
		LOGGER.info("[{0}] {1}", getName(), String.join(" ", processBuilder.command()));
		return new DefaultProcess(name, processBuilder.start());
	}

	int exec(String name, ProcessBuilder processBuilder) throws IOException {
		Process process = start(name, processBuilder);
		process.getStdOut().attach(LOGGER::info);
		process.getStdErr().attach(LOGGER::error);
		return process.waitFor();
	}

	private Process startServer(ProcessBuilder processBuilder) throws IOException {
		try {
			return startServerPowershell(processBuilder);
		}
		catch (IOException powershell) {
			try {
				return startServerBat(processBuilder);
			}
			catch (IOException bat) {
				powershell.addSuppressed(bat);
			}
			throw powershell;
		}
	}

	private Process startServerPowershell(ProcessBuilder processBuilder) throws IOException {
		Path executable = getWorkingDirectory().resolve("bin/cassandra.ps1");
		assertFileExists(executable);
		List<String> command = new ArrayList<>();
		command.add("powershell");
		command.add("-ExecutionPolicy");
		command.add("Unrestricted");
		command.add(executable.toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (getVersion().compareTo(Version.parse("2.2")) >= 0) {
			command.add("-a");
		}
		command.add("-f");
		processBuilder.command(command);
		return start(getName() + ":bin/cassandra.ps1", processBuilder);
	}

	private Process startServerBat(ProcessBuilder processBuilder) throws IOException {
		Path executable = getWorkingDirectory().resolve("bin/cassandra.bat");
		assertFileExists(executable);
		List<String> command = new ArrayList<>();
		command.add(executable.toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (getVersion().compareTo(Version.parse("2.2")) >= 0) {
			command.add("-a");
		}
		command.add("-f");
		processBuilder.command(command);
		return start(getName() + ":bin/cassandra.bat", processBuilder);
	}

	private int stopServer(ProcessBuilder processBuilder, boolean force) throws IOException {
		try {
			return stopServerPowershell(processBuilder, force);
		}
		catch (IOException powershell) {
			try {
				return stopServerBat(processBuilder, force);
			}
			catch (IOException bat) {
				powershell.addSuppressed(bat);
			}
			throw powershell;
		}
	}

	private int stopServerBat(ProcessBuilder processBuilder, boolean force) throws IOException {
		Path executable = getWorkingDirectory().resolve("bin/stop-server.bat");
		assertFileExists(executable);
		List<String> command = new ArrayList<>();
		command.add(executable.toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (force) {
			command.add("-f");
		}
		processBuilder.command(command);
		return exec(getName() + ":bin/stop-server.bat", processBuilder);
	}

	private int stopServerPowershell(ProcessBuilder processBuilder, boolean force) throws IOException {
		Path executable = getWorkingDirectory().resolve("bin/stop-server.ps1");
		assertFileExists(executable);
		List<String> command = new ArrayList<>();
		command.add("powershell");
		command.add("-ExecutionPolicy");
		command.add("Unrestricted");
		command.add(executable.toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (force) {
			command.add("-f");
		}
		processBuilder.command(command);
		return exec(getName() + ":bin/stop-server.ps1", processBuilder);
	}

	private int taskKill(ProcessBuilder processBuilder, long pid, boolean forceful) throws IOException {
		String name = getName() + ":taskkill";
		List<String> command = new ArrayList<>();
		command.add("taskkill");
		command.add("/T");
		if (forceful) {
			command.add("/F");
		}
		command.add("/PID");
		command.add(Long.toString(pid));
		processBuilder.command(command);
		return exec(name, processBuilder);
	}

	private boolean hasStopFiles() {
		Path powershell = getWorkingDirectory().resolve("bin/stop-server.ps1");
		Path bat = getWorkingDirectory().resolve("bin/stop-server.bat");
		return Files.exists(powershell) || Files.exists(bat);
	}

	private static void assertFileExists(Path executable) throws FileNotFoundException {
		if (!Files.exists(executable)) {
			throw new FileNotFoundException(String.format("%s does not exist", executable));
		}
	}

}
