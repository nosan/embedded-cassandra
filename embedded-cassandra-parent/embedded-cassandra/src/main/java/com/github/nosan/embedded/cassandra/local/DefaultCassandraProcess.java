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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.OS;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.ProcessUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.SystemProperty;

/**
 * Default implementation of the {@link CassandraProcess}.
 *
 * @author Dmytro Nosan
 * @see DefaultCassandraProcessFactory
 * @since 1.0.9
 */
class DefaultCassandraProcess implements CassandraProcess {

	private static final Logger log = LoggerFactory.getLogger(DefaultCassandraProcess.class);

	@Nonnull
	private final Path directory;

	@Nonnull
	private final Duration timeout;

	@Nonnull
	private final List<String> jvmOptions;

	@Nonnull
	private final Version version;

	@Nullable
	private final Path javaHome;

	private final boolean allowRoot;

	private final int jmxPort;

	@Nullable
	private Path pidFile;

	@Nullable
	private Process process;

	@Nullable
	private Settings settings;

	private long pid = -1;

	/**
	 * Creates a {@link DefaultCassandraProcess}.
	 *
	 * @param directory a configured base directory
	 * @param version a version
	 * @param timeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param javaHome java home directory
	 * @param allowRoot allow running as a root
	 * @param jmxPort JMX port
	 */
	DefaultCassandraProcess(@Nonnull Path directory, @Nonnull Version version, @Nonnull Duration timeout,
			@Nonnull List<String> jvmOptions, @Nullable Path javaHome, int jmxPort, boolean allowRoot) {
		this.directory = directory;
		this.timeout = timeout;
		this.version = version;
		this.javaHome = javaHome;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
		this.jmxPort = jmxPort;
		this.allowRoot = allowRoot;
	}

	@Override
	@Nonnull
	public Settings start() throws IOException {
		Path directory = this.directory;
		Version version = this.version;
		Duration timeout = this.timeout;
		int major = version.getMajor();
		int minor = version.getMinor();
		Settings settings = getSettings(directory, version);
		this.settings = settings;
		Path executable = (OS.get() == OS.WINDOWS) ?
				directory.resolve("bin/cassandra.ps1") : directory.resolve("bin/cassandra");
		Path pidFile = directory.resolve(String.format("bin/%s.pid", UUID.randomUUID()));
		this.pidFile = pidFile;

		List<Object> arguments = new ArrayList<>();
		if (OS.get() == OS.WINDOWS) {
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
		}
		arguments.add(executable.toAbsolutePath());
		arguments.add("-f");

		if (OS.get() == OS.WINDOWS && (major > 2 || (major == 2 && minor > 1))) {
			arguments.add("-a");
		}
		if (this.allowRoot && OS.get() != OS.WINDOWS && (major > 3 || (major == 3 && minor > 1))) {
			arguments.add("-R");
		}
		arguments.add("-p");
		arguments.add(pidFile.toAbsolutePath());

		Map<String, String> environment = new LinkedHashMap<>();

		List<String> jvmOptions = new ArrayList<>();
		int jmxPort = (this.jmxPort != 0) ? this.jmxPort : PortUtils.getPort();
		jvmOptions.add(String.format("-Dcassandra.jmx.local.port=%d", jmxPort));
		jvmOptions.addAll(this.jvmOptions);
		environment.put("JVM_EXTRA_OPTS", String.join(" ", jvmOptions));

		String javaHome = getJavaHome(this.javaHome);
		if (StringUtils.hasText(javaHome)) {
			environment.put("JAVA_HOME", javaHome);
		}
		OutputReadiness output = new OutputReadiness();
		Predicate<String> outputFilter = new StackTraceFilter().and(new CompilerFilter());
		Process process = new RunProcess(true, directory, environment, arguments)
				.run(new FilteredOutput(output, outputFilter),
						new FilteredOutput(LoggerFactory.getLogger(Cassandra.class)::info, outputFilter));
		this.process = process;
		this.pid = ProcessUtils.getPid(process);

		if (log.isDebugEnabled()) {
			log.debug("Cassandra Process ({}) has been started", getPidString(this.pid));
		}
		try {
			await(settings, timeout, output, process);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IOException(ex);
		}
		return settings;
	}

