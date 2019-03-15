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

import java.util.function.Supplier;

/**
 * Util class that contains {@link Process} and pid.
 *
 * @author Dmytro Nosan
 */
class ProcessId {

	private final Process process;

	private final Supplier<Long> pid;

	/**
	 * Creates {@link ProcessId}.
	 *
	 * @param process the process
	 * @param pid the pid, or (-1)
	 * @see ProcessUtils#getPid(Process)
	 */
	ProcessId(Process process, long pid) {
		this(process, () -> pid);
	}

	/**
	 * Creates {@link ProcessId}.
	 *
	 * @param process the process
	 * @param pid the pid supplier
	 * @see ProcessUtils#getPid(Process)
	 */
	ProcessId(Process process, Supplier<Long> pid) {
		this.process = process;
		this.pid = pid;
	}

	/**
	 * Return the process.
	 *
	 * @return the process
	 */
	Process getProcess() {
		return this.process;
	}

	/**
	 * Return the pid.
	 *
	 * @return the pid (or -1)
	 */
	Supplier<Long> getPid() {
		return this.pid;
	}

}
