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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.nosan.embedded.cassandra.lang.Nullable;

/**
 * Utility class for dealing with {@link Process}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class ProcessUtils {

	@Nullable
	private static final Method PID_METHOD;

	static {
		Method method = null;
		try {
			//java >= 9
			method = Process.class.getMethod("pid");
		}
		catch (Throwable ignore) {
		}
		PID_METHOD = method;
	}

	/**
	 * Returns the {@link Process} pid.
	 *
	 * @param process a {@link Process}
	 * @return the pid (or {@code -1})
	 */
	static long getPid(Process process) {
		try {
			if (PID_METHOD != null) {
				return Long.parseLong(String.valueOf(PID_METHOD.invoke(process)));
			}
			Field field = process.getClass().getDeclaredField("pid");
			field.setAccessible(true);
			return Long.parseLong(String.valueOf(field.get(process)));
		}
		catch (Throwable ex) {
			return -1;
		}
	}

}
