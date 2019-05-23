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
import java.time.Duration;
import java.util.Map;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Unix based {@link CassandraNode} implementation.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class UnixCassandraNode extends AbstractCassandraNode {

	private final boolean allowRoot;

	UnixCassandraNode(Path workingDirectory, Version version, Duration startupTimeout, boolean daemon,
			@Nullable Path javaHome, JvmParameters jvmParameters, boolean allowRoot) {
		super(workingDirectory, version, startupTimeout, daemon, javaHome, jvmParameters);
		this.allowRoot = allowRoot;
	}

	@Override
	ProcessId start(Map<String, String> environment) throws IOException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		ProcessBuilder builder = newBuilder();
		builder.environment().putAll(environment);
		builder.command(workingDirectory.resolve("bin/cassandra").toString(), "-f");
		if (this.allowRoot && (version.getMajor() > 3 || (version.getMajor() == 3 && version.getMinor() > 1))) {
			builder.command().add("-R");
		}
		return new ProcessId(new RunProcess(builder).run());
	}

	@Override
	int terminate(ProcessId processId) throws InterruptedException {
		long pid = processId.getPid();
		if (pid != -1) {
			return new RunProcess(newBuilder().command("kill", Long.toString(pid)))
					.runAndWait(this.threadFactory, this.log::info);
		}
		return -1;
	}

	@Override
	int kill(ProcessId processId) throws InterruptedException {
		long pid = processId.getPid();
		if (pid != -1) {
			return new RunProcess(newBuilder().command("kill", "-SIGKILL", Long.toString(pid)))
					.runAndWait(this.threadFactory, this.log::info);
		}
		return -1;
	}

	private ProcessBuilder newBuilder() {
		return new ProcessBuilder().directory(this.workingDirectory.toFile()).redirectErrorStream(true);
	}

}
