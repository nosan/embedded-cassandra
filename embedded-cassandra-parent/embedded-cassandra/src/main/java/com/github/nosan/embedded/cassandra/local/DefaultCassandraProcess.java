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

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

	@Nonnull
	private final Path directory;

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

	private long pid = -1;

	/**
	 * Creates a {@link DefaultCassandraProcess}.
	 *
	 * @param directory a configured directory
	 * @param startupTimeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param version a version
	 * @param javaHome java home directory
	 */
	DefaultCassandraProcess(@Nonnull Path directory, @Nonnull Version version, @Nonnull Duration startupTimeout,
			@Nonnull List<String> jvmOptions, @Nullable Path javaHome) {
		this.directory = directory;
		this.startupTimeout = startupTimeout;
		this.version = version;
		this.javaHome = javaHome;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
	}

	@Override
	@Nonnull
	public Settings start() throws Exception {
		Path directory = this.directory;
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
		log.debug("Cassandra Process ({}) has been started", (this.pid > 0) ? this.pid : "???");
		Duration timeout = this.startupTimeout;
		Settings settings = getSettings(directory);
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
				throwException(String.format("Cassandra has not be started. Please see logs (%s) for more details.",
						logPath), outputCapture);
			}
		}
		return settings;
	}


	@Override
	public void stop() throws Exception {
		try {
			Process process = this.process;
			if (process != null && process.isAlive()) {
				Path pidFile = this.pidFile;
				long pid = this.pid;
				log.debug("Stops Cassandra Process ({})", (pid > 0) ? pid : "???");
				if (pidFile != null && Files.exists(pidFile)) {
					stop(pidFile);
				}
				if (pid > 0) {
					stop(pid);
				}
				process.destroy();
				boolean waitFor = process.waitFor(15, TimeUnit.SECONDS);
				if (!waitFor) {
					throw new IOException("Casandra Process has not been stopped correctly");
				}
				//The  cannot access the file because it is being used by another process
				Thread.sleep(2000);
			}
		}
		finally {
			this.pid = -1;
			this.pidFile = null;
			this.process = null;
		}
	}

	private static Settings getSettings(@Nonnull Path directory) throws IOException {
		Path target = directory.resolve("conf/cassandra.yaml");
		try (InputStream is = Files.newInputStream(target)) {
			Yaml yaml = new Yaml();
			return new MapSettings(yaml.loadAs(is, Map.class));
		}
	}


	private static String getJavaHome(Path javaHome) {
		if (javaHome != null) {
			return String.valueOf(javaHome.toAbsolutePath());
		}
		return new SystemProperty("java.home").or("");
	}


	private static void stop(Path pidFile) throws IOException, InterruptedException {
		if (OS.isWindows()) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
			arguments.add(pidFile.getParent().resolve("stop-server.ps1").toAbsolutePath());
			arguments.add("-p");
			arguments.add(pidFile.toAbsolutePath());
			new RunProcess(arguments).runAndWait(log::info);
		}
		else {
			new RunProcess(Arrays.asList("bash", "-c",
					String.format("kill -SIGINT `cat %s`", pidFile.toAbsolutePath()))).runAndWait(log::info);
		}
	}


	private static void stop(long pid) throws IOException, InterruptedException {
		if (OS.isWindows()) {
			new RunProcess(Arrays.asList("TASKKILL", "/T", "/pid", pid)).runAndWait(log::info);
		}
		else {
			new RunProcess(Arrays.asList("kill", "-SIGINT", pid)).runAndWait(log::info);
		}
	}


	private static void throwException(String message, OutputCapture outputCapture) throws IOException {
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
