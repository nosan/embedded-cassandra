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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.util.MDCUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Utility class to run a {@code Process}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class RunProcess {

	private static final Logger log = LoggerFactory.getLogger(RunProcess.class);

	@Nullable
	private final Path workingDirectory;

	@Nonnull
	private final List<Object> arguments;

	@Nonnull
	private final Map<String, String> environment;

	private final boolean daemon;

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param arguments the program to execute and its arguments
	 */
	RunProcess(@Nonnull List<?> arguments) {
		this(false, null, null, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param arguments the program to execute and its arguments
	 * @param daemon whether process is daemon or not
	 */
	RunProcess(boolean daemon, @Nonnull List<?> arguments) {
		this(daemon, null, null, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param arguments the program to execute and its arguments
	 * @param environment the environment variables
	 */
	RunProcess(@Nullable Map<String, String> environment, @Nonnull List<?> arguments) {
		this(false, null, environment, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param arguments the program to execute and its arguments
	 * @param environment the environment variables
	 * @param daemon whether process is daemon or not
	 */
	RunProcess(boolean daemon, @Nullable Map<String, String> environment, @Nonnull List<?> arguments) {
		this(daemon, null, environment, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param workingDirectory the working directory of the child process
	 * @param arguments the program to execute and its arguments
	 */
	RunProcess(@Nullable Path workingDirectory, @Nonnull List<?> arguments) {
		this(false, workingDirectory, null, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param workingDirectory the working directory of the child process
	 * @param arguments the program to execute and its arguments
	 * @param daemon whether process is daemon or not
	 */
	RunProcess(boolean daemon, @Nullable Path workingDirectory, @Nonnull List<?> arguments) {
		this(daemon, workingDirectory, null, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param workingDirectory the working directory of the child process
	 * @param arguments the program to execute and its arguments
	 * @param environment the environment variables
	 */
	RunProcess(@Nullable Path workingDirectory, @Nullable Map<String, String> environment,
			@Nonnull List<?> arguments) {
		this(false, workingDirectory, environment, arguments);
	}

	/**
	 * Creates a new {@link RunProcess} instance.
	 *
	 * @param workingDirectory the working directory of the child process
	 * @param arguments the program to execute and its arguments
	 * @param environment the environment variables
	 * @param daemon whether process is daemon or not
	 */
	RunProcess(boolean daemon, @Nullable Path workingDirectory, @Nullable Map<String, String> environment,
			@Nonnull List<?> arguments) {
		Objects.requireNonNull(arguments, "Arguments must not be null");
		this.workingDirectory = workingDirectory;
		this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
		this.environment = Collections.unmodifiableMap((environment != null) ?
				new LinkedHashMap<>(environment) : Collections.emptyMap());
		this.daemon = daemon;
	}

	/**
	 * Starts a new process using the arguments and env variables and delegates output to the {@link Output}.
	 * Causes the current thread to wait, if necessary, until the process represented by process object has
	 * terminated.
	 *
	 * @param outputs output consumers.
	 * @return the exit value of the subprocess represented by {@code Process} object. By convention, the value
	 * {@code 0} indicates normal termination.
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another
	 * thread while it is waiting, then the wait is ended and an {@link InterruptedException} is thrown.
	 */
	int runAndWait(@Nullable Output... outputs) throws IOException, InterruptedException {
		return run(outputs).waitFor();
	}

	/**
	 * Starts a new process using the arguments and env variables and delegates output to the {@link Output}.
	 *
	 * @param outputs output consumers.
	 * @return a new process object for managing the subprocess
	 * @throws IOException if an I/O error occurs
	 */
	Process run(@Nullable Output... outputs) throws IOException {
		List<String> command = this.arguments.stream().map(String::valueOf).collect(Collectors.toList());
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Path workingDirectory = this.workingDirectory;
		if (workingDirectory != null) {
			processBuilder.directory(workingDirectory.toAbsolutePath().toFile());
		}
		Map<String, String> environment = this.environment;
		if (!environment.isEmpty()) {
			processBuilder.environment().putAll(environment);
		}
		processBuilder.redirectErrorStream(true);
		if (log.isDebugEnabled()) {
			StringBuilder message = new StringBuilder(String.format("Execute %s", command));
			if (!environment.isEmpty()) {
				message.append(String.format(" with environment %s", environment));
			}
			if (workingDirectory != null) {
				message.append(String.format(" and use a directory (%s)", workingDirectory));
			}
			log.debug(message.toString());
		}
		return start(processBuilder, this.daemon, outputs);
	}

	private static Process start(ProcessBuilder builder, boolean daemon, Output[] outputs) throws IOException {
		Process process = builder.start();
		if (outputs != null && outputs.length > 0) {
			CountDownLatch latch = new CountDownLatch(1);
			Map<String, String> context = MDCUtils.getContext();
			Thread thread = new Thread(() -> {
				MDCUtils.setContext(context);
				latch.countDown();
				read(process, outputs);
			}, Thread.currentThread().getName());
			thread.setDaemon(daemon);
			thread.start();
			try {
				latch.await(1, TimeUnit.SECONDS);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		return process;
	}

	private static void read(Process process, Output[] outputs) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = readline(reader)) != null) {
				if (StringUtils.hasText(line)) {
					for (Output output : outputs) {
						output.accept(line);
					}
				}
			}
		}
		catch (IOException ex) {
			log.error(String.format("Could not create a stream for (%s)", process), ex);
		}
	}

	private static String readline(BufferedReader reader) {
		try {
			return reader.readLine();
		}
		catch (IOException ex) {
			//stream closed. nothing special
			return null;
		}
	}

	/**
	 * Output consumer.
	 */
	@FunctionalInterface
	interface Output extends Consumer<String> {

		/**
		 * Consumes the given line.
		 *
		 * @param line a source line (never empty)
		 */
		@Override
		void accept(@Nonnull String line);
	}
}
