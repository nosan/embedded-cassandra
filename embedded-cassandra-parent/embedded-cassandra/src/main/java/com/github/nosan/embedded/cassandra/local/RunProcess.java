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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.util.MDCUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Utility class to run a {@link Process}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class RunProcess {

	private static final Logger log = LoggerFactory.getLogger(RunProcess.class);

	private final ProcessBuilder processBuilder;

	private final ThreadFactory threadFactory;

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param processBuilder {@link ProcessBuilder} to create {@link Process}
	 */
	RunProcess(ProcessBuilder processBuilder) {
		this(processBuilder, null);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param processBuilder {@link ProcessBuilder} to create a {@link Process}
	 * @param threadFactory {@link ThreadFactory} to create a {@link Thread}
	 */
	RunProcess(ProcessBuilder processBuilder, @Nullable ThreadFactory threadFactory) {
		this.processBuilder = processBuilder;
		this.threadFactory = (threadFactory != null) ? threadFactory : (runnable) -> {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		};
	}

	/**
	 * Starts a new process.
	 *
	 * @param consumer output consumer.
	 * @return a new process
	 * @throws IOException if an I/O error occurs
	 * @see CompositeConsumer
	 */
	Process run(Consumer<? super String> consumer) throws IOException {
		ProcessBuilder builder = this.processBuilder;
		if (log.isDebugEnabled()) {
			String message = String.format("Execute '%s' within a directory '%s'", String.join(" ", builder.command()),
					builder.directory());
			log.debug(message);
		}
		return start(this.processBuilder, this.threadFactory, consumer);
	}

	private static Process start(ProcessBuilder builder, ThreadFactory threadFactory, Consumer<? super String> consumer)
			throws IOException {
		Process process = builder.start();
		Map<String, String> context = MDCUtils.getContext();
		threadFactory.newThread(() -> {
			MDCUtils.setContext(context);
			read(process, consumer);
		}).start();
		return process;
	}

	private static void read(Process process, Consumer<? super String> consumer) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = readline(reader)) != null) {
				if (StringUtils.hasText(line)) {
					try {
						consumer.accept(line);
					}
					catch (Throwable ex) {
						if (log.isDebugEnabled()) {
							log.error(String.format("Line '%s' is not handled by consumer '%s'", line, consumer), ex);
						}
					}
				}
			}
		}
		catch (IOException ex) {
			log.error(String.format("Could not create a stream for '%s'", process), ex);
		}
	}

	@Nullable
	private static String readline(BufferedReader reader) {
		try {
			return reader.readLine();
		}
		catch (IOException ex) {
			//stream closed. nothing special
			return null;
		}
	}

}
