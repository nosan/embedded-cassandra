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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.StringUtils;
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
	protected ProcessId start(ProcessBuilder processBuilder, ThreadFactory threadFactory,
			Consumer<? super String> consumer) throws IOException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		Path pidFile = workingDirectory.resolve(UUID.randomUUID().toString());
		this.pidFile = pidFile;
		processBuilder.command("powershell", "-ExecutionPolicy", "Unrestricted");
		processBuilder.command().add(workingDirectory.resolve("bin/cassandra.ps1").toAbsolutePath().toString());
		processBuilder.command().add("-f");
		if (version.getMajor() > 2 || (version.getMajor() == 2 && version.getMinor() > 1)) {
			processBuilder.command().add("-a");
		}
		processBuilder.command().add("-p");
		processBuilder.command().add(pidFile.toAbsolutePath().toString());

		CountDownLatch latch = new CountDownLatch(1);

		CompositeConsumer<String> compositeConsumer = new CompositeConsumer<>();
		compositeConsumer.add(new Consumer<String>() {

			@Override
			public void accept(String line) {
				latch.countDown();
				compositeConsumer.remove(this);
			}
		});
		compositeConsumer.add(consumer);

		Process process = new RunProcess(processBuilder, threadFactory).run(compositeConsumer);
		try {
			latch.await(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return new ProcessId(process, getPid(process, pidFile));
	}

	@Override
	protected void stop(ProcessId processId, ProcessBuilder processBuilder, ThreadFactory threadFactory,
			Consumer<? super String> consumer) throws IOException {
		long id = processId.getPid();
		Process process = processId.getProcess();
		Path pidFile = this.pidFile;
		Path stopServerFile = this.workingDirectory.resolve("bin/stop-server.ps1");
		if (pidFile != null && Files.exists(pidFile) && Files.exists(stopServerFile)) {
			processBuilder.command("powershell", "-ExecutionPolicy", "Unrestricted");
			processBuilder.command().add(stopServerFile.toAbsolutePath().toString());
			processBuilder.command().add("-p");
			processBuilder.command().add(pidFile.toAbsolutePath().toString());
			new RunProcess(processBuilder, threadFactory).run(consumer);
		}
		else if (id != -1) {
			processBuilder.command("taskkill", "/pid", Long.toString(id));
			new RunProcess(processBuilder, threadFactory).run(consumer);
		}
		else {
			process.destroy();
		}
	}

	private static long getPid(Process process, Path pidFile) {
		long pid = ProcessUtils.getPid(process);
		if (pid != -1) {
			return pid;
		}
		return getPid(pidFile);
	}

	private static long getPid(Path pidFile) {
		if (!Files.exists(pidFile)) {
			return -1;
		}
		try {
			String id = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8)
					.replaceAll("\\D", "");
			return StringUtils.hasText(id) ? Long.parseLong(id) : -1;
		}
		catch (Throwable ex) {
			return -1;
		}
	}

}
