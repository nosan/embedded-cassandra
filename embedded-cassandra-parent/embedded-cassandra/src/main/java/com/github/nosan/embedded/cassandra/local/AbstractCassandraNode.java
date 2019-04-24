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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.SystemUtils;

/**
 * Abstract {@link CassandraNode} that includes common functionality.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
abstract class AbstractCassandraNode implements CassandraNode {

	private static final AtomicLong counter = new AtomicLong();

	private static final Pattern RPC_TRANSPORT_NOT_STARTING_PATTERN = Pattern
			.compile("(?i).*not\\s*starting\\s*rpc\\s*server.*");

	private static final Pattern TRANSPORT_NOT_STARTING_PATTERN = Pattern
			.compile("(?i).*((not\\s*starting\\s*client\\s*transports)|(not\\s*starting\\s*native\\s*transport)).*");

	private static final Pattern RPC_TRANSPORT_PATTERN = Pattern
			.compile("(?i).*binding\\s*thrift\\s*service\\s*to.*/(.+):(\\d+).*");

	private static final Pattern TRANSPORT_PATTERN = Pattern
			.compile("(?i).*listening\\s*for\\s*cql\\s*clients\\s*on.*/(.+):(\\d+).*");

	private static final String ENCRYPTED = "(encrypted)";

	private static final String JVM_EXTRA_OPTS = "JVM_EXTRA_OPTS";

	private static final String JAVA_HOME = "JAVA_HOME";

	final Logger log = LoggerFactory.getLogger(getClass());

	final Path workingDirectory;

	final ThreadFactory threadFactory;

	final Version version;

	private final Ports ports;

	private final List<String> jvmOptions;

	@Nullable
	private final Path javaHome;

	@Nullable
	private Settings settings;

	@Nullable
	private ProcessId processId;

	AbstractCassandraNode(Path workingDirectory, Version version, @Nullable Path javaHome, Ports ports,
			List<String> jvmOptions) {
		long id = counter.incrementAndGet();
		this.version = version;
		this.workingDirectory = workingDirectory;
		this.javaHome = javaHome;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
		this.ports = ports;
		this.threadFactory = new DefaultThreadFactory(String.format("cassandra-%d-db", id));
	}

	@Override
	public void start() throws IOException, InterruptedException {
		Map<String, String> environment = new LinkedHashMap<>();
		Path javaHome = Optional.ofNullable(this.javaHome)
				.orElseGet(() -> SystemUtils.getJavaHomeDirectory().orElse(null));
		if (javaHome != null) {
			environment.put(JAVA_HOME, javaHome.toString());
		}
		JvmOptions jvmOptions = new JvmOptions(this.ports, this.jvmOptions);
		environment.put(JVM_EXTRA_OPTS, jvmOptions.toString());
		ProcessId processId = start(environment);
		this.processId = processId;
		this.settings = getSettings(processId);
		this.log.info("Apache Cassandra Node '{}' is started", this.processId.getPid());
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

	private NodeSettings getSettings(ProcessId processId) throws InterruptedException, IOException {
		Logger logger = LoggerFactory.getLogger(Cassandra.class);
		NodeSettings settings = new NodeSettings(this.version);
		Process process = processId.getProcess();
		this.threadFactory.newThread(() -> ProcessUtils.read(process, line -> {
			logger.info(line);
			parse(line, settings);
		})).start();

		long start = System.nanoTime();
		Duration timeout = Duration.ofMinutes(2);
		long rem = timeout.toNanos();
		do {
			long pid = processId.getPid();
			if (!process.isAlive()) {
				int exitValue = process.exitValue();
				throw new IOException(String.format("Apache Cassandra Node '%s' is not alive. Exit code is '%s'."
						+ " Please see logs for more details.", pid, exitValue));
			}
			if (isStarted(settings)) {
				return settings;
			}
			if (rem > 0) {
				Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
			}
			rem = timeout.toNanos() - (System.nanoTime() - start);
		} while (rem > 0);

		throw new IllegalStateException(
				String.format("There is no way to detect whether Apache Cassandra Node '%s' %s is started or not."
						+ " Note, that Apache Cassandra <output> must be enabled."
						+ " If the <output> is enabled, and you see this message, then either you found a bug"
						+ " or Apache Cassandra is hanging.", processId, settings));
	}

	private boolean isStarted(NodeSettings settings) {
		Version version = settings.getVersion();

		if (!settings.getRpcTransportStarted().isPresent() && version.getMajor() < 4) {
			return false;
		}

		if (!settings.getTransportStarted().isPresent() && version.getMajor() > 1) {
			return false;
		}

		boolean transportStarted = settings.getTransportStarted().orElse(false);
		boolean rpcTransportStarted = settings.getRpcTransportStarted().orElse(false);

		if (transportStarted && settings.getPort().isPresent()
				&& !PortUtils.isPortBusy(settings.getRequiredAddress(), settings.getRequiredPort())) {
			return false;
		}
		if (transportStarted && settings.getSslPort().isPresent()
				&& !PortUtils.isPortBusy(settings.getRequiredAddress(), settings.getRequiredSslPort())) {
			return false;
		}

		return !rpcTransportStarted || !settings.getRpcPort().isPresent()
				|| PortUtils.isPortBusy(settings.getRequiredAddress(), settings.getRequiredRpcPort());

	}

	private void parse(String line, NodeSettings settings) {
		onMatch(TRANSPORT_PATTERN, line, matcher -> {
			onAddress(matcher.group(1), settings::setAddress);
			onPort(matcher.group(2), port -> {
				if (line.toLowerCase(Locale.ENGLISH).contains(ENCRYPTED)) {
					settings.setSslPort(port);
				}
				else {
					settings.setPort(port);
				}
			});
			settings.setTransportStarted(true);
		});
		onMatch(RPC_TRANSPORT_PATTERN, line, matcher -> {
			onAddress(matcher.group(1), settings::setAddress);
			onPort(matcher.group(2), settings::setRpcPort);
			settings.setRpcTransportStarted(true);
		});
		onMatch(TRANSPORT_NOT_STARTING_PATTERN, line, matcher -> settings.setTransportStarted(false));
		onMatch(RPC_TRANSPORT_NOT_STARTING_PATTERN, line, matcher -> settings.setRpcTransportStarted(false));
	}

	private void onAddress(String address, Consumer<InetAddress> addressConsumer) {
		try {
			addressConsumer.accept(InetAddress.getByName(address));
		}
		catch (Exception ex) {
			throw new IllegalStateException(String.format("Can not parse an address '%s'", address), ex);
		}
	}

	private void onPort(String port, IntConsumer portConsumer) {
		try {
			portConsumer.accept(Integer.parseInt(port));
		}
		catch (Exception ex) {
			throw new IllegalStateException(String.format("Can not parse a port '%s'", port), ex);
		}
	}

	private void onMatch(Pattern pattern, String line, Consumer<Matcher> matcherConsumer) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			matcherConsumer.accept(matcher);
		}
	}

}
