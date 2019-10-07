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

import com.github.nosan.embedded.cassandra.api.Version;

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
	Process doStart(RunProcess runProcess) throws IOException {
		Path executableFile = this.workingDirectory.resolve("bin/cassandra");
		if (!Files.isExecutable(executableFile)) {
			executableFile.toFile().setExecutable(true);
		}
		runProcess.setArguments(executableFile, "-f");
		if (this.rootAllowed && this.version.compareTo(Version.of("3.1")) > 0) {
			runProcess.addArguments("-R");
		}
		return runProcess.start();
	}

	@Override
	void doStop(Process process, long pid) throws IOException, InterruptedException {
		if (pid > 0 && kill(pid) == 0) {
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				sigkill(pid);
			}
		}
		else {
			process.destroy();
		}
	}

	private int kill(long pid) throws InterruptedException, IOException {
		return new RunProcess(this.workingDirectory, "kill", "-SIGINT", pid).run(this.logger::info);
	}

	private void sigkill(long pid) throws InterruptedException, IOException {
		new RunProcess(this.workingDirectory, "kill", "-SIGKILL", pid).run(this.logger::info);
	}

}
