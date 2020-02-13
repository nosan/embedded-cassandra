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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class UnixCassandraDatabase extends AbstractCassandraDatabase {

	UnixCassandraDatabase(String name, Version version, Path workingDirectory,
			Map<String, String> environmentVariables, Map<String, Object> configProperties,
			Map<String, String> systemProperties, Set<String> jvmOptions) {
		super(name, version, workingDirectory, environmentVariables, configProperties, systemProperties, jvmOptions);
	}

	@Override
	protected Process doStart(ProcessBuilder processBuilder) throws IOException {
		Path executable = getWorkingDirectory().resolve("bin/cassandra");
		if (!Files.isExecutable(executable)) {
			Set<PosixFilePermission> permissions = new LinkedHashSet<>(Files.getPosixFilePermissions(executable));
			permissions.add(PosixFilePermission.OWNER_EXECUTE);
			permissions.add(PosixFilePermission.GROUP_EXECUTE);
			permissions.add(PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(executable, permissions);
		}
		List<String> command = new ArrayList<>();
		command.add(executable.toString());
		if (getVersion().compareTo(Version.parse("3.1")) > 0) {
			command.add("-R");
		}
		command.add("-f");
		processBuilder.command(command);
		this.logger.info(String.join(" ", command));
		return Process.start(getName(), processBuilder);
	}

	@Override
	protected void doStop(Process process) throws IOException {
		int pid = process.getPid();
		if (pid > 0 && kill(pid) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		if (pid > 0 && sigkill(pid) == 0 && process.waitFor(10, TimeUnit.SECONDS)) {
			return;
		}
		process.destroy();
	}

	private int kill(int pid) throws IOException {
		String name = getName() + "-kill:SIGINT";
		return exec(name, new String[]{"kill", "-SIGINT", Integer.toString(pid)});
	}

	private int sigkill(int pid) throws IOException {
		String name = getName() + "-kill:SIGKILL";
		return exec(name, new String[]{"kill", "-SIGKILL", Integer.toString(pid)});
	}

}
