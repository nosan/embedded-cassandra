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

import java.nio.file.Path;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Utility class that contains {@link Process} information.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
class ProcessId {

	private final Process process;

	@Nullable
	private final Path pidFile;

	private long pid = -1;

	ProcessId(Process process) {
		this(process, null);
	}

	ProcessId(Process process, @Nullable Path pidFile) {
		this.process = process;
		this.pidFile = pidFile;
	}

	@Override
	public String toString() {
		return String.valueOf(getPid());
	}

	/**
	 * Returns the process.
	 *
	 * @return the process
	 */
	Process getProcess() {
		return this.process;
	}

	/**
	 * Returns the pid of the process.
	 *
	 * @return the pid (or -1)
	 */
	long getPid() {
		if (this.pid == -1) {
			this.pid = ProcessUtils.getPid(this.process);
		}
		if (this.pid == -1 && this.pidFile != null) {
			this.pid = ProcessUtils.getPid(this.pidFile);
		}
		return this.pid;
	}

	/**
	 * Returns the pid file.
	 *
	 * @return the pid file or null
	 */
	@Nullable
	Path getPidFile() {
		return this.pidFile;
	}

}
