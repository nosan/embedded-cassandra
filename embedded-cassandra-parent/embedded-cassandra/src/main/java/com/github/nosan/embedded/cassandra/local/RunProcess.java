/*
 * Copyright 2018-2018 the original author or authors.
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


	/**
	 * Creates new {@link RunProcess} instance.
	 *
	 * @param arguments the program to execute and its arguments
	 */
	RunProcess(@Nonnull List<?> arguments) {
		this(null, null, arguments);
	}

	/**
	 * Creates new {@link RunProcess} instance.
	 *
	 * @param arguments the program to execute and its arguments
	 * @param environment the environment variables
	 */
	RunProcess(@Nullable Map<String, String> environment, @Nonnull List<?> arguments) {
		this(null, environment, arguments);
	}

	/**
	 * Creates new {@link RunProcess} instance.
	 *
	 * @param workingDirectory the working directory of the child process
	 * @param arguments the program to execute and its arguments
	 */
	RunProcess(@Nullable Path workingDirectory, @Nonnull List<?> arguments) {
		this(workingDirectory, null, arguments);
	}

	/**
	 * Creates new {@link RunProcess} instance.
	 *
	 * @param workingDirectory the working directory of the child process
	 * @param arguments the program to execute and its arguments
	 * @param environment the environment variables
	 */
	RunProcess(@Nullable Path workingDirectory, @Nullable Map<String, String> environment,
			@Nonnull List<?> arguments) {
		Objects.requireNonNull(arguments, "Arguments must not be null");
		this.workingDirectory = workingDirectory;
		this.arguments = new ArrayList<>(arguments);
		this.environment = (environment != null) ? new LinkedHashMap<>(environment) : Collections.emptyMap();
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
	 */
	int runAndWait(@Nullable Output... outputs) throws IOException {
		try {
			return run(outputs).waitFor();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return -1;
		}
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
				message.append(String.format(" and directory (%s)", workingDirectory));
			}
			log.debug(message.toString());
		}
		return start(processBuilder, outputs);
	}

	private static Process start(ProcessBuilder builder, @Nullable Output[] outputs) throws IOException {
		final class Handler {
			@Nullable
			private Process process;

			@Nullable
			private IOException exception;
		}
		Handler handler = new Handler();
		CountDownLatch latch = new CountDownLatch(1);
		new Thread(() -> {
			try {
				handler.process = builder.start();
			}
			catch (IOException ex) {
				handler.exception = ex;
			}
			finally {
				latch.countDown();
			}
			Process process = handler.process;
			if (process != null && outputs != null && outputs.length > 0) {
				read(process, outputs);
			}
		}, String.format("%s:cassandra", Thread.currentThread().getName())).start();

		try {
			latch.await();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		if (handler.process != null) {
			return handler.process;
		}
		if (handler.exception != null) {
			throw handler.exception;
		}

		throw new IllegalStateException("Both 'Handler.process' and 'Handler.exception' fields are null");
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
