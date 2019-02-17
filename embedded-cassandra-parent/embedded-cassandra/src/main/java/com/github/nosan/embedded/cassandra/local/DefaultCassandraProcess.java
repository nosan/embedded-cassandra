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
import java.net.InetAddress;
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.util.NetworkUtils;
import com.github.nosan.embedded.cassandra.util.OS;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.ProcessUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.SystemProperty;
import com.github.nosan.embedded.cassandra.util.ThreadNameSupplier;

/**
 * Default implementation of the {@link CassandraProcess}.
 *
 * @author Dmytro Nosan
 * @since 1.0.9
 */
class DefaultCassandraProcess implements CassandraProcess {

	private static final Logger log = LoggerFactory.getLogger(DefaultCassandraProcess.class);

	private static final AtomicLong instanceCounter = new AtomicLong();

	@Nonnull
	private final ThreadNameSupplier threadNameSupplier = new ThreadNameSupplier(String.format("cassandra-%d",
			instanceCounter.incrementAndGet()));

	@Nonnull
	private final ThreadFactory threadFactory = runnable -> {
		Thread thread = new Thread(runnable, this.threadNameSupplier.get());
		thread.setDaemon(true);
		return thread;
	};

	@Nonnull
	private final Path workingDirectory;

	@Nonnull
	private final Duration startupTimeout;

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
	private SmartSettings settings;

	private long pid = -1;

	/**
	 * Creates a {@link DefaultCassandraProcess}.
	 *
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param startupTimeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param javaHome java home directory
	 * @param allowRoot allow running as a root
	 * @param jmxPort JMX port
	 */
	DefaultCassandraProcess(@Nonnull Path workingDirectory, @Nonnull Version version, @Nonnull Duration startupTimeout,
			@Nonnull List<String> jvmOptions, @Nullable Path javaHome, int jmxPort, boolean allowRoot) {
		this.workingDirectory = workingDirectory;
		this.startupTimeout = startupTimeout;
		this.version = version;
		this.javaHome = javaHome;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
		this.jmxPort = jmxPort;
		this.allowRoot = allowRoot;
	}

