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
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.commons.logging.Logger;

class UnixCassandraDatabase extends AbstractCassandraDatabase {

	private static final Logger LOGGER = Logger.get(UnixCassandraDatabase.class);

	UnixCassandraDatabase(String name, Version version, Path configurationFile, Path workingDirectory,
			Map<String, String> environmentVariables, Map<String, Object> configProperties,
			Map<String, String> systemProperties, Set<String> jvmOptions) {
		super(name, version, configurationFile, workingDirectory, environmentVariables, configProperties,
				systemProperties, jvmOptions);
	}

	@Override
	protected Process doStart() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getWorkingDirectory().toAbsolutePath().toFile());
		processBuilder.environment().putAll(getEnvironmentVariables());
		Path executable = getWorkingDirectory().resolve("bin/cassandra").toAbsolutePath();
		if (!Files.exists(executable)) {
			throw new FileNotFoundException(String.format("%s does not exist", executable));
		}
		setExecutable(executable);
		List<String> command = new ArrayList<>();
		command.add(executable.toString());
		if (getVersion().compareTo(Version.parse("3.1")) > 0) {
			command.add("-R");
		}
		command.add("-f");
		return start(getName() + ":bin/cassandra", processBuilder.command(command));
	}

	@Override
	protected void doStop(Process process) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getWorkingDirectory().toAbsolutePath().toFile());
		processBuilder.environment().putAll(getEnvironmentVariables());
		long pid = process.getPid();
		if (pid > 0 && kill(processBuilder, pid) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		if (pid > 0 && sigkill(processBuilder, pid) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		process.destroy();
	}

	void setExecutable(Path executable) throws IOException {
		if (!Files.isExecutable(executable)) {
			Set<PosixFilePermission> permissions = new LinkedHashSet<>(Files.getPosixFilePermissions(executable));
			permissions.add(PosixFilePermission.OWNER_EXECUTE);
			permissions.add(PosixFilePermission.GROUP_EXECUTE);
			permissions.add(PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(executable, permissions);
		}
	}

	Process start(String name, ProcessBuilder processBuilder) throws IOException {
		LOGGER.info("[{0}] {1}", name, String.join(" ", processBuilder.command()));
		return new DefaultProcess(name, processBuilder.start());
	}

	int exec(String name, ProcessBuilder processBuilder) throws IOException {
		Process process = start(name, processBuilder);
		process.getStdOut().attach(LOGGER::info);
		process.getStdErr().attach(LOGGER::error);
		return process.waitFor();
	}

	private int kill(ProcessBuilder processBuilder, long pid) throws IOException {
		String name = getName() + ":kill";
		List<String> command = new ArrayList<>();
		command.add("kill");
		command.add("-SIGINT");
		command.add(Long.toString(pid));
		return exec(name, processBuilder.command(command));
	}

	private int sigkill(ProcessBuilder processBuilder, long pid) throws IOException {
		String name = getName() + ":kill";
		List<String> command = new ArrayList<>();
		command.add("kill");
		command.add("-SIGKILL");
		command.add(Long.toString(pid));
		return exec(name, processBuilder.command(command));
	}

}
