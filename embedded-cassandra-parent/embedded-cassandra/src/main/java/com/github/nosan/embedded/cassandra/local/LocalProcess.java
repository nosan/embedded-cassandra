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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
 * Utility class to run Cassandra.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class LocalProcess {

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

	@Nonnull
	private final Supplier<Path> directory;

	@Nonnull
	private final Duration startupTimeout;

	@Nonnull
	private final List<String> jvmOptions;

	@Nonnull
	private final Version version;

	@Nullable
	private final Path javaHome;

	@Nullable
	private Path pidFile;

	@Nullable
	private Process process;

	@Nullable
	private Settings settings;

	private long pid = -1;

	/**
	 * Creates a {@link LocalProcess}.
	 *
	 * @param directory {@code Supplier} of an initialized working directory
	 * @param startupTimeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param version a version
	 * @param javaHome java home directory
	 */
	LocalProcess(@Nonnull Supplier<Path> directory, @Nonnull Duration startupTimeout,
			@Nonnull List<String> jvmOptions, @Nonnull Version version, @Nullable Path javaHome) {
		this.directory = directory;
		this.startupTimeout = startupTimeout;
		this.version = version;
		this.javaHome = javaHome;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
	}

	/**
	 * Starts the Cassandra.
	 *
	 * @throws Exception if the Cassandra cannot be started
	 */
	void start() throws Exception {
		Path directory = this.directory.get();
		Settings settings = getSettings();
		Path executable = OS.isWindows() ? directory.resolve("bin/cassandra.ps1") : directory.resolve("bin/cassandra");
		Path pidFile = directory.resolve(String.format("bin/%s.pid", UUID.randomUUID()));
		Path logPath = directory.resolve("logs");
		Path errorFile = logPath.resolve("hs_err_pid_%p.log");

		this.pidFile = pidFile;

		List<Object> arguments = new ArrayList<>();
		if (OS.isWindows()) {
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
		}
		arguments.add(executable.toAbsolutePath());
		arguments.add("-f");
		Version version = this.version;
		if (OS.isWindows()) {
			if (version.getMajor() > 2 || (version.getMajor() == 2 && version.getMinor() > 1)) {
				arguments.add("-a");
			}
		}
		arguments.add("-p");
		arguments.add(pidFile.toAbsolutePath());
		arguments.add("-E");
		arguments.add(errorFile.toAbsolutePath());
		arguments.add("-H");
		arguments.add(logPath.toAbsolutePath());

		Map<String, String> environment = new LinkedHashMap<>();
		String javaHome = getJavaHome(this.javaHome);
		if (StringUtils.hasText(javaHome)) {
			environment.put("JAVA_HOME", javaHome);
		}
		List<String> jvmOptions = new ArrayList<>();
		jvmOptions.add(String.format("-Dcassandra.jmx.local.port=%d", PortUtils.getPort()));
		jvmOptions.addAll(this.jvmOptions);
		//travis and appveyor.
		if (Boolean.valueOf(new SystemProperty("CASSANDRA.CI.BUILD").or("false"))) {
			jvmOptions.add("-Xmx512m");
			jvmOptions.add("-Xms512m");
		}
		environment.put("JVM_EXTRA_OPTS", String.join(" ", jvmOptions));

		OutputCapture outputCapture = new OutputCapture(20);

		Process process = new RunProcess(directory, environment, arguments)
				.run(outputCapture, log::info);

		this.process = process;
		this.pid = ProcessUtils.getPid(process);
		log.debug("Cassandra Process ({}) has been started", this.pid);
		Duration timeout = this.startupTimeout;
		if (timeout.toNanos() > 0) {
			boolean result = WaitUtils.await(timeout, () -> {
				if (!process.isAlive()) {
					throwException(String.format("Cassandra has not be started. Please see logs (%s) for more details.",
							logPath), outputCapture);
				}
				int storagePort = (settings.getStoragePort() != -1) ? settings.getStoragePort() : 7001;
				int port = -1;
				if (settings.isStartNativeTransport()) {
					port = (settings.getPort() != -1) ? settings.getPort() : 9042;
				}
				else if (settings.isStartRpc()) {
					port = (settings.getRpcPort() != -1) ? settings.getRpcPort() : 9160;
				}
				return PortUtils.isPortBusy(settings.getAddress(), storagePort) &&
						(port == -1 || PortUtils.isPortBusy(settings.getAddress(), port));
			});
			if (!result) {
				throwException(String.format("Cassandra has not be started. Storage port (%s) is not available." +
						" Please see logs (%s) for more details.", logPath, settings.getStoragePort()), outputCapture);
			}
		}
	}


	/**
	 * Stops the Cassandra.
	 *
	 * @throws Exception if the Cassandra cannot be stopped
	 */
	void stop() throws Exception {
		try {
			Process process = this.process;
			if (process != null && process.isAlive()) {
				try {
					Path pidFile = this.pidFile;
					long pid = this.pid;
					log.debug("Stops Cassandra Process ({})", pid);
					if (pidFile != null && Files.exists(pidFile)) {
						stop(pidFile);
					}
					if (pid > 0) {
						stop(pid);
					}
					boolean waitFor = process.destroyForcibly().waitFor(15, TimeUnit.SECONDS);
					if (!waitFor) {
						throw new IOException("Casandra Process has not been stopped correctly");
					}
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		}
		finally {
			this.pid = -1;
			this.pidFile = null;
			this.settings = null;
			this.process = null;
		}
	}


	/**
	 * Returns the settings this Cassandra is running on.
	 *
	 * @return the settings
	 * @throws Exception if could not get a settings
	 */
	@Nonnull
	Settings getSettings() throws Exception {
		if (this.settings == null) {
			Path target = this.directory.get().resolve("conf/cassandra.yaml");
			try (InputStream is = Files.newInputStream(target)) {
				Yaml yaml = new Yaml();
				this.settings = new MapSettings(yaml.loadAs(is, Map.class));
			}
		}
		return this.settings;
	}


	private static String getJavaHome(Path javaHome) {
		if (javaHome != null) {
			return String.valueOf(javaHome.toAbsolutePath());
		}
		return new SystemProperty("java.home").or("");
	}


	private static void stop(Path pidFile) throws IOException {
		if (OS.isWindows()) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
			arguments.add(pidFile.getParent().resolve("stop-server.ps1").toAbsolutePath());
			arguments.add("-f");
			arguments.add("-p");
			arguments.add(pidFile.toAbsolutePath());
			new RunProcess(arguments).runAndWait(log::info);
		}
		else {
			new RunProcess(Arrays.asList("bash", "-c",
					String.format("kill -9 `cat %s`", pidFile.toAbsolutePath()))).runAndWait(log::info);
		}
	}


	private static void stop(long pid) throws IOException {
		if (OS.isWindows()) {
			new RunProcess(Arrays.asList("TASKKILL", "/F", "/T", "/pid", pid)).runAndWait(log::info);
		}
		else {
			new RunProcess(Arrays.asList("kill", -9, pid)).runAndWait(log::info);
		}
	}


	private static void throwException(String message, OutputCapture outputCapture)
			throws IOException {
		StringBuilder builder = new StringBuilder(message);
		if (!outputCapture.isEmpty()) {
			Collection<String> lines = outputCapture.lines();
			builder.append(String.format(" Last (%s) lines:", lines.size()));
			for (String line : lines) {
				builder.append(String.format("%n%s", line));
			}
		}
		throw new IOException(builder.toString());
	}


}
