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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Version;

/**
 * UNIX implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
class UnixCassandraNode extends AbstractCassandraNode {

	private final boolean allowRoot;

	private long pid;

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
	UnixCassandraNode(@Nonnull Path workingDirectory, @Nonnull Version version, @Nonnull Duration timeout,
			@Nonnull List<String> jvmOptions, @Nullable Path javaHome, int jmxPort, boolean allowRoot) {
		super(workingDirectory, version, timeout, jvmOptions, javaHome, jmxPort);
		this.allowRoot = allowRoot;
	}

	@Nonnull
	@Override
	protected Process start(@Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException {
		this.pid = -1;
		List<Object> arguments = new ArrayList<>();
		arguments.add(workingDirectory.resolve("bin/cassandra").toAbsolutePath());
		arguments.add("-f");
		if (this.allowRoot && (version.getMajor() > 3 || (version.getMajor() == 3 && version.getMinor() > 1))) {
			arguments.add("-R");
		}
		Process process = new RunProcess(workingDirectory, environment, threadFactory, arguments)
				.run(outputs);
		this.pid = ProcessUtils.getPid(process);
		return process;
	}

	@Override
	protected void stop(@Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException {
		new RunProcess(workingDirectory, environment, threadFactory, Arrays.asList("kill", "-SIGINT", getId()))
				.run(outputs);
	}

	@Override
	protected void forceStop(@Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException {
		new RunProcess(workingDirectory, environment, threadFactory, Arrays.asList("kill", "-9", getId()))
				.run(outputs);
	}

	@Nonnull
	@Override
	protected Long getId() {
		return this.pid;
	}

}
