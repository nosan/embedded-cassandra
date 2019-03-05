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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.SystemProperty;
import com.github.nosan.embedded.cassandra.util.ThreadNameSupplier;

/**
 * The common implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
abstract class AbstractCassandraNode implements CassandraNode {

	private static final AtomicLong instanceCounter = new AtomicLong();

	private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

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
	private final Version version;

	@Nonnull
	private final Duration timeout;

	@Nonnull
	private final List<String> jvmOptions;

	private final int jmxPort;

	@Nullable
	private final Path javaHome;

	@Nullable
	private Process process;

	@Nullable
	private RuntimeNodeSettings settings;

	/**
	 * Creates a {@link AbstractCassandraNode}.
	 *
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param timeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param javaHome java home directory
	 * @param jmxPort JMX port
	 */
	AbstractCassandraNode(@Nonnull Path workingDirectory, @Nonnull Version version, @Nonnull Duration timeout,
			@Nonnull List<String> jvmOptions, @Nullable Path javaHome, int jmxPort) {
		this.workingDirectory = workingDirectory;
		this.version = version;
		this.timeout = timeout;
		this.javaHome = javaHome;
		this.jmxPort = jmxPort;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
	}

	@Override
	public final void start() throws IOException, InterruptedException {
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		ThreadFactory threadFactory = this.threadFactory;
		Duration timeout = this.timeout;
		RuntimeNodeSettings settings = getSettings(workingDirectory, version);
		this.settings = settings;
		Map<String, String> environment = new LinkedHashMap<>();
		List<String> jvmOptions = new ArrayList<>();
		jvmOptions.add(String.format("-Dcassandra.jmx.local.port=%d", getJmxPort(this.jmxPort)));
		jvmOptions.addAll(this.jvmOptions);
		String javaHome = getJavaHome(this.javaHome);
		if (StringUtils.hasText(javaHome)) {
			environment.put("JAVA_HOME", javaHome);
		}
		environment.put("JVM_EXTRA_OPTS", String.join(" ", jvmOptions));
		Predicate<String> outputFilter = new StackTraceFilter().and(new CompilerFilter());
		NodeReadiness nodeReadiness = new NodeReadiness();
		Process process = start(workingDirectory, version, environment, threadFactory,
				new FilteredOutput(nodeReadiness, outputFilter),
				new FilteredOutput(new RpcAddressParser(settings), outputFilter),
				new FilteredOutput(new ListenAddressParser(settings), outputFilter),
				new FilteredOutput(log::info, outputFilter));
		this.process = process;
		boolean result;
		try {
			result = waitForStarted(process, timeout, settings, nodeReadiness);
		}
		catch (InterruptedException | IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IOException(ex);
		}
		String id = getId(process);
		if (!result) {
			throw new IOException(String.format("Cassandra Node (%s) has not been started," +
					" seems like (%d) milliseconds is not enough.", id, timeout.toMillis()));
		}
		log.info("Cassandra Node ({}) has been started", id);
	}

	@Override
	public final void stop() throws IOException, InterruptedException {
		Process process = this.process;
		RuntimeNodeSettings settings = this.settings;
		Path workingDirectory = this.workingDirectory;
		Version version = this.version;
		ThreadFactory threadFactory = this.threadFactory;
		Map<String, String> environment = new LinkedHashMap<>();
		if (process != null && process.isAlive()) {
			String id = getId(process);
			boolean result = false;
			try {
				stop(process, workingDirectory, version, environment, threadFactory, log::info);
				result = waitForStopped(Duration.ofSeconds(10), process, settings);
			}
			catch (InterruptedException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				log.error(String.format("Could not stop Cassandra Node (%s).", id), ex);
			}
			if (!result) {
				try {
					forceStop(process, workingDirectory, version, environment, threadFactory, log::info);
					result = waitForStopped(Duration.ofSeconds(5), process, settings);
				}
				catch (InterruptedException ex) {
					throw ex;
				}
				catch (Throwable ex) {
					log.error(String.format("Could not <force> stop Cassandra Node (%s).", id), ex);
				}
			}
			if (!result) {
				try {
					result = process.destroyForcibly().waitFor(3, TimeUnit.SECONDS);
				}
				catch (InterruptedException ex) {
					throw ex;
				}
				catch (Throwable ex) {
					log.error(String.format("Could not destroy Cassandra Node (%s).", id), ex);
				}
			}
			if (!result) {
				throw new IOException(String.format("Casandra Node (%s) has not been stopped.", id));
			}
			this.settings = null;
			this.process = null;
			log.info("Cassandra Node ({}) has been stopped", id);
		}
	}

	@Override
	@Nullable
	public final Settings getSettings() {
		return this.settings;
	}

	/**
	 * Creates a new node process.
	 *
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param threadFactory factory to create a process
	 * @param environment the environment variables that should be associated with process
	 * @param outputs the process output consumers
	 * @return the newly created node process
	 * @throws IOException if an I/O error occurs
	 */
	@Nonnull
	protected abstract Process start(@Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException;

	/**
	 * Stops a node process.
	 *
	 * @param process the process
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param threadFactory factory to create a shutdown process
	 * @param environment the environment variables that should be associated with shutdown process
	 * @param outputs the shutdown process output consumers
	 * @throws IOException if an I/O error occurs
	 */
	protected abstract void stop(@Nonnull Process process, @Nonnull Path workingDirectory, @Nonnull Version version,
			@Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException;

	/**
	 * Stops a node process.
	 *
	 * @param process the process
	 * @param workingDirectory a configured base directory
	 * @param version a version
	 * @param threadFactory factory to create a shutdown process
	 * @param environment the environment variables that should be associated with shutdown process
	 * @param outputs the shutdown process output consumers
	 * @throws IOException if an I/O error occurs
	 */
	protected abstract void forceStop(@Nonnull Process process, @Nonnull Path workingDirectory,
			@Nonnull Version version, @Nonnull Map<String, String> environment, @Nonnull ThreadFactory threadFactory,
			@Nonnull RunProcess.Output... outputs) throws IOException;

	/**
	 * Returns the ID of the Cassandra node.
	 *
	 * @param process the Cassandra process
	 * @return the ID of the node
	 */
	@Nonnull
	protected abstract String getId(@Nonnull Process process);

	private int getJmxPort(int jmxPort) {
		return jmxPort != 0 ? jmxPort : PortUtils.getPort();
	}

	private String getJavaHome(Path javaHome) {
		if (javaHome != null) {
			return String.valueOf(javaHome.toAbsolutePath());
		}
		return new SystemProperty("java.home").get();
	}

	private RuntimeNodeSettings getSettings(Path directory, Version version) throws IOException {
		Path target = directory.resolve("conf/cassandra.yaml");
		try (InputStream is = Files.newInputStream(target)) {
			Yaml yaml = new Yaml();
			return new RuntimeNodeSettings(version, yaml.loadAs(is, Map.class));
		}
	}

	private boolean waitForStarted(Process process, Duration timeout, RuntimeNodeSettings settings,
			NodeReadiness nodeReadiness) throws Exception {
		String id = getId(process);
		long start = System.currentTimeMillis();
		return WaitUtils.await(timeout, () -> {
			if (!process.isAlive()) {
				throw new IOException(String.format("Cassandra Node (%s) is not alive. " +
						"Please see logs for more details.", id));
			}
			long elapsed = System.currentTimeMillis() - start;
			if (elapsed > 20000) {
				return TransportUtils.isReady(settings);
			}
			return nodeReadiness.isReady() && TransportUtils.isReady(settings);
		});
	}

	private boolean waitForStopped(Duration timeout, Process process, Settings settings) throws Exception {
		return WaitUtils.await(timeout, () -> (settings == null || TransportUtils.isDisabled(settings))
				&& !process.isAlive());
	}

	/**
	 * A simple implementation of {@link RunProcess.Output} class to find a {@code rpc_address} and set it into
	 * the {@link RuntimeNodeSettings}.
	 */
	private static final class RpcAddressParser implements RunProcess.Output {

		@Nonnull
		private final RuntimeNodeSettings settings;

		@Nonnull
		private final Pattern regex;

		private boolean alreadySet;

		/**
		 * Creates a new {@link RpcAddressParser}.
		 *
		 * @param settings the node settings
		 */
		RpcAddressParser(@Nonnull RuntimeNodeSettings settings) {
			this.settings = settings;
			this.regex = Pattern.compile(String.format(".*/(.+):(%d|%d).*", settings.getPort(),
					settings.getSslPort()));
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
					if (log.isDebugEnabled()) {
						log.error(String.format("Could not parse an InetAddress (%s)", address), ex);
					}
				}
			}
		}
	}

	/**
	 * A simple implementation of {@link RunProcess.Output} class to find a {@code listen_address} and set it into
	 * the {@link RuntimeNodeSettings}.
	 */
	private static final class ListenAddressParser implements RunProcess.Output {

		@Nonnull
		private final RuntimeNodeSettings settings;

		@Nonnull
		private final Pattern regex;

		private boolean alreadySet;

		/**
		 * Creates a new {@link ListenAddressParser}.
		 *
		 * @param settings the node settings
		 */
		ListenAddressParser(@Nonnull RuntimeNodeSettings settings) {
			this.settings = settings;
			this.regex = Pattern.compile(String.format(".*/(.+):(%d|%d).*", settings.getStoragePort(),
					settings.getSslStoragePort()));
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
					if (log.isDebugEnabled()) {
						log.error(String.format("Could not parse an InetAddress (%s)", address), ex);
					}
				}
			}
		}
	}

	/**
	 * A simple implementation of {@link RunProcess.Output} class to check whether {@code cassandra node} is ready or
	 * not.
	 */
	private static final class NodeReadiness implements RunProcess.Output {

		private volatile boolean ready = false;

		@Override
		public void accept(@Nonnull String line) {
			if (!this.ready) {
				this.ready = line.matches("(?i).*listening\\s*for\\s*cql.*") ||
						line.matches("(?i).*not\\s*starting\\s*native.*");
			}
		}

		/**
		 * Tests whether {@code cassandra} node is ready or not.
		 *
		 * @return {@code true} if ready, otherwise {@code false}
		 */
		boolean isReady() {
			return this.ready;
		}
	}

	/**
	 * A basic implementation of the {@link NodeSettings} with a way to set {@code realListenAddress} and
	 * {@code realAddress}.
	 */
	private static final class RuntimeNodeSettings extends NodeSettings {

		@Nullable
		private volatile InetAddress realListenAddress;

		@Nullable
		private volatile InetAddress realAddress;

		/**
		 * Creates a new {@link RuntimeNodeSettings}.
		 *
		 * @param version a version
		 * @param properties a node properties
		 */
		RuntimeNodeSettings(@Nonnull Version version, @Nullable Map<?, ?> properties) {
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
