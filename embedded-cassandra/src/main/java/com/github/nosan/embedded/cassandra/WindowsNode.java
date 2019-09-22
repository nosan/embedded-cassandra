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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * Windows {@link Node}.
 *
 * @author Dmytro Nosan
 */
class WindowsNode extends AbstractNode {

	private final Version version;

	private final Path workingDirectory;

	WindowsNode(Version version, Path workingDirectory, List<String> jvmOptions, Map<String, Object> systemProperties,
			Map<String, Object> environmentVariables, Map<String, Object> properties) {
		super(workingDirectory, properties, jvmOptions, systemProperties, environmentVariables);
		this.version = version;
		this.workingDirectory = workingDirectory;
	}

	@Override
	protected NodeProcess doStart(RunProcess runProcess) throws IOException, InterruptedException {
		Path workDir = this.workingDirectory;
		Version version = this.version;
		Path pidFile = Files.createTempFile(workDir, "", ".pid");
		Path executableFile = workDir.resolve("bin/cassandra.ps1");
		runProcess.setArguments("powershell", "-ExecutionPolicy", "Unrestricted", executableFile, "-f", "-p", pidFile);
		if (version.compareTo(Version.of("2.1")) > 0) {
			runProcess.addArguments("-a");
		}
		WindowsProcess process = new WindowsProcess(runProcess.start(), pidFile, workDir);
		long timeout = TimeUnit.SECONDS.toNanos(1);
		long start = System.nanoTime();
		long rem = timeout;
		while (rem > 0 && process.getPid() == -1) {
			Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 10));
			rem = timeout - (System.nanoTime() - start);
		}
		return process;
	}

	private static final class WindowsProcess extends AbstractNodeProcess {

		private static final Logger log = LoggerFactory.getLogger(WindowsProcess.class);

		private final ProcessId processId;

		private final Path pidFile;

		private final Path workingDirectory;

		private long pid = -1;

		private WindowsProcess(ProcessId processId, Path pidFile, Path workingDirectory) {
			super(processId);
			this.processId = processId;
			this.pidFile = pidFile;
			this.workingDirectory = workingDirectory;
		}

		@Override
		public long getPid() {
			if (this.pid == -1) {
				this.pid = this.processId.getPid();
				if (this.pid == -1 && Files.exists(this.pidFile)) {
					this.pid = getPid(this.pidFile);
				}
			}
			return this.pid;
		}

		@Override
		void doStop() throws IOException, InterruptedException {
			long pid = getPid();
			Path pidFile = this.pidFile;
			Process process = this.processId.getProcess();
			Path executableFile = this.workingDirectory.resolve("bin/stop-server.ps1");
			if (Files.exists(executableFile) && Files.exists(pidFile)) {
				doStop(process, executableFile, pidFile, pid);
			}
			else if (pid > 0) {
				doStop(process, pid);
			}
		}

		private void doStop(Process process, long pid) throws IOException, InterruptedException {
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

		private long getPid(Path pidFile) {
			try {
				String pid = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8).replaceAll("\\D+", "");
				return StringUtils.hasText(pid) ? Long.parseLong(pid) : -1;
			}
			catch (Exception ex) {
				return -1;
			}
		}

		private int stop(Path executableFile, Path pidFile) throws InterruptedException, IOException {
			return new RunProcess(this.workingDirectory, "powershell", "-ExecutionPolicy", "Unrestricted",
					executableFile, "-p", pidFile).run(log::info);
		}

		private int taskKill(long pid, boolean forceful) throws InterruptedException, IOException {
			return new RunProcess(this.workingDirectory, "taskkill", forceful ? "/F" : "", "/T", "/PID", pid).run(
					log::info);
		}

	}

}
