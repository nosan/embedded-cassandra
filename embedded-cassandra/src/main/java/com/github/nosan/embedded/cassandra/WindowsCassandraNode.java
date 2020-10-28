/*
 * Copyright 2018-2020 the original author or authors.
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * Windows {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 */
class WindowsCassandraNode extends AbstractCassandraNode {

	private final Version version;

	private final Path workingDirectory;

	@Nullable
	private volatile Path pidFile;

	WindowsCassandraNode(Version version, Path workingDirectory, List<String> jvmOptions,
			Map<String, Object> systemProperties,
			Map<String, Object> environmentVariables, Map<String, Object> properties) {
		super(version, workingDirectory, properties, jvmOptions, systemProperties, environmentVariables);
		this.version = version;
		this.workingDirectory = workingDirectory;
	}

	@Override
	Process doStart(RunProcess runProcess) throws IOException {
		Path workDir = this.workingDirectory;
		Version version = this.version;
		Path pidFile = Files.createTempFile(workDir, "", ".pid");
		this.pidFile = pidFile;
		Path executableFile = workDir.resolve("bin/cassandra.ps1");
		runProcess.setArguments("powershell", "-ExecutionPolicy", "Unrestricted", executableFile, "-f", "-p", pidFile);
		if (version.compareTo(Version.of("2.1")) > 0) {
			runProcess.addArguments("-a");
		}
		return runProcess.start();
	}

	@Override
	void doStop(Process process, long pid) throws IOException, InterruptedException {
		Path pidFile = this.pidFile;
		Path executableFile = this.workingDirectory.resolve("bin/stop-server.ps1");
		if (Files.exists(executableFile) && (pidFile != null && Files.exists(pidFile))) {
			doStop(process, executableFile, pidFile, pid);
		}
		else if (pid > 0) {
			doTaskKill(process, pid);
		}
		this.pidFile = null;
	}

	@Override
	long getPid(Process process) throws IOException, InterruptedException {
		long timeout = TimeUnit.SECONDS.toNanos(1);
		long start = System.nanoTime();
		long rem = timeout;
		long pid = super.getPid(process);
		while (rem > 0 && pid == -1) {
			Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 50));
			rem = timeout - (System.nanoTime() - start);
			Path pidFile = this.pidFile;
			if (pidFile != null && Files.exists(pidFile)) {
				pid = getPid(pidFile);
			}
		}
		return pid;
	}

	private void doTaskKill(Process process, long pid) throws IOException, InterruptedException {
		if (taskKill(pid, false) == 0) {
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				taskKill(pid, true);
			}
		}
	}

	private void doStop(Process process, Path executableFile, Path pidFile, long pid)
			throws IOException, InterruptedException {
		if (stop(executableFile, pidFile) == 0) {
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				if (pid > 0) {
					doStop(process, pid);
				}
			}
		}
		else if (pid > 0) {
			doStop(process, pid);
		}
	}

	private long getPid(Path pidFile) throws IOException {
		String pid = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8).replaceAll("\\D+", "");
		return StringUtils.hasText(pid) ? Long.parseLong(pid) : -1;
	}

	private int stop(Path executableFile, Path pidFile) throws InterruptedException, IOException {
		return new RunProcess(this.workingDirectory, "powershell", "-ExecutionPolicy", "Unrestricted",
				executableFile, "-p", pidFile).run(this.logger::info);
	}

	private int taskKill(long pid, boolean forceful) throws InterruptedException, IOException {
		return new RunProcess(this.workingDirectory, "taskkill", forceful ? "/F" : "", "/T", "/PID", pid)
				.run(this.logger::info);
	}

}
