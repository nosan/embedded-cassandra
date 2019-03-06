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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Windows implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
class WindowsCassandraNode extends AbstractCassandraNode {

	private final Path pidFile;

	@Nullable
	private volatile Long pid;

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
	WindowsCassandraNode(Path workingDirectory,
			Version version, Duration timeout,
			List<String> jvmOptions, @Nullable Path javaHome, int jmxPort) {
		super(workingDirectory, version, timeout, jvmOptions, javaHome, jmxPort);
		this.pidFile = workingDirectory.resolve(String.format("%s.pid", UUID.randomUUID()));
	}

	@Override
	protected Process start(Path workingDirectory, Version version,
			Map<String, String> environment, ThreadFactory threadFactory,
			RunProcess.Output... outputs) throws IOException {
		Files.deleteIfExists(this.pidFile);
		this.pid = null;
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
		Process process = new RunProcess(workingDirectory, environment, threadFactory, arguments)
				.run(append(line -> {
					Long pid = this.pid;
					if (pid != null && pid == -1) {
						this.pid = getPid(this.pidFile);
					}
				}, outputs));
		this.pid = ProcessUtils.getPid(process);
		return process;
	}

	@Override
	protected void stop(Path workingDirectory, Version version,
			Map<String, String> environment, ThreadFactory threadFactory,
			RunProcess.Output... outputs) throws IOException {
		stop(workingDirectory, environment, threadFactory, false, outputs);
	}

	@Override
	protected void forceStop(Path workingDirectory, Version version,
			Map<String, String> environment, ThreadFactory threadFactory,
			RunProcess.Output... outputs) throws IOException {
		stop(workingDirectory, environment, threadFactory, true, outputs);
	}

	@Override
	protected Long getId() {
		Long pid = this.pid;
		return (pid != null) ? pid : -1;
	}

	private void stop(Path workingDirectory, Map<String, String> environment,
			ThreadFactory threadFactory, boolean force, RunProcess.Output... outputs) throws IOException {
		Path pidFile = this.pidFile;
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
		else {
			List<Object> arguments = new ArrayList<>();
			arguments.add("taskkill");
			if (force) {
				arguments.add("/f");
			}
			arguments.add("/t");
			arguments.add("/pid");
			arguments.add(getId());
			new RunProcess(workingDirectory, environment, threadFactory, arguments).run(outputs);
		}
	}

	private static RunProcess.Output[] append(RunProcess.Output element, RunProcess.Output[] elements) {
		List<RunProcess.Output> outputs = new ArrayList<>();
		outputs.add(element);
		outputs.addAll(Arrays.asList(elements));
		return outputs.toArray(new RunProcess.Output[0]);
	}

	private static long getPid(Path pidFile) {
		if (!Files.exists(pidFile)) {
			return -1;
		}
		try {
			String id = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8)
					.replaceAll("[^\\d]", "");
			return StringUtils.hasText(id) ? Long.parseLong(id) : -1;
		}
		catch (Throwable ex) {
			return -1;
		}
	}

}
