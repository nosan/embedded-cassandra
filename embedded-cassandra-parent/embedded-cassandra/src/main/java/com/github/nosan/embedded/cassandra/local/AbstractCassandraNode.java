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
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.Nullable;
import com.github.nosan.embedded.cassandra.util.NetworkUtils;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.StringUtils;
import com.github.nosan.embedded.cassandra.util.SystemProperty;

/**
 * The common implementation of the {@link CassandraNode}.
 *
 * @author Dmytro Nosan
 * @since 1.4.1
 */
abstract class AbstractCassandraNode implements CassandraNode {

	private static final AtomicLong instanceCounter = new AtomicLong();

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final long instance = instanceCounter.incrementAndGet();

	private final ThreadFactory threadFactory = runnable -> {
		Thread thread = new Thread(runnable, String.format("cassandra-%d", this.instance));
		thread.setDaemon(true);
		return thread;
	};

	private final Path workingDirectory;

	private final Version version;

	private final Duration timeout;

	private final List<String> jvmOptions;

	private final int jmxPort;

	@Nullable
	private final Path javaHome;

	@Nullable
	private ProcessId processId;

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
	AbstractCassandraNode(Path workingDirectory, Version version, Duration timeout,
			List<String> jvmOptions, @Nullable Path javaHome, int jmxPort) {
		this.workingDirectory = workingDirectory;
		this.version = version;
		this.timeout = timeout;
		this.javaHome = javaHome;
		this.jmxPort = jmxPort;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
	}

	@Override
	public final Settings start() throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder()
				.directory(this.workingDirectory.toFile())
				.redirectErrorStream(true);
		RuntimeNodeSettings settings = getSettings(this.workingDirectory, this.version);

		List<String> jvmOptions = new ArrayList<>();
		jvmOptions.add(String.format("-Dcassandra.jmx.local.port=%d", getJmxPort(this.jmxPort)));
		jvmOptions.addAll(this.jvmOptions);
		String javaHome = getJavaHome(this.javaHome);
		if (StringUtils.hasText(javaHome)) {
			processBuilder.environment().put("JAVA_HOME", javaHome);
		}
		processBuilder.environment().put("JVM_EXTRA_OPTS", String.join(" ", jvmOptions));

		CompositeConsumer<String> consumer = new CompositeConsumer<>();
		NodeReadiness nodeReadiness = new NodeReadiness(consumer);
		TransportReadiness transportReadiness = new TransportReadiness(settings);
		BufferedConsumer bufferedConsumer = new BufferedConsumer(5);
		consumer.add(new LoggerConsumer(LoggerFactory.getLogger(Cassandra.class)));
		consumer.add(bufferedConsumer);
		consumer.add(nodeReadiness);
		consumer.add(new RpcAddressConsumer(settings, consumer));
		consumer.add(new ListenAddressConsumer(settings, consumer));

		ProcessId processId = start(processBuilder, this.threadFactory,
				new FilteredConsumer<>(consumer, new StackTraceFilter()));
		this.processId = processId;
		Process process = processId.getProcess();