	@Override
	@Nonnull
	public Settings start() throws IOException, InterruptedException {
		Path directory = this.workingDirectory;
		Version version = this.version;
		Duration timeout = this.startupTimeout;
		ThreadFactory threadFactory = this.threadFactory;
		SmartSettings settings = getSettings(directory, version);
		this.settings = settings;
		Path executable = (OS.get() == OS.WINDOWS) ?
				directory.resolve("bin/cassandra.ps1") : directory.resolve("bin/cassandra");
		Path pidFile = directory.resolve(String.format("bin/%s.pid", UUID.randomUUID()));
		this.pidFile = pidFile;
		String javaHome = getJavaHome(this.javaHome);

		List<Object> arguments = new ArrayList<>();
		if (OS.get() == OS.WINDOWS) {
			arguments.add("powershell");
			arguments.add("-ExecutionPolicy");
			arguments.add("Unrestricted");
		}
		arguments.add(executable.toAbsolutePath());
		arguments.add("-f");
		if (OS.get() == OS.WINDOWS && (version.getMajor() > 2 || (version.getMajor() == 2 && version.getMinor() > 1))) {
			arguments.add("-a");
		}
		if (this.allowRoot && OS.get() != OS.WINDOWS &&
				(version.getMajor() > 3 || (version.getMajor() == 3 && version.getMinor() > 1))) {
			arguments.add("-R");
		}
		arguments.add("-p");
		arguments.add(pidFile.toAbsolutePath());

		List<String> jvmOptions = new ArrayList<>();
		int jmxPort = (this.jmxPort != 0) ? this.jmxPort : PortUtils.getPort();
		jvmOptions.add(String.format("-Dcassandra.jmx.local.port=%d", jmxPort));
		jvmOptions.addAll(this.jvmOptions);

		Map<String, String> environment = new LinkedHashMap<>();
		if (StringUtils.hasText(javaHome)) {
			environment.put("JAVA_HOME", javaHome);
		}
		environment.put("JVM_EXTRA_OPTS", String.join(" ", jvmOptions));

		Predicate<String> outputFilter = new StackTraceFilter().and(new CompilerFilter());
		BufferedOutput bufferedOutput = new BufferedOutput(10);
		ReadinessOutput readinessOutput = new ReadinessOutput();
		Process process = new RunProcess(directory, environment, threadFactory, arguments)
				.run(new FilteredOutput(bufferedOutput, outputFilter),
						new FilteredOutput(readinessOutput, outputFilter),
						new FilteredOutput(new RpcAddressParser(settings), outputFilter),
						new FilteredOutput(new ListenAddressParser(settings), outputFilter),
						new FilteredOutput(LoggerFactory.getLogger(Cassandra.class)::info, outputFilter));

		this.process = process;
		this.pid = ProcessUtils.getPid(process);

		if (log.isDebugEnabled()) {
			log.debug("Cassandra Process ({}) has been started", getPidString(this.pid));
		}

		try {
			long start = System.currentTimeMillis();
			boolean result = WaitUtils.await(timeout, () -> {
				if (!process.isAlive()) {
					throwException("Cassandra is not alive. Please see logs for more details.", bufferedOutput);
				}
				long elapsed = System.currentTimeMillis() - start;
				//when it is not possible to check cassandra output.
				if (elapsed > 20000L) {
					return TransportUtils.isReady(settings);
				}
				return readinessOutput.isReady() && TransportUtils.isReady(settings);
			});
			if (!result) {
				throwException(String.format("Cassandra has not been started, seems like (%d) milliseconds " +
						"is not enough.", timeout.toMillis()), bufferedOutput);
			}
		}
		catch (InterruptedException | IOException ex) {
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
		Path directory = this.workingDirectory;
		long pid = this.pid;
		ThreadFactory threadFactory = this.threadFactory;
		Settings settings = this.settings;
		if (process != null && process.isAlive()) {
			if (log.isDebugEnabled()) {
				log.debug("Stops Cassandra process ({})", getPidString(pid));
			}
			stop(threadFactory, process, pidFile, directory, pid);
			if (settings != null) {
				try {
					WaitUtils.await(Duration.ofSeconds(10), () -> TransportUtils.isDisabled(settings)
							&& !process.isAlive());
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				catch (Throwable ex) {
					log.error(String.format("Could not check whether process (%s) is stopped or not",
							getPidString(pid)), ex);
				}
			}
			if (process.isAlive()) {
				forceStop(threadFactory, process, pidFile, directory, pid);
			}
			try {
				if (!process.waitFor(3, TimeUnit.SECONDS)) {
					throw new IOException(String.format("Casandra Process (%s) has not been stopped correctly",
							getPidString(pid)));
				}
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			this.settings = null;
			this.pid = -1;
			this.pidFile = null;
			this.process = null;
		}
	}

	private static SmartSettings getSettings(Path directory, Version version) throws IOException {
		Path target = directory.resolve("conf/cassandra.yaml");
		try (InputStream is = Files.newInputStream(target)) {
			Yaml yaml = new Yaml();
			return new SmartSettings(version, yaml.loadAs(is, Map.class));
		}
	}

	private static String getJavaHome(Path javaHome) {
		if (javaHome != null) {
			return String.valueOf(javaHome.toAbsolutePath());
		}
		return new SystemProperty("java.home").get();
	}

	private static String getPidString(long pid) {
		return (pid > 0) ? String.valueOf(pid) : "???";
	}

	private static void stop(ThreadFactory threadFactory, Process process, Path pidFile, Path directory, long pid) {
		if (pidFile != null && Files.exists(pidFile)) {
			try {
				stop(threadFactory, pidFile, directory, false);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (Throwable ex) {
				log.error(String.format("Could not stop a process (%s) by a file (%s)",
						getPidString(pid), pidFile), ex);
			}
		}
		else if (pid > 0) {
			try {
				stop(threadFactory, pid, directory, false);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (Throwable ex) {
				log.error(String.format("Could not  <kill or taskkill> a process (%s)", getPidString(pid)),
						ex);
			}
		}
		else {
			process.destroy();
		}
	}

	private static void forceStop(ThreadFactory threadFactory, Process process, Path pidFile, Path directory,
			long pid) {
		if (pidFile != null && Files.exists(pidFile)) {
			try {
				stop(threadFactory, pidFile, directory, true);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (Throwable ex) {
				log.error(
						String.format("Could not force stop a process (%s) by a file (%s)", getPidString(pid), pidFile),
						ex);
			}
		}
		if (pid > 0) {
			try {
				stop(threadFactory, pid, directory, true);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (Throwable ex) {
				log.error(String.format("Could not force <kill or taskkill> a process (%s)", getPidString(pid)),
						ex);
			}
		}
		process.destroyForcibly();
	}

	private static void stop(ThreadFactory threadFactory, Path pidFile, Path directory, boolean force)
			throws IOException, InterruptedException {
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
			new RunProcess(directory, null, threadFactory, arguments).runAndWait(log::info);
		}
		else {
			String signal = force ? "-9" : "-SIGINT";
			new RunProcess(directory, null,
					threadFactory,
					Arrays.asList("bash", "-c", String.format("kill %s `cat %s`", signal, pidFile.toAbsolutePath())))
					.runAndWait(log::info);
		}
	}

	private static void stop(ThreadFactory threadFactory, long pid, Path directory, boolean force)
			throws IOException, InterruptedException {
		if (OS.get() == OS.WINDOWS) {
			List<Object> arguments = new ArrayList<>();
			arguments.add("taskkill");
			if (force) {
				arguments.add("/F");
			}
			arguments.add("/T");
			arguments.add("/pid");
			arguments.add(pid);
			new RunProcess(directory, null, threadFactory, arguments).runAndWait(log::info);
		}
		else {
			String signal = force ? "-9" : "-SIGINT";
			new RunProcess(directory, null, threadFactory, Arrays.asList("kill", signal, pid))
					.runAndWait(log::info);
		}
	}

	private static void throwException(String message, BufferedOutput bufferedOutput) throws IOException {
		StringBuilder builder = new StringBuilder(message);
		if (!bufferedOutput.isEmpty()) {
			Collection<String> lines = bufferedOutput.lines();
			builder.append(String.format(" Last (%s) lines:", lines.size()));
			for (String line : lines) {
				builder.append(String.format("%n\t%s", line));
			}
		}
		throw new IOException(builder.toString());
	}

	/**
	 * A simple implementation of {@link RunProcess.Output} class to find a {@code rpc_address} and set it into
	 * the {@link SmartSettings}.
	 */
	private static final class RpcAddressParser implements RunProcess.Output {

		@Nonnull
		private final SmartSettings settings;

		@Nonnull
		private final Pattern regex;

		private boolean alreadySet;

		/**
		 * Creates a new {@link RpcAddressParser}.
		 *
		 * @param settings the node settings
		 */
		RpcAddressParser(@Nonnull SmartSettings settings) {
			this.settings = settings;
			this.regex = Pattern.compile(String.format(".*/(.+):(%d|%d).*", settings.getPort(), settings.getSslPort()));
		}

		@Override
		public void accept(@Nonnull String line) {
			if (this.alreadySet) {
				return;
			}
			Matcher matcher = this.regex.matcher(line);
			if (matcher.matches()) {
				String address = matcher.group(1).trim();
				try {
					this.settings.setRealAddress(NetworkUtils.getInetAddress(address.trim()));
					this.alreadySet = true;
				}
				catch (Throwable ex) {
					if (log.isTraceEnabled()) {
						log.error(String.format("Could not parse an InetAddress (%s)", address), ex);
					}
				}
			}
		}
	}

	/**
	 * A simple implementation of {@link RunProcess.Output} class to find a {@code listen_address} and set it into
	 * the {@link SmartSettings}.
	 */
	private static final class ListenAddressParser implements RunProcess.Output {

		@Nonnull
		private final SmartSettings settings;

		@Nonnull
		private final Pattern regex;

		private boolean alreadySet;

		/**
		 * Creates a new {@link ListenAddressParser}.
		 *
		 * @param settings the node settings
		 */
		ListenAddressParser(@Nonnull SmartSettings settings) {
			this.settings = settings;
			this.regex = Pattern.compile(
					String.format(".*/(.+):(%d|%d).*", settings.getStoragePort(), settings.getSslStoragePort()));
		}

		@Override
		public void accept(@Nonnull String line) {
			if (this.alreadySet) {
				return;
			}
			Matcher matcher = this.regex.matcher(line);
			if (matcher.matches()) {
				String address = matcher.group(1).trim();
				try {
					this.settings.setRealListenAddress(NetworkUtils.getInetAddress(address.trim()));
					this.alreadySet = true;
				}
				catch (Throwable ex) {
					if (log.isTraceEnabled()) {
						log.error(String.format("Could not parse an InetAddress (%s)", address), ex);
					}
				}
			}
		}
	}

	/**
	 * A simple implementation of {@link RunProcess.Output} class to check whether {@code cassandra} output is ready or
	 * not.
	 */
	private static final class ReadinessOutput implements RunProcess.Output {

		private boolean ready = false;

		@Override
		public void accept(@Nonnull String line) {
			if (!this.ready) {
				this.ready = line.matches("(?i).*listening\\s*for\\s*cql.*") ||
						line.matches("(?i).*not\\s*starting\\s*native.*");
			}
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

	/**
	 * A basic implementation of the {@link NodeSettings} with an opportunity to set {@code realListenAddress} and
	 * {@code realAddress}.
	 */
	private static final class SmartSettings extends NodeSettings {

		@Nullable
		private volatile InetAddress realListenAddress;

		@Nullable
		private volatile InetAddress realAddress;

		/**
		 * Creates a new {@link SmartSettings}.
		 *
		 * @param version a version
		 * @param properties a node properties
		 */
		SmartSettings(@Nonnull Version version, @Nullable Map<?, ?> properties) {
			super(version, properties);
		}

		@Nonnull
		@Override
		public InetAddress getRealAddress() {
			InetAddress address = this.realAddress;
			if (address != null) {
				return address;
			}
			return super.getRealAddress();
		}

		/**
		 * Set the rpc address.
		 *
		 * @param address an IP address
		 */
		void setRealAddress(@Nullable InetAddress address) {
			this.realAddress = address;
		}

		@Nonnull
		@Override
		public InetAddress getRealListenAddress() {
			InetAddress address = this.realListenAddress;
			if (address != null) {
				return address;
			}
			return super.getRealListenAddress();
		}

		/**
		 * Set the listen address.
		 *
		 * @param address an IP address
		 */
		void setRealListenAddress(@Nullable InetAddress address) {
			this.realListenAddress = address;
		}
	}
}
