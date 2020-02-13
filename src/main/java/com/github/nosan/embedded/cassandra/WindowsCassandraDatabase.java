/*
 * Copyright 2020 the original author or authors.
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class WindowsCassandraDatabase extends AbstractCassandraDatabase {

	private final Path pidFile;

	WindowsCassandraDatabase(String name, Version version, Path workingDirectory,
			Map<String, String> environmentVariables, Map<String, Object> configProperties,
			Map<String, String> systemProperties, Set<String> jvmOptions, Path pidFile) {
		super(name, version, workingDirectory, environmentVariables, configProperties, systemProperties, jvmOptions);
		this.pidFile = pidFile;
	}

	@Override
	protected Process doStart(ProcessBuilder processBuilder) throws IOException {
		return startServer(processBuilder);
	}

	@Override
	protected void doStop(Process process) throws IOException {
		if (stopServer(false) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		if (stopServer(true) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		int pid = process.getPid();
		if (pid > 0 && taskKill(pid, false) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		if (pid > 0 && taskKill(pid, true) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		process.destroy();
	}

	private Process startServer(ProcessBuilder processBuilder) throws IOException {
		try {
			return startServerPowershell(processBuilder);
		}
		catch (Exception ex) {
			try {
				return startServerBat(processBuilder);
			}
			catch (Exception suppressed) {
				ex.addSuppressed(suppressed);
			}
			throw ex;
		}
	}

	private Process startServerPowershell(ProcessBuilder processBuilder) throws IOException {
		List<String> command = new ArrayList<>();
		command.add("powershell");
		command.add("-ExecutionPolicy");
		command.add("Unrestricted");
		command.add(getWorkingDirectory().resolve("bin/cassandra.ps1").toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (getVersion().compareTo(Version.parse("2.1")) > 0) {
			command.add("-a");
		}
		command.add("-f");
		logCommand(command);
		return Process.start(getName(), processBuilder.command(command));
	}

	private Process startServerBat(ProcessBuilder processBuilder) throws IOException {
		List<String> command = new ArrayList<>();
		command.add(getWorkingDirectory().resolve("bin/cassandra.bat").toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (getVersion().compareTo(Version.parse("2.1")) > 0) {
			command.add("-a");
		}
		command.add("-f");
		processBuilder.command(command);
		logCommand(command);
		return Process.start(getName(), processBuilder.command(command));
	}

	private int stopServer(boolean force) throws IOException {
		try {
			return stopServerPowershell(force);
		}
		catch (Exception ex) {
			try {
				return stopServerBat(force);
			}
			catch (Exception suppressed) {
				ex.addSuppressed(suppressed);
			}
			throw ex;
		}
	}

	private int stopServerBat(boolean force) throws IOException {
		String name = getName() + "-bin/stop-server.bat";
		List<String> command = new ArrayList<>();
		command.add(getWorkingDirectory().resolve("bin/stop-server.bat").toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (force) {
			command.add("-f");
		}
		logCommand(command);
		return exec(name, command.toArray(new String[0]));
	}

	private int stopServerPowershell(boolean force) throws IOException {
		String name = getName() + "-bin/stop-server.ps1";
		List<String> command = new ArrayList<>();
		command.add("powershell");
		command.add("-ExecutionPolicy");
		command.add("Unrestricted");
		command.add(getWorkingDirectory().resolve("bin/stop-server.ps1").toString());
		command.add("-p");
		command.add(this.pidFile.toString());
		if (force) {
			command.add("-f");
		}
		logCommand(command);
		return exec(name, command.toArray(new String[0]));
	}

	private int taskKill(int pid, boolean forceful) throws IOException {
		String name = getName() + "-taskkill";
		List<String> command = new ArrayList<>();
		command.add("taskkill");
		command.add("/T");
		if (forceful) {
			command.add("/F");
		}
		command.add("/PID");
		command.add(Integer.toString(pid));
		logCommand(command);
		return exec(name, command.toArray(new String[0]));
	}

	private void logCommand(List<String> command) {
		this.logger.info("{0}: {1}", getName(), String.join(" ", command));
	}

}
