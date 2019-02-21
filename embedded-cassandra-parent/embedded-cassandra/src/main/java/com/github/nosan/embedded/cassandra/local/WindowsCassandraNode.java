/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.ProcessUtils;

/**
 * Windows implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
class WindowsCassandraNode extends AbstractCassandraNode {

	@Nonnull
	private final Path pidFile;

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
	WindowsCassandraNode(@Nonnull Path workingDirectory,
			@Nonnull Version version, @Nonnull Duration timeout,
			@Nonnull List<String> jvmOptions, @Nullable Path javaHome, int jmxPort) {
		super(workingDirectory, version, timeout, jvmOptions, javaHome, jmxPort);
		this.pidFile = workingDirectory.resolve(String.format("%s.pid", UUID.randomUUID()));
	}

	@Nonnull
	@Override
	protected Process start(@Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException {
		Files.deleteIfExists(this.pidFile);
		List<Object> arguments = new ArrayList<>();
		arguments.add("powershell");
		arguments.add("-ExecutionPolicy");
		arguments.add("Unrestricted");
		arguments.add(workingDirectory.resolve("bin/cassandra.ps1").toAbsolutePath());
		arguments.add("-f");
		if (version.getMajor() > 2 || (version.getMajor() == 2 && version.getMinor() > 1)) {
			arguments.add("-a");
		}
		arguments.add("-p");
		arguments.add(this.pidFile.toAbsolutePath());
		return new RunProcess(workingDirectory, environment, threadFactory, arguments)
				.run(outputs);
	}

	@Override
	protected void stop(@Nonnull Process process, @Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException {
		stop(process, workingDirectory, environment, threadFactory, this.pidFile, false, outputs);
	}

	@Override
	protected void forceStop(@Nonnull Process process, @Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException {
		stop(process, workingDirectory, environment, threadFactory, this.pidFile, true, outputs);
	}

	private static void stop(Process process, Path workingDirectory, Map<String, String> environment,
			ThreadFactory threadFactory, Path pidFile, boolean force, RunProcess.Output... outputs) throws IOException {
		long pid = ProcessUtils.getPid(process);
		if (Files.exists(pidFile)) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
			arguments.add(workingDirectory.resolve("bin/stop-server.ps1").toAbsolutePath());
			if (force) {
				arguments.add("-f");
			}
			arguments.add("-p");
			arguments.add(pidFile.toAbsolutePath());
			new RunProcess(workingDirectory, environment, threadFactory, arguments).run(outputs);
		}
		else if (pid > 0) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("taskkill");
			if (force) {
				arguments.add("/F");
			}
			arguments.add("/T");
			arguments.add("/pid");
			arguments.add(pid);
			new RunProcess(workingDirectory, environment, threadFactory, arguments).run(outputs);
		}
	}

}