		long start = System.nanoTime();
		long timeout = this.timeout.toNanos();
		long rem = timeout;
		do {
			if (!process.isAlive()) {
				throw new IOException(String.format("Cassandra Node '%s' is not alive. Exit code is '%s'. " +
								"Please see logs for more details.%n%s", processId.getPid(), process.exitValue(),
						bufferedConsumer));
			}
			if (transportReadiness.get() && nodeReadiness.get()) {
				this.log.info("Cassandra Node '{}' has been started", processId.getPid());
				consumer.remove(bufferedConsumer);
				consumer.remove(nodeReadiness);
				return settings;
			}

			if (rem > 0) {
				Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
			}
			rem = timeout - (System.nanoTime() - start);
		}
		while (rem > 0);
		throw new IOException(String.format("Cassandra Node '%s' has not been started," +
				" seems like (%d) milliseconds is not enough.", processId.getPid(), this.timeout.toMillis()));
	}

	@Override
	public final void stop() throws IOException, InterruptedException {
		ProcessId processId = this.processId;
		Process process = (processId != null) ? processId.getProcess() : null;
		if (processId != null && process.isAlive()) {
			try {
				ProcessBuilder processBuilder = new ProcessBuilder()
						.directory(this.workingDirectory.toFile())
						.redirectErrorStream(true);
				stop(processId, processBuilder, this.threadFactory, new LoggerConsumer(this.log));
			}
			catch (IOException ex) {
				this.log.error(String.format("Could not stop Cassandra Node '%s'.", processId.getPid()), ex);
			}
			if (!process.waitFor(10, TimeUnit.SECONDS)) {
				throw new IOException(String.format("Casandra Node '%s' has not been stopped.", processId.getPid()));
			}
			this.processId = null;
			this.log.info("Cassandra Node '{}' has been stopped", processId.getPid());
		}
	}

	protected abstract ProcessId start(ProcessBuilder processBuilder, ThreadFactory threadFactory,
			Consumer<? super String> consumer) throws IOException;

	protected abstract void stop(ProcessId processId, ProcessBuilder processBuilder, ThreadFactory threadFactory,
			Consumer<? super String> consumer) throws IOException;

	private int getJmxPort(int jmxPort) {
		return jmxPort != 0 ? jmxPort : PortUtils.getPort();
	}

	@Nullable
	private static String getJavaHome(@Nullable Path javaHome) {
		if (javaHome != null) {
			return String.valueOf(javaHome.toAbsolutePath());
		}
		return new SystemProperty("java.home").get();
	}

	private static RuntimeNodeSettings getSettings(Path directory, Version version) throws IOException {
		Path target = directory.resolve("conf/cassandra.yaml");
		try (InputStream is = Files.newInputStream(target)) {
			Yaml yaml = new Yaml();
			return new RuntimeNodeSettings(version, yaml.loadAs(is, Map.class));
		}
	}

	private static final class TransportReadiness implements Supplier<Boolean> {

		private final Settings settings;

		TransportReadiness(Settings settings) {
			this.settings = settings;
		}

		@Override
		public Boolean get() {
			Settings settings = this.settings;
			Predicate<Settings> condition = (ignore) -> true;
			if (settings.isStartNativeTransport()) {
				condition = condition.and(TransportReadiness::isNativeTransportReady);
			}
			if (settings.isStartRpc()) {
				condition = condition.and(TransportReadiness::isRpcTransportReady);
			}
			return condition.test(settings);
		}

		private static boolean isNativeTransportReady(Settings settings) {
			InetAddress address = settings.getRealAddress();
			int port = settings.getPort();
			Integer sslPort = settings.getSslPort();
			if (sslPort != null) {
				return PortUtils.isPortBusy(address, port) && PortUtils.isPortBusy(address, sslPort);
			}
			return PortUtils.isPortBusy(address, port);
		}

		private static boolean isRpcTransportReady(Settings settings) {
			InetAddress address = settings.getRealAddress();
			int port = settings.getRpcPort();
			return PortUtils.isPortBusy(address, port);
		}

	}

	private static final class NodeReadiness implements Consumer<String>, Supplier<Boolean> {

		private static final long DEFAULT_STARTUP_TIMEOUT = 20000L;

		private final CompositeConsumer<? extends String> consumer;

		private final long start;

		private volatile boolean ready = false;

		NodeReadiness(CompositeConsumer<? extends String> consumer) {
			this.consumer = consumer;
			this.start = System.currentTimeMillis();
		}

		@Override
		public void accept(String line) {
			long elapsed = System.currentTimeMillis() - this.start;
			if (match(line) || elapsed > DEFAULT_STARTUP_TIMEOUT) {
				this.ready = true;
				this.consumer.remove(this);
			}
		}

		@Override
		public Boolean get() {
			return this.ready;
		}

		private static boolean match(String line) {
			return line.matches("(?i).*listening.*clients.*") ||
					line.matches("(?i).*not\\s*starting.*as\\s*requested.*");
		}

	}

	private static final class RpcAddressConsumer implements Consumer<String> {

		private static final Logger log = LoggerFactory.getLogger(RpcAddressConsumer.class);

		private final RuntimeNodeSettings settings;

		private final Pattern regex;

		private final CompositeConsumer<? extends String> consumer;

		RpcAddressConsumer(RuntimeNodeSettings settings, CompositeConsumer<? extends String> consumer) {
			this.settings = settings;
			this.regex = Pattern.compile(String.format(".*/(.+):\\s*(%d|%d).*", settings.getPort(),
					settings.getSslPort()));
			this.consumer = consumer;
		}

		@Override
		public void accept(String line) {
			Matcher matcher = this.regex.matcher(line);
			if (matcher.matches()) {
				String address = matcher.group(1).trim();
				try {
					this.settings.setRealAddress(NetworkUtils.getInetAddress(address.trim()));
					this.consumer.remove(this);
				}
				catch (Throwable ex) {
					if (log.isDebugEnabled()) {
						log.error(String.format("Could not parse an InetAddress '%s'", address), ex);
					}
				}
			}
		}

	}

	private static final class ListenAddressConsumer implements Consumer<String> {

		private static final Logger log = LoggerFactory.getLogger(ListenAddressConsumer.class);

		private final RuntimeNodeSettings settings;

		private final Pattern regex;

		private final CompositeConsumer<? extends String> consumer;

		ListenAddressConsumer(RuntimeNodeSettings settings, CompositeConsumer<? extends String> consumer) {
			this.settings = settings;
			this.regex = Pattern.compile(String.format(".*/(.+):\\s*(%d|%d).*", settings.getStoragePort(),
					settings.getSslStoragePort()));
			this.consumer = consumer;
		}

		@Override
		public void accept(String line) {
			Matcher matcher = this.regex.matcher(line);
			if (matcher.matches()) {
				String address = matcher.group(1).trim();
				try {
					this.settings.setRealListenAddress(NetworkUtils.getInetAddress(address.trim()));
					this.consumer.remove(this);
				}
				catch (Throwable ex) {
					if (log.isDebugEnabled()) {
						log.error(String.format("Could not parse an InetAddress '%s'", address), ex);
					}
				}
			}
		}

	}

	private static final class LoggerConsumer implements Consumer<String> {

		private final Logger logger;

		LoggerConsumer(Logger logger) {
			this.logger = logger;
		}

		@Override
		public void accept(String line) {
			this.logger.info(line);
		}

	}

	private static final class RuntimeNodeSettings extends NodeSettings {

		@Nullable
		private volatile InetAddress realListenAddress;

		@Nullable
		private volatile InetAddress realAddress;

		RuntimeNodeSettings(Version version, @Nullable Map<?, ?> properties) {
			super(version, properties);
		}

		@Override
		public InetAddress getRealAddress() {
			InetAddress address = this.realAddress;
			if (address != null) {
				return address;
			}
			return super.getRealAddress();
		}

		/**
		 * Initializes the value for the {@link #getRealAddress()} attribute.
		 *
		 * @param realAddress The value for realAddress
		 */
		void setRealAddress(@Nullable InetAddress realAddress) {
			this.realAddress = realAddress;
		}

		@Override
		public InetAddress getRealListenAddress() {
			InetAddress address = this.realListenAddress;
			if (address != null) {
				return address;
			}
			return super.getRealListenAddress();
		}

		/**
		 * Initializes the value for the {@link #getRealListenAddress()}  attribute.
		 *
		 * @param realListenAddress The value for realListenAddress
		 */
		void setRealListenAddress(@Nullable InetAddress realListenAddress) {
			this.realListenAddress = realListenAddress;
		}

	}

	private static final class BufferedConsumer implements Consumer<String> {

		private final Deque<String> lines = new ConcurrentLinkedDeque<>();

		private final int count;

		BufferedConsumer(int count) {
			this.count = count;
		}

		@Override
		public void accept(String line) {
			if (this.lines.size() > this.count) {
				this.lines.removeFirst();
			}
			this.lines.addLast(line);
		}

		@Override
		public String toString() {
			return this.lines.stream().collect(Collectors.joining(System.lineSeparator()));
		}

	}

	private static final class StackTraceFilter implements Predicate<String> {

		private static final Pattern STACKTRACE_PATTERN = Pattern.compile("\\s+(at|\\.{3})\\s+.*");

		@Override
		public boolean test(String line) {
			return !STACKTRACE_PATTERN.matcher(line).matches();
		}

	}

}
