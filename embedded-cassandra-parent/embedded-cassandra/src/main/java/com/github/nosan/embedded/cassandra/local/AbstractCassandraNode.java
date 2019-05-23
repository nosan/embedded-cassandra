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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.MDCThreadFactory;
import com.github.nosan.embedded.cassandra.util.SystemUtils;

/**
 * Abstract {@link CassandraNode} that includes common functionality.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
abstract class AbstractCassandraNode implements CassandraNode {

	private static final AtomicLong nodeNumber = new AtomicLong();

	private static final Pattern TRANSPORT_START_PATTERN = Pattern
			.compile("(?i).*listening\\s*for\\s*cql\\s*clients\\s*on.*/(.+):(\\d+).*");

	private static final Pattern TRANSPORT_STOP_PATTERN = Pattern
			.compile("(?i).*stop\\s*listening\\s*for\\s*cql\\s*clients.*");

	private static final Pattern TRANSPORT_NOT_START_PATTERN = Pattern
			.compile("(?i).*((not\\s*starting\\s*client\\s*transports)|(not\\s*starting\\s*native\\s*transport)).*");

	private static final Pattern RPC_TRANSPORT_NOT_START_PATTERN = Pattern
			.compile("(?i).*not\\s*starting\\s*rpc\\s*server.*");

	private static final Pattern RPC_TRANSPORT_START_PATTERN = Pattern
			.compile("(?i).*binding\\s*thrift\\s*service\\s*to.*/(.+):(\\d+).*");

	private static final Pattern RPC_TRANSPORT_STOP_PATTERN = Pattern
			.compile("(?i).*stop\\s*listening\\s*to\\s*thrift\\s*clients.*");

	private static final String ENCRYPTED = "(encrypted)";

	private static final String JVM_EXTRA_OPTS = "JVM_EXTRA_OPTS";

	private static final String JAVA_HOME = "JAVA_HOME";

	final Logger log = LoggerFactory.getLogger(getClass());

	final Path workingDirectory;

	final Version version;

	final ThreadFactory threadFactory;

	private final JvmParameters jvmParameters;

	private final Duration startupTimeout;

	@Nullable
	private final Path javaHome;

	@Nullable
	private volatile Settings settings;

	@Nullable
	private volatile ProcessId processId;

	AbstractCassandraNode(Path workingDirectory, Version version, Duration startupTimeout,
			boolean daemon, @Nullable Path javaHome, JvmParameters jvmParameters) {
		this.version = version;
		this.workingDirectory = workingDirectory;
		this.startupTimeout = startupTimeout;
		this.javaHome = javaHome;
		this.jvmParameters = jvmParameters;
		this.threadFactory = new MDCThreadFactory(String.format("cassandra-%d-db", nodeNumber.incrementAndGet()),
				daemon);
	}

	@Override
	public void start() throws IOException, InterruptedException {
		Map<String, String> environment = new LinkedHashMap<>();
		Path javaHome = Optional.ofNullable(this.javaHome)
				.orElseGet(() -> SystemUtils.getJavaHomeDirectory().orElse(null));
		if (javaHome != null) {
			environment.put(JAVA_HOME, javaHome.toString());
		}
		List<String> parameters = this.jvmParameters.getParameters();
		if (!parameters.isEmpty()) {
			environment.put(JVM_EXTRA_OPTS, String.join(" ", parameters));
		}
		ProcessId processId = start(environment);
		this.processId = processId;
		this.settings = awaitStart(processId);
		this.log.info("Apache Cassandra Node '{}' is started", processId.getPid());
	}

	@Override
	public void stop() throws IOException, InterruptedException {
		ProcessId processId = this.processId;
		Process process = (processId != null) ? processId.getProcess() : null;
		if (processId != null && process.isAlive()) {
			long pid = processId.getPid();
			if (terminate(processId) != 0) {
				process.destroy();
			}
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				if (kill(processId) != 0) {
					process.destroy();
				}
				if (!process.waitFor(5, TimeUnit.SECONDS)) {
					process.destroyForcibly();
				}
			}
			if (process.isAlive()) {
				throw new IOException(String.format("Apache Casandra Node '%s' is not stopped.", pid));
			}
			this.processId = null;
			this.settings = null;
			this.log.info("Apache Cassandra Node '{}' is stopped", pid);
		}
	}

	@Override
	public Settings getSettings() throws IllegalStateException {
		return Optional.ofNullable(this.settings)
				.orElseThrow(() -> new IllegalStateException(String.format("Apache Cassandra '%s' is not running.",
						getVersion())));
	}

	@Override
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Starts the Apache Cassandra.
	 *
	 * @param environment the environment variables
	 * @return the process that handles {@code Cassandra} process
	 * @throws IOException in the case of I/O errors
	 * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
	 */
	abstract ProcessId start(Map<String, String> environment) throws IOException, InterruptedException;

	/**
	 * Terminates the Apache Cassandra.
	 *
	 * @param processId the process that handles {@code Cassandra} process
	 * @return the exit code
	 * @throws IOException in the case of I/O errors
	 * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
	 */
	abstract int terminate(ProcessId processId) throws IOException, InterruptedException;

	/**
	 * Kills the Apache Cassandra.
	 *
	 * @param processId the process that handles {@code Cassandra} process
	 * @return the exit code
	 * @throws IOException in the case of I/O errors
	 * @throws InterruptedException if the current thread is {@link Thread#interrupt() interrupted} by another thread
	 */
	abstract int kill(ProcessId processId) throws IOException, InterruptedException;

	private NodeSettings awaitStart(ProcessId processId) throws InterruptedException, IOException {
		Logger logger = LoggerFactory.getLogger(Cassandra.class);
		NodeSettings settings = new NodeSettings(this.version);
		Process process = processId.getProcess();
		AtomicBoolean capture = new AtomicBoolean(true);
		Deque<String> lines = new ConcurrentLinkedDeque<>();
		Thread thread = this.threadFactory.newThread(() -> ProcessUtils.read(process, line -> {
			if (capture.get()) {
				if (lines.size() == 20) {
					lines.removeFirst();
				}
				lines.addLast(line);
			}
			logger.info(line);
			parse(line, settings);
		}));

		thread.start();

		Duration timeout = this.startupTimeout;
		try {
			long start = System.nanoTime();
			long rem = timeout.toNanos();
			do {
				long pid = processId.getPid();
				if (!process.isAlive()) {
					try {
						thread.join(1000);
					}
					catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					throw new IOException(String.format("Apache Cassandra Node '%s' is not alive."
									+ " Please see logs for more details.%n\t%s", pid,
							String.join(String.format("%n\t"), lines)));
				}
				if (isStarted(settings)) {
					return settings;
				}
				if (rem > 0) {
					Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
				}
				rem = timeout.toNanos() - (System.nanoTime() - start);
			} while (rem > 0);
		}
		finally {
			capture.set(false);
			lines.clear();
		}
		String message = String.format("Apache Cassandra Node '%s' is in unknown state.%n", processId.getPid())
				+ String.format("\tDescription:%n")
				+ String.format("\t\t- Settings: %s%n", settings)
				+ String.format("\t\t- Startup timeout: '%s second(s)'%n", timeout.getSeconds())
				+ String.format("\t\t- PID: '%s'%n", processId.getPid())
				+ String.format("\tAction:%n")
				+ String.format("\t\t- Try to increase a startup timeout%n")
				+ String.format("\t\t- Apache Cassandra <output> must be enabled.%n")
				+ String.format("\t\t- If you feel this is a bug. Could you please copy logs and create an issue.%n");
		throw new IllegalStateException(message);
	}

	private boolean isStarted(NodeSettings settings) {
		Boolean transportStarted = settings.transportStarted().orElse(null);
		Boolean rpcTransportStarted = settings.rpcTransportStarted().orElse(null);
		if (transportStarted == null || rpcTransportStarted == null) {
			return false;
		}
		if (transportStarted) {
			InetAddress address = settings.address().orElse(null);
			Integer port = settings.port().orElse(null);
			Integer sslPort = settings.sslPort().orElse(null);
			if ((port != null || sslPort != null) && address == null) {
				return false;
			}
			if (port != null && port != 0 && !SocketUtils.connect(address, port)) {
				return false;
			}
			if (sslPort != null && sslPort != 0 && !SocketUtils.connect(address, sslPort)) {
				return false;
			}
		}
		if (rpcTransportStarted) {
			InetAddress address = settings.address().orElse(null);
			Integer rpcPort = settings.rpcPort().orElse(null);
			if (rpcPort == null || address == null) {
				return false;
			}
			return rpcPort == 0 || SocketUtils.connect(address, rpcPort);
		}
		return true;
	}

	private void parse(String line, NodeSettings settings) {
		onMatch(new Pattern[]{TRANSPORT_START_PATTERN}, line, matcher -> {
			InetAddress address = SocketUtils.getAddress(matcher.group(1));
			int port = SocketUtils.getPort(matcher.group(2));
			boolean ssl = line.toLowerCase(Locale.ENGLISH).contains(ENCRYPTED);
			settings.setAddress(address);
			if (ssl) {
				settings.setSslPort(port);
			}
			else {
				settings.setPort(port);
			}
			settings.setTransportStarted(true);
		});
		onMatch(new Pattern[]{RPC_TRANSPORT_START_PATTERN}, line, matcher -> {
			InetAddress address = SocketUtils.getAddress(matcher.group(1));
			int port = SocketUtils.getPort(matcher.group(2));
			settings.setRpcPort(port);
			settings.setRpcAddress(address);
			settings.setRpcTransportStarted(true);
		});
		onMatch(new Pattern[]{TRANSPORT_NOT_START_PATTERN, TRANSPORT_STOP_PATTERN}, line, matcher -> {
			settings.setTransportStarted(false);
			settings.setAddress(null);
			settings.setSslPort(null);
			settings.setPort(null);
		});
		onMatch(new Pattern[]{RPC_TRANSPORT_NOT_START_PATTERN, RPC_TRANSPORT_STOP_PATTERN}, line, matcher -> {
			settings.setRpcTransportStarted(false);
			settings.setRpcPort(null);
			settings.setRpcAddress(null);
		});
	}

	private void onMatch(Pattern[] patterns, String line, Consumer<? super Matcher> matcherConsumer) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				matcherConsumer.accept(matcher);
				return;
			}
		}
	}

}
