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
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Windows implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
class WindowsCassandraNode extends AbstractCassandraNode {

	private final Path workingDirectory;

	private final Version version;

	@Nullable
	private Path pidFile;

	/**
	 * Creates a {@link WindowsCassandraNode}.
	 *
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param timeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param javaHome java home directory
	 * @param jmxPort JMX port
	 */
	WindowsCassandraNode(Path workingDirectory, Version version, Duration timeout, List<String> jvmOptions,
			@Nullable Path javaHome, int jmxPort) {
		super(workingDirectory, version, timeout, jvmOptions, javaHome, jmxPort);
		this.workingDirectory = workingDirectory;
		this.version = version;
	}

	@Override
	protected ProcessId start(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		Path pidFile = workingDirectory.resolve(UUID.randomUUID().toString());
		this.pidFile = pidFile;
		builder.command("powershell", "-ExecutionPolicy", "Unrestricted",
				workingDirectory.resolve("bin/cassandra.ps1").toString(), "-f");
		if (version.getMajor() > 2 || (version.getMajor() == 2 && version.getMinor() > 1)) {
			builder.command().add("-a");
		}
		builder.command().add("-p");
		builder.command().add(pidFile.toString());
		Process process = new RunProcess(builder, threadFactory).run(consumer);
		return new ProcessId(process, pidFile);
	}

	@Override
	protected int terminate(long pid, ProcessBuilder builder, ThreadFactory threadFactory,
			Consumer<String> consumer) throws IOException, InterruptedException {
		int exitCode = terminateByPidFile(builder, threadFactory, consumer);
		if (exitCode != 0) {
			return terminateByPid(pid, builder, threadFactory, consumer);
		}
		return exitCode;
	}

	@Override
	protected int kill(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		int exitCode = killByPidFile(builder, threadFactory, consumer);
		if (exitCode != 0) {
			return killByPid(pid, builder, threadFactory, consumer);
		}
		return exitCode;
	}

	private int terminateByPidFile(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPidFile(builder, threadFactory, consumer, false);
	}

	private int terminateByPid(long pid, ProcessBuilder builder, ThreadFactory threadFactory,
			Consumer<String> consumer) throws IOException, InterruptedException {
		return killByPid(pid, builder, threadFactory, consumer, false);
	}

	private int killByPid(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPid(pid, builder, threadFactory, consumer, true);

	}

	private int killByPidFile(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPidFile(builder, threadFactory, consumer, true);

	}

	private int killByPidFile(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer,
			boolean force) throws IOException, InterruptedException {
		Path pidFile = this.pidFile;
		Path stopServerFile = this.workingDirectory.resolve("bin/stop-server.ps1");
		if (pidFile != null && Files.exists(pidFile) && Files.exists(stopServerFile)) {
			builder.command("powershell", "-ExecutionPolicy", "Unrestricted");
			builder.command().add(stopServerFile.toString());
			builder.command().add("-p");
			builder.command().add(pidFile.toString());
			if (force) {
				builder.command().add("-f");
			}
			return new RunProcess(builder, threadFactory).run(consumer).waitFor();
		}
		return -1;
	}

	private int killByPid(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer,
			boolean force) throws IOException, InterruptedException {
		if (pid != -1) {
			builder.command("taskkill");
			if (force) {
				builder.command().add("/f");
			}
			builder.command().add("/pid");
			builder.command().add(Long.toString(pid));
			return new RunProcess(builder, threadFactory).run(consumer).waitFor();
		}
		return -1;
	}

}
