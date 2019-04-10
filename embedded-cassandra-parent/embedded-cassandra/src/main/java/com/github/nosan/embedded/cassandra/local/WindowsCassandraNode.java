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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Windows based {@link CassandraNode} implementation.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class WindowsCassandraNode extends AbstractCassandraNode {

	private final Version version;

	private final Path workingDirectory;

	WindowsCassandraNode(Version version, Path workingDirectory, @Nullable Path javaHome,
			Ports ports, List<String> jvmOptions) {
		super(version, javaHome, ports, jvmOptions);
		this.version = version;
		this.workingDirectory = workingDirectory;
	}

	@Override
	ProcessId start(Map<String, String> environment) throws IOException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		ProcessBuilder builder = new ProcessBuilder().directory(workingDirectory.toFile())
				.redirectErrorStream(true);
		builder.environment().putAll(environment);
		Path pidFile = workingDirectory.resolve(UUID.randomUUID().toString());
		builder.command("powershell", "-ExecutionPolicy", "Unrestricted",
				workingDirectory.resolve("bin/cassandra.ps1").toString(), "-f");
		if (version.getMajor() > 2 || (version.getMajor() == 2 && version.getMinor() > 1)) {
			builder.command().add("-a");
		}
		builder.command().add("-p");
		builder.command().add(pidFile.toString());
		return new ProcessId(new RunProcess(builder).run(), pidFile);
	}

	@Override
	void stop(ProcessId processId, Map<String, String> environment) throws InterruptedException {
		Path workingDirectory = this.workingDirectory;
		ProcessBuilder builder = new ProcessBuilder().directory(workingDirectory.toFile())
				.redirectErrorStream(true);
		builder.environment().putAll(environment);
		Process process = processId.getProcess();
		if (terminate(processId, builder) != 0) {
			process.destroy();
		}
		if (!process.waitFor(5, TimeUnit.SECONDS)) {
			if (kill(processId, builder) != 0) {
				process.destroy();
			}
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				process.destroyForcibly();
			}
		}
	}

	private int terminate(ProcessId processId, ProcessBuilder builder) throws InterruptedException {
		long pid = processId.getPid();
		Path pidFile = processId.getPidFile();
		int exit = killByPidFile(pidFile, builder, false);
		if (exit != 0 && pid != -1) {
			return killByPid(pid, builder, false);
		}
		return exit;
	}

	private int kill(ProcessId processId, ProcessBuilder builder) throws InterruptedException {
		long pid = processId.getPid();
		Path pidFile = processId.getPidFile();
		int exit = killByPidFile(pidFile, builder, true);
		if (exit != 0 && pid != -1) {
			return killByPid(pid, builder, true);
		}
		return exit;
	}

	private int killByPidFile(@Nullable Path pidFile, ProcessBuilder builder, boolean force)
			throws InterruptedException {
		Path stopServerFile = this.workingDirectory.resolve("bin/stop-server.ps1");
		if (pidFile != null && Files.exists(pidFile) && Files.exists(stopServerFile)) {
			builder.command("powershell", "-ExecutionPolicy", "Unrestricted");
			builder.command().add(stopServerFile.toString());
			builder.command().add("-p");
			builder.command().add(pidFile.toString());
			if (force) {
				builder.command().add("-f");
			}
			return new RunProcess(builder).runAndWait();
		}
		return -1;
	}

	private int killByPid(long pid, ProcessBuilder builder, boolean force) throws InterruptedException {
		builder.command("taskkill");
		if (force) {
			builder.command().add("/f");
		}
		builder.command().add("/pid");
		builder.command().add(Long.toString(pid));
		return new RunProcess(builder).runAndWait();
	}

}