	@Override
	public void stop() throws IOException {
		Process process = this.process;
		Path pidFile = this.pidFile;
		Path directory = this.directory;
		long pid = this.pid;
		Settings settings = this.settings;

		if (process != null && process.isAlive()) {
			if (log.isDebugEnabled()) {
				log.debug("Stops Cassandra process ({})", getPidString(pid));
			}

			try {
				stop(process, pidFile, directory, pid);
			}
			catch (Exception ex) {
				log.error(String.format("Process (%s) has not been stopped correctly", getPidString(pid)), ex);
				forceStop(process, pidFile, directory, pid);
			}

			try {
				if (settings != null) {
					WaitUtils.await(Duration.ofSeconds(5), () -> TransportUtils.isDisabled(settings)
							&& !process.isAlive());
				}
				else {
					process.waitFor(5, TimeUnit.SECONDS);
				}
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (Exception ex) {
				log.error(String.format("Could not check whether process (%s) is stopped or not", getPidString(pid)),
						ex);
			}

			if (process.isAlive()) {
				forceStop(process, pidFile, directory, pid);
			}

			try {
				process.waitFor(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			if (process.isAlive()) {
				throw new IOException(String.format("Casandra Process (%s) has not been stopped correctly",
						getPidString(pid)));
			}
			this.settings = null;
			this.pid = -1;
			this.pidFile = null;
			this.process = null;
		}
	}

	private static Settings getSettings(Path directory, Version version) throws IOException {
		Path target = directory.resolve("conf/cassandra.yaml");
		try (InputStream is = Files.newInputStream(target)) {
			Yaml yaml = new Yaml();
			return new NodeSettings(version, yaml.loadAs(is, Map.class));
		}
	}

	private static String getJavaHome(Path javaHome) {
		if (javaHome != null) {
			return String.valueOf(javaHome.toAbsolutePath());
		}
		return new SystemProperty("java.home").or("");
	}

	private static String getPidString(long pid) {
		return (pid > 0) ? String.valueOf(pid) : "???";
	}

	private static void await(Settings settings, Duration timeout, OutputReadiness output, Process process)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Waits ({}) for Cassandra transport and output", timeout);
		}
		boolean result = WaitUtils.await(timeout, () -> {
			if (!process.isAlive()) {
				throwException("Cassandra Process is not alive. Please see logs for more details.", output);
			}
			return output.isReady() && TransportUtils.isReady(settings);
		});
		if (!result) {
			throwException(String.format("Cassandra has not been started, seems like (%d) milliseconds " +
					"is not enough. This could happen either Cassandra transport is not ready" +
					" or there is no way to determine  whether Cassandra is started" +
					" or not if <console> output is disabled.", timeout.toMillis()), output);
		}
	}

	private static void stop(Process process, Path pidFile, Path directory, long pid) throws IOException {
		if (pidFile != null && Files.exists(pidFile)) {
			stop(pidFile, directory, false);
		}
		else if (pid > 0) {
			stop(pid, directory, false);
		}
		else {
			process.destroy();
		}
	}

	private static void forceStop(Process process, Path pidFile, Path directory, long pid) {
		try {
			if (pidFile != null && Files.exists(pidFile)) {
				stop(pidFile, directory, true);
			}
		}
		catch (Throwable ex) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Could not force stop a process (%s) by file (%s)", getPidString(pid), pidFile),
						ex);
			}
		}
		try {
			if (pid > 0) {
				stop(pid, directory, true);
			}
		}
		catch (Throwable ex) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Could not force <kill or taskkill> a process (%s)", getPidString(pid)), ex);
			}
		}
		try {
			process.destroyForcibly();
		}
		catch (Throwable ex) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Could not force destroy a process (%s)", getPidString(pid)), ex);
			}
		}
	}

	private static void stop(Path pidFile, Path directory, boolean force) throws IOException {
		if (OS.get() == OS.WINDOWS) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
			arguments.add(directory.resolve("bin/stop-server.ps1").toAbsolutePath());
			if (force) {
				arguments.add("-f");
			}
			arguments.add("-p");
			arguments.add(pidFile.toAbsolutePath());
			new RunProcess(true, directory, arguments).runAndWait(log::info);
		}
		else {
			String signal = force ? "-9" : "-SIGINT";
			new RunProcess(true, directory, Arrays.asList("bash", "-c",
					String.format("kill %s `cat %s`", signal, pidFile.toAbsolutePath()))).runAndWait(log::info);
		}
	}

	private static void stop(long pid, Path directory, boolean force) throws IOException {
		if (OS.get() == OS.WINDOWS) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("taskkill");
			if (force) {
				arguments.add("/F");
			}
			arguments.add("/T");
			arguments.add("/pid");
			arguments.add(pid);
			new RunProcess(true, directory, arguments).runAndWait(log::info);
		}
		else {
			String signal = force ? "-9" : "-SIGINT";
			new RunProcess(true, directory, Arrays.asList("kill", signal, pid)).runAndWait(log::info);
		}
	}

	private static void throwException(String message, OutputCapture outputCapture) throws IOException {
		StringBuilder builder = new StringBuilder(message);
		if (!outputCapture.isEmpty()) {
			Collection<String> lines = outputCapture.lines();
			builder.append(String.format(" Last (%s) lines:", lines.size()));
			for (String line : lines) {
				builder.append(String.format("%n\t%s", line));
			}
		}
		throw new IOException(builder.toString());
	}

	/**
	 * A simple implementation of {@link OutputCapture} class to check whether {@code cassandra} output is ready or not.
	 */
	private static final class OutputReadiness extends OutputCapture {

		private volatile boolean ready = false;

		/**
		 * Creates a new {@link OutputReadiness}.
		 */
		OutputReadiness() {
			super(10);
		}

		@Override
		public void accept(@Nonnull String line) {
			if (!this.ready) {
				String lowerCase = line.toLowerCase(Locale.ENGLISH);
				this.ready = lowerCase.contains("listening for cql") || lowerCase.contains("not starting native");
			}
			super.accept(line);
		}

		/**
		 * Tests whether {@code cassandra} output is ready or not.
		 *
		 * @return {@code true} if ready, otherwise {@code false}
		 */
		boolean isReady() {
			return this.ready;
		}
	}
}
