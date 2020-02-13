/*
 * Copyright 2020 the original author or authors.
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.nosan.embedded.cassandra.commons.logging.Logger;

abstract class AbstractCassandraDatabase implements CassandraDatabase {

	protected final Logger logger = Logger.get(getClass());

	private final String name;

	private final Version version;

	private final Path workingDirectory;

	private final Map<String, String> environmentVariables;

	private final Map<String, Object> configProperties;

	private final Map<String, String> systemProperties;

	private final Set<String> jvmOptions;

	private volatile Process process;

	AbstractCassandraDatabase(String name, Version version, Path workingDirectory,
			Map<String, String> environmentVariables, Map<String, Object> configProperties,
			Map<String, String> systemProperties, Set<String> jvmOptions) {
		this.name = name;
		this.version = version;
		this.workingDirectory = workingDirectory;
		this.environmentVariables = Collections.unmodifiableMap(new LinkedHashMap<>(environmentVariables));
		this.configProperties = Collections.unmodifiableMap(new LinkedHashMap<>(configProperties));
		this.systemProperties = Collections.unmodifiableMap(new LinkedHashMap<>(systemProperties));
		this.jvmOptions = Collections.unmodifiableSet(new LinkedHashSet<>(jvmOptions));
	}

	@Override
	public final synchronized void start() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(this.workingDirectory.toFile());
		processBuilder.environment().putAll(this.environmentVariables);
		this.process = doStart(processBuilder);
	}

	@Override
	public final synchronized void stop() throws IOException {
		Process process = this.process;
		if (process != null && process.isAlive()) {
			doStop(process);
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				if (!process.waitFor(3, TimeUnit.SECONDS)) {
					throw new IOException("Unable to stop " + toString());
				}
			}
			process.destroy();
		}
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final Map<String, String> getEnvironmentVariables() {
		return this.environmentVariables;
	}

	@Override
	public final Map<String, Object> getConfigProperties() {
		return this.configProperties;
	}

	@Override
	public final Map<String, String> getSystemProperties() {
		return this.systemProperties;
	}

	@Override
	public final Set<String> getJvmOptions() {
		return this.jvmOptions;
	}

	@Override
	public final synchronized CompletableFuture<? extends CassandraDatabase> onExit() {
		return this.process.onExit()
				.thenApply((Function<Process, CassandraDatabase>) processHandler -> this);
	}

	@Override
	public final synchronized boolean isAlive() {
		Process process = this.process;
		return (process != null) && process.isAlive();
	}

	@Override
	public final Version getVersion() {
		return this.version;
	}

	@Override
	public final Path getWorkingDirectory() {
		return this.workingDirectory;
	}

	@Override
	public final synchronized Process.Output getStdOut() {
		return this.process.getStdOut();
	}

	@Override
	public final synchronized Process.Output getStdErr() {
		return this.process.getStdErr();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + "process=" + this.process + '}';
	}

	protected abstract Process doStart(ProcessBuilder processBuilder) throws IOException;

	protected abstract void doStop(Process process) throws IOException;

	protected final int exec(String name, String[] command) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(this.workingDirectory.toFile());
		processBuilder.environment().putAll(this.environmentVariables);
		Process process = Process.start(name, processBuilder);
		process.getStdOut().attach(this.logger::info);
		process.getStdErr().attach(this.logger::error);
		return process.waitFor();
	}

}
