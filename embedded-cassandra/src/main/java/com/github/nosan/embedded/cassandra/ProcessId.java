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

package com.github.nosan.embedded.cassandra;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * Representation of {@link Process} and its PID.
 *
 * @author Dmytro Nosan
 */
class ProcessId {

	@Nullable
	private static final Method PID_METHOD;

	static {
		Method method;
		try {
			method = Process.class.getMethod("pid");
		}
		catch (NoSuchMethodException ex) {
			method = null;
		}
		PID_METHOD = method;
	}

	private final Process process;

	private final long pid;

	/**
	 * Constructs a new {@link ProcessId} with the specified process.
	 *
	 * @param process the process
	 */
	ProcessId(Process process) {
		this.process = process;
		this.pid = getPid(process);
	}

	/**
	 * Constructs a new {@link ProcessId} with the specified process and its pid.
	 *
	 * @param process the process
	 * @param pid the PID
	 */
	ProcessId(Process process, long pid) {
		this.process = process;
		this.pid = (pid > 0) ? pid : -1;
	}

	/**
	 * Returns the {@link Process}.
	 *
	 * @return the process
	 */
	Process getProcess() {
		return this.process;
	}

	/**
	 * Returns the pid.
	 *
	 * @return the pid (or -1 if none)
	 */
	long getPid() {
		return this.pid;
	}

	@Override
	public String toString() {
		return String.format("%s [%d]", this.process, this.pid);
	}

	private static long getPid(Process process) {
		try {
			if (PID_METHOD != null) {
				return (long) PID_METHOD.invoke(process);
			}
			Field field = process.getClass().getDeclaredField("pid");
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return (int) field.get(process);
		}
		catch (NoSuchFieldException ex) {
			return -1;
		}
		catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
