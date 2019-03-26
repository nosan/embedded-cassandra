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
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
		CompositeConsumer<String> compositeConsumer = new CompositeConsumer<>();
		PidFileSupplier pidFileSupplier = new PidFileSupplier(pidFile, compositeConsumer);
		compositeConsumer.add(pidFileSupplier);
		compositeConsumer.add(consumer);
		Process process = new RunProcess(builder, threadFactory).run(compositeConsumer);
		return new ProcessId(process, new PidSupplier(ProcessUtils.getPid(process), pidFileSupplier));
	}

	@Override
	protected boolean terminate(long pid, ProcessBuilder builder, ThreadFactory threadFactory,
			Consumer<String> consumer) throws IOException, InterruptedException {
		return terminateByPidFile(builder, threadFactory, consumer) || terminateByPid(pid, builder, threadFactory,
				consumer);
	}

	@Override
	protected boolean kill(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPidFile(builder, threadFactory, consumer) || killByPid(pid, builder, threadFactory, consumer);
	}

	private boolean terminateByPidFile(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPidFile(builder, threadFactory, consumer, false);
	}

	private boolean terminateByPid(long pid, ProcessBuilder builder, ThreadFactory threadFactory,
			Consumer<String> consumer) throws IOException, InterruptedException {
		return killByPid(pid, builder, threadFactory, consumer, false);
	}

	private boolean killByPid(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPid(pid, builder, threadFactory, consumer, true);

	}

	private boolean killByPidFile(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		return killByPidFile(builder, threadFactory, consumer, true);

	}

	private boolean killByPidFile(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer,
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
			return new RunProcess(builder, threadFactory).run(consumer).waitFor() == 0;
		}
		return false;
	}

	private boolean killByPid(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer,
			boolean force) throws IOException, InterruptedException {
		if (pid != -1) {
			builder.command("taskkill");
			if (force) {
				builder.command().add("/f");
			}
			builder.command().add("/pid");
			builder.command().add(Long.toString(pid));
			return new RunProcess(builder, threadFactory).run(consumer).waitFor() == 0;
		}
		return false;
	}

	private static final class PidSupplier implements Supplier<Long> {

		private final PidFileSupplier supplier;

		private final long pid;

		PidSupplier(long pid, PidFileSupplier supplier) {
			this.pid = pid;
			this.supplier = supplier;
		}

		@Override
		public Long get() {
			long pid = this.pid;
			if (pid == -1) {
				return this.supplier.get();
			}
			return pid;
		}

	}

	private static final class PidFileSupplier implements Supplier<Long>, Consumer<String> {

		private static final int DEFAULT_STARTUP_TIMEOUT = 20000;

		private final Path pidFile;

		private final CompositeConsumer<String> consumer;

		private final long start;

		private volatile long pid = -1;

		PidFileSupplier(Path pidFile, CompositeConsumer<String> consumer) {
			this.pidFile = pidFile;
			this.consumer = consumer;
			this.start = System.currentTimeMillis();
		}

		@Override
		public void accept(String line) {
			if (this.pid == -1) {
				this.pid = getPid(this.pidFile);
			}
			long elapsed = System.currentTimeMillis() - this.start;
			if (elapsed > DEFAULT_STARTUP_TIMEOUT || this.pid != -1) {
				this.consumer.remove(this);
			}
		}

		@Override
		public Long get() {
			return this.pid;
		}

		private static long getPid(Path pidFile) {
			if (Files.exists(pidFile)) {
				try {
					String id = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8).replaceAll("\\D", "");
					return StringUtils.hasText(id) ? Long.parseLong(id) : -1;
				}
				catch (Throwable ex) {
					return -1;
				}
			}
			return -1;
		}

	}

}
