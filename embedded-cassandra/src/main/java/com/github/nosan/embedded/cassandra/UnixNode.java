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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.ProcessId;
import com.github.nosan.embedded.cassandra.commons.RunProcess;

/**
 * Unix {@link Node}.
 *
 * @author Dmytro Nosan
 */
class UnixNode extends AbstractNode {

	private final Version version;

	private final Path workingDirectory;

	private final boolean rootAllowed;

	UnixNode(Version version, Path workingDirectory, List<String> jvmOptions, Map<String, Object> systemProperties,
			Map<String, Object> environmentVariables, Map<String, Object> properties, boolean rootAllowed) {
		super(workingDirectory, properties, jvmOptions, systemProperties, environmentVariables);
		this.version = version;
		this.workingDirectory = workingDirectory;
		this.rootAllowed = rootAllowed;
	}

	@Override
	protected NodeProcess doStart(RunProcess runProcess) throws IOException {
		Path workDir = this.workingDirectory;
		Path executableFile = workDir.resolve("bin/cassandra");
		if (!Files.isExecutable(executableFile)) {
			executableFile.toFile().setExecutable(true);
		}
		runProcess.setArguments(executableFile, "-f");
		if (this.rootAllowed && this.version.compareTo(Version.of("3.1")) > 0) {
			runProcess.addArguments("-R");
		}
		return new UnixProcess(workDir, runProcess.start());
	}

	private static final class UnixProcess extends AbstractNodeProcess {

		private static final Logger log = LoggerFactory.getLogger(UnixProcess.class);

		private final Path workingDirectory;

		private final ProcessId processId;

		private UnixProcess(Path workingDirectory, ProcessId processId) {
			super(processId);
			this.workingDirectory = workingDirectory;
			this.processId = processId;
		}

		@Override
		void doStop() throws IOException, InterruptedException {
			Process process = this.processId.getProcess();
			long pid = getPid();
			if (pid <= 0) {
				process.destroy();
				return;
			}
			if (kill(pid) == 0) {
				if (!process.waitFor(5, TimeUnit.SECONDS)) {
					if (sigkill(pid) == 0) {
						if (!process.waitFor(5, TimeUnit.SECONDS)) {
							process.destroyForcibly();
						}
					}
				}
			}
			else {
				process.destroy();
			}
		}

		private int kill(long pid) throws InterruptedException, IOException {
			return new RunProcess(this.workingDirectory, "kill", "-SIGINT", pid).run(log::info);
		}

		private int sigkill(long pid) throws InterruptedException, IOException {
			return new RunProcess(this.workingDirectory, "kill", "-SIGKILL", pid).run(log::info);
		}

	}

}
