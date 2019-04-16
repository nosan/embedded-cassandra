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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Utility class for dealing with a {@link Process}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class ProcessUtils {

	private static final Logger log = LoggerFactory.getLogger(ProcessUtils.class);

	@Nullable
	private static final Method PID_METHOD;

	static {
		Method method = null;
		try {
			method = Process.class.getMethod("pid");
		}
		catch (Throwable ex) {
			//ignore
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

	/**
	 * Returns the pid from a file.
	 *
	 * @param pidFile a pid file.
	 * @return the pid (or {@code -1})
	 * @since 2.0.0
	 */
	static long getPid(Path pidFile) {
		if (Files.exists(pidFile)) {
			try {
				String id = new String(Files.readAllBytes(pidFile), StandardCharsets.UTF_8)
						.replaceAll("\\D", "");
				return StringUtils.hasText(id) ? Long.parseLong(id) : -1;
			}
			catch (Throwable ex) {
				//ignore
			}
		}
		return -1;
	}

	/**
	 * Read the process output and write it to the consumer.
	 *
	 * @param process a process
	 * @param consumer a consumer
	 * @since 2.0.0
	 */
	static void read(Process process, Consumer<? super String> consumer) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = readline(reader)) != null) {
				if (StringUtils.hasText(line)) {
					try {
						consumer.accept(line);
					}
					catch (Exception ex) {
						log.error(String.format("'%s' is not handled by a consumer '%s'", line, consumer), ex);
					}
				}
			}
		}
		catch (IOException ex) {
			log.error(String.format("Can not create a stream for '%s'", process), ex);
		}
	}

	@Nullable
	private static String readline(BufferedReader reader) {
		try {
			return reader.readLine();
		}
		catch (IOException ex) {
			return null;
		}
	}

}
