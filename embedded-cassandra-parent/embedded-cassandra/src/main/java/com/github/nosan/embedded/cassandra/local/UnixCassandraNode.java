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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Unix based {@link CassandraNode} implementation.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class UnixCassandraNode extends AbstractCassandraNode {

	private final Version version;

	private final Path workingDirectory;

	private final boolean allowRoot;

	UnixCassandraNode(Version version, Path workingDirectory, @Nullable Path javaHome,
			Ports ports, List<String> jvmOptions, boolean allowRoot) {
		super(version, javaHome, ports, jvmOptions);
		this.version = version;
		this.workingDirectory = workingDirectory;
		this.allowRoot = allowRoot;
	}

	@Override
	protected ProcessId start(Map<String, String> environment) throws IOException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		ProcessBuilder builder = new ProcessBuilder().directory(workingDirectory.toFile())
				.redirectErrorStream(true);
		builder.environment().putAll(environment);
		builder.command(workingDirectory.resolve("bin/cassandra").toString(), "-f");
		if (this.allowRoot && (version.getMajor() > 3 || (version.getMajor() == 3 && version.getMinor() > 1))) {
			builder.command().add("-R");
		}
		return new ProcessId(new RunProcess(builder).run());
	}

	@Override
	protected void stop(ProcessId processId, Map<String, String> environment) throws InterruptedException {
		ProcessBuilder builder = new ProcessBuilder().directory(this.workingDirectory.toFile())
				.redirectErrorStream(true);
		builder.environment().putAll(environment);
		Process process = processId.getProcess();
		long pid = processId.getPid();
		if (terminate(pid, builder) != 0) {
			process.destroy();
		}
		if (!process.waitFor(5, TimeUnit.SECONDS)) {
			if (kill(pid, builder) != 0) {
				process.destroy();
			}
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				process.destroyForcibly();
			}
		}
	}

	private int terminate(long pid, ProcessBuilder builder) throws InterruptedException {
		if (pid != -1) {
			return new RunProcess(builder.command("kill", Long.toString(pid))).runAndWait();
		}
		return -1;
	}

	private int kill(long pid, ProcessBuilder builder) throws InterruptedException {
		if (pid != -1) {
			return new RunProcess(builder.command("kill", "-SIGINT", Long.toString(pid))).runAndWait();
		}
		return -1;
	}

}
