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
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * UNIX implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
class UnixCassandraNode extends AbstractCassandraNode {

	private final Path workingDirectory;

	private final Version version;

	private final boolean allowRoot;

	/**
	 * Creates a {@link UnixCassandraNode}.
	 *
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param timeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param javaHome java home directory
	 * @param jmxPort JMX port
	 * @param allowRoot allow running as a root
	 */
	UnixCassandraNode(Path workingDirectory, Version version, Duration timeout, List<String> jvmOptions,
			@Nullable Path javaHome, int jmxPort, boolean allowRoot) {
		super(workingDirectory, version, timeout, jvmOptions, javaHome, jmxPort);
		this.workingDirectory = workingDirectory;
		this.version = version;
		this.allowRoot = allowRoot;
	}

	@Override
	protected ProcessId start(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		builder.command(workingDirectory.resolve("bin/cassandra").toString(), "-f");
		if (this.allowRoot && (version.getMajor() > 3 || (version.getMajor() == 3 && version.getMinor() > 1))) {
			builder.command().add("-R");
		}
		Process process = new RunProcess(builder, threadFactory).run(consumer);
		return new ProcessId(process, ProcessUtils.getPid(process));
	}

	@Override
	protected boolean terminate(long pid, ProcessBuilder builder, ThreadFactory threadFactory,
			Consumer<String> consumer) throws IOException, InterruptedException {
		if (pid != -1) {
			return new RunProcess(builder.command("kill", Long.toString(pid)), threadFactory).run(consumer).waitFor()
					== 0;
		}
		return false;
	}

	@Override
	protected boolean kill(long pid, ProcessBuilder builder, ThreadFactory threadFactory, Consumer<String> consumer)
			throws IOException, InterruptedException {
		if (pid != -1) {
			return new RunProcess(builder.command("kill", "-SIGKILL", Long.toString(pid)), threadFactory).run(consumer)
					.waitFor() == 0;
		}
		return false;
	}

}
