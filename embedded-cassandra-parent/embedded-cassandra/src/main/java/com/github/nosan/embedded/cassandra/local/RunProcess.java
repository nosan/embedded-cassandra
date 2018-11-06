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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
		ProcessBuilder processBuilder =
				new ProcessBuilder(this.arguments.stream().map(String::valueOf).collect(Collectors.toList()));
		if (this.workingDirectory != null) {
			processBuilder.directory(this.workingDirectory.toFile());
		}
		if (!this.environment.isEmpty()) {
			processBuilder.environment().putAll(this.environment);
		}
		processBuilder.redirectErrorStream(true);
		log.debug("Execute ({}) with environment ({}) in a ({})", this.arguments, this.environment,
				this.workingDirectory);
		Process process = processBuilder.start();
		if (outputs != null && outputs.length > 0) {
			String name = Thread.currentThread().getName();
			Thread thread = new Thread(new ProcessReader(process, outputs), String.format("%s:Cassandra", name));
			thread.setDaemon(true);
			thread.start();
		}
		return process;
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


	private static final class ProcessReader implements Runnable {

		@Nonnull
		private final List<Output> targets;

		@Nonnull
		private final Process process;


		/**
		 * Creates a {@link ProcessReader}.
		 *
		 * @param process the process
		 * @param targets the output consumers
		 */
		ProcessReader(@Nonnull Process process, @Nonnull Output... targets) {
			this.process = process;
			this.targets = Arrays.asList(targets);
		}

		@Override
		public void run() {
			InputStream stream = this.process.getInputStream();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 128)) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (StringUtils.hasText(line)) {
						for (Output output : this.targets) {
							output.accept(line);
						}
					}
				}
			}
			catch (IOException ignore) {
				//stream closed. nothing special
			}
		}
	}


}
