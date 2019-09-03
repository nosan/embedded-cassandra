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

package com.github.nosan.embedded.cassandra.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * This class is used to create and run a {@link Process}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class RunProcess {

	private static final Logger log = LoggerFactory.getLogger(RunProcess.class);

	private static final ThreadFactory threadFactory = new MDCThreadFactory();

	private static final AtomicLong number = new AtomicLong();

	private final List<Object> arguments = new ArrayList<>();

	private final Map<String, Object> environment = new LinkedHashMap<>();

	@Nullable
	private Path workingDirectory;

	/**
	 * Constructs a {@link RunProcess} with the specified arguments.
	 *
	 * @param arguments the arguments
	 */
	public RunProcess(Object... arguments) {
		this(null, arguments);
	}

	/**
	 * Constructs a {@link RunProcess} with the specified arguments and working directory.
	 *
	 * @param arguments the arguments
	 * @param workingDirectory the working directory
	 */
	public RunProcess(@Nullable Path workingDirectory, Object... arguments) {
		Objects.requireNonNull(arguments, "'arguments' must not be null");
		this.workingDirectory = workingDirectory;
		this.arguments.addAll(Arrays.asList(arguments));
	}

	/**
	 * Returns arguments.
	 *
	 * @return the arguments
	 */
	public List<Object> getArguments() {
		return this.arguments;
	}

	/**
	 * Sets arguments. Setting this value will replace any previously configured arguments.
	 *
	 * @param arguments the arguments
	 */
	public void setArguments(Object... arguments) {
		Objects.requireNonNull(arguments, "'arguments' must not be null");
		this.arguments.clear();
		this.arguments.addAll(Arrays.asList(arguments));
	}

	/**
	 * Adds arguments.
	 *
	 * @param arguments the arguments
	 */
	public void addArguments(Object... arguments) {
		Objects.requireNonNull(arguments, "'arguments' must not be null");
		this.arguments.addAll(Arrays.asList(arguments));
	}

	/**
	 * Returns environment variables.
	 *
	 * @return the environment variables
	 */
	public Map<String, Object> getEnvironment() {
		return this.environment;
	}

	/**
	 * Puts environment variable.
	 *
	 * @param name the name of the env variable
	 * @param value the value of the env variable
	 */
	public void putEnvironment(String name, @Nullable Object value) {
		Objects.requireNonNull(name, "'name' must not be null");
		this.environment.put(name, value);
	}

	/**
	 * Returns the working directory.
	 *
	 * @return the working directory  (or null if none)
	 */
	@Nullable
	public Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	/**
	 * Sets the working directory.
	 *
	 * @param workingDirectory the working directory
	 */
	public void setWorkingDirectory(@Nullable Path workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Starts a new process.
	 *
	 * @return a new {@link ProcessId}
	 * @throws IOException if an I/O error occurs
	 * @see ProcessBuilder#start()
	 */
	public ProcessId start() throws IOException {
		Path workDir = this.workingDirectory;
		List<String> arguments = this.arguments.stream().filter(Objects::nonNull).map(Object::toString).filter(
				StringUtils::hasText).collect(Collectors.toList());
		ProcessBuilder builder = new ProcessBuilder(arguments).redirectErrorStream(true);
		if (workDir != null) {
			builder.directory(workDir.toFile());
		}
		Map<String, String> environment = this.environment.entrySet().stream().filter(
				entry -> Objects.nonNull(entry.getKey())).collect(
				Collectors.toMap(Map.Entry::getKey, entry -> Objects.toString(entry.getValue(), "")));
		builder.environment().putAll(environment);
		printCommand(workDir, arguments, environment);
		Process process = builder.start();
		try {
			return new ProcessId(process);
		}
		catch (Throwable ex) {
			log.error(String.format("ProcessId for '%s' cannot be constructed", process), ex);
			return new ProcessId(process, -1);
		}
	}

	/**
	 * Starts a new process and returns the exit code.
	 *
	 * @param consumer the {@code Process'} output consumer
	 * @return the exit code of the {@link Process}
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if the current thread is interrupted
	 * @see ProcessBuilder#start()
	 */
	public int run(Consumer<? super String> consumer) throws InterruptedException, IOException {
		Objects.requireNonNull(consumer, "'consumer' must not be null");
		ProcessId processId = start();
		Process process = processId.getProcess();
		Thread thread = threadFactory.newThread(() -> {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				try {
					reader.lines().filter(StringUtils::hasText).forEach(consumer);
				}
				catch (UncheckedIOException ex) {
					if (!ex.getMessage().contains("Stream closed")) {
						throw ex;
					}
				}
			}
			catch (IOException ex) {
				throw new UncheckedIOException("Stream cannot be closed", ex);
			}
		});
		thread.setUncaughtExceptionHandler((t, ex) -> log.error("Exception in thread " + t, ex));
		thread.setName("process-" + number.getAndIncrement());
		thread.setDaemon(true);
		thread.start();
		int exit = process.waitFor();
		thread.join(100);
		return exit;
	}

	private static void printCommand(@Nullable Path workDir, List<String> arguments, Map<String, String> environment) {
		StringBuilder msg = new StringBuilder(String.format("Run a command '%s'", String.join(" ", arguments)));
		if (workDir != null) {
			msg.append(String.format(" within the directory '%s'", workDir));
		}
		if (!environment.isEmpty()) {
			msg.append(String.format(" using the environment %s", environment));
		}
		log.info(msg.toString());
	}

}
