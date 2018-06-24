/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import com.github.nosan.embedded.cassandra.config.Config;
import com.github.nosan.embedded.cassandra.customizer.FileCustomizer;
import com.github.nosan.embedded.cassandra.util.YamlUtils;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.ISupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.process.ProcessConfig;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.AbstractProcess;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.runtime.Processes;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractProcess} for an embedded cassandra server.
 *
 * @author Dmytro Nosan
 */
public class CassandraProcess
		extends AbstractProcess<CassandraConfig, CassandraExecutable, CassandraProcess> {

	private static final Logger log = LoggerFactory.getLogger(CassandraProcess.class);

	private ProcessKiller processKiller;

	private IRuntimeConfig runtimeConfig;

	CassandraProcess(Distribution distribution, CassandraConfig config,
			IRuntimeConfig runtimeConfig, CassandraExecutable executable)
			throws IOException {
		super(distribution, config, runtimeConfig, executable);
	}

	@Override
	protected void onBeforeProcess(IRuntimeConfig runtimeConfig) throws IOException {
		this.runtimeConfig = runtimeConfig;
		super.onBeforeProcess(runtimeConfig);
	}

	@Override
	protected List<String> getCommandLine(Distribution distribution,
			CassandraConfig cassandraConfig, IExtractedFileSet fileSet)
			throws IOException {

		this.processKiller = new ProcessKiller(distribution, this.runtimeConfig,
				cassandraConfig);

		FileCustomizer fileCustomizer = cassandraConfig.getFileCustomizer();
		for (File file : fileSet.files(FileType.Library)) {
			fileCustomizer.customize(file, distribution);
		}

		ProcessCommand processCommand = new ProcessCommand(distribution, cassandraConfig,
				fileSet);

		List<String> args = processCommand.getArgs();
		log.info("Starting the new cassandra server using directory '" + fileSet.baseDir()
				+ "' with arguments " + args);
		return args;
	}

	@Override
	protected void onAfterProcessStart(ProcessControl process,
			IRuntimeConfig runtimeConfig) throws IOException {

		ProcessOutput processOutput = runtimeConfig.getProcessOutput();
		CassandraConfig cassandraConfig = getConfig();
		Duration timeout = cassandraConfig.getTimeout();

		LogWatchWaiter logWatchWaiter = new LogWatchWaiter(processOutput,
				cassandraConfig.getConfig(), timeout);

		Processors.connect(process.getReader(), logWatchWaiter);
		Processors.connect(process.getError(),
				StreamToLineProcessor.wrap(processOutput.getError()));

		long mark = System.currentTimeMillis();

		if (!logWatchWaiter.waitForResult()) {
			if (logWatchWaiter.getError() != null) {
				throw new IOException(
						"Could not start process. " + logWatchWaiter.getError());
			}
			throw new IOException(
					"Could not start process. Please increase your startup timeout");

		}

		NetworkWaiter networkWaiter = new NetworkWaiter(cassandraConfig.getConfig(),
				timeout.minusMillis(System.currentTimeMillis() - mark));

		if (!networkWaiter.waitForResult()) {
			throw new IOException(
					"Could not start process. Please increase your startup timeout");

		}

	}

	@Override
	protected void stopInternal() {
		for (int i = 0; i < 3; i++) {
			try {
				killProcess();
			}
			catch (RuntimeException ex) {
				try {
					TimeUnit.SECONDS.sleep(5);
				}
				catch (InterruptedException ignore) {
					Thread.currentThread().interrupt();
				}
			}
		}

		if (isProcessRunning()) {
			ISupportConfig supportConfig = getConfig().supportConfig();
			IllegalStateException ex = new IllegalStateException(
					"Couldn't kill " + supportConfig.getName() + " " + "process!");
			log.error(supportConfig.messageOnException(getClass(), ex));
			throw ex;

		}
		log.info("Cassandra server has been stopped.");

	}

	@Override
	protected void cleanupInternal() {

	}

	private void killProcess() {
		if (isProcessRunning()) {
			log.info("Stopping the cassandra server...");
			if (this.processKiller != null && !this.processKiller.kill(getProcessId())) {
				log.warn("Could not stop cassandra server. Trying to destroy it.");
			}
			stopProcess();
		}
	}

	/**
	 * Utility class for watching cassandra logs and looking for error/success messages.
	 */
	private static final class LogWatchWaiter implements IStreamProcessor {

		private final Duration timeout;

		private final LogWatchStreamProcessor logWatch;

		LogWatchWaiter(ProcessOutput processOutput, Config config, Duration timeout) {
			this.logWatch = new LogWatchStreamProcessor(getSuccess(config), getFailures(),
					StreamToLineProcessor.wrap(processOutput.getOutput()));
			this.timeout = timeout;
		}

		boolean waitForResult() {
			this.logWatch.waitForResult(this.timeout.toMillis());
			return this.logWatch.isInitWithSuccess();
		}

		String getError() {
			return this.logWatch.getFailureFound();
		}

		@Override
		public void process(String block) {
			this.logWatch.process(block);
		}

		@Override
		public void onProcessed() {
			this.logWatch.onProcessed();
		}

		private String getSuccess(Config config) {

			if (config.isStartNativeTransport()) {
				return "Starting listening for CQL";
			}

			if (config.isStartRpc()) {
				return "Listening for thrift clients";
			}

			if (config.getListenInterface() != null || config.getRpcInterface() != null) {
				return "Starting Messaging Service";
			}

			return "Not starting";
		}

		private LinkedHashSet<String> getFailures() {
			return new LinkedHashSet<>(Arrays.asList("encountered during startup",
					"Missing required", "Address already in use", "Port already in use",
					"ConfigurationException"));
		}

	}

	/**
	 * Utility class for waiting while cassandras network is not ready.
	 */
	private static final class NetworkWaiter {

		private final Config config;

		private final Duration timeout;

		NetworkWaiter(Config config, Duration timeout) {
			this.config = config;
			this.timeout = timeout;
		}

		boolean waitForResult() {
			long startTime = System.nanoTime();
			long rem = this.timeout.toNanos();
			do {
				if (tryConnect(this.config)) {
					return true;
				}
				if (rem > 0) {
					try {
						Thread.sleep(
								Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 500));
					}
					catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
				rem = this.timeout.toNanos() - (System.nanoTime() - startTime);
			}
			while (rem > 0);
			return false;

		}

		private boolean tryConnect(Config config) {
			if (config.isStartNativeTransport()) {
				return tryConnect(
						ObjectUtils.defaultIfNull(config.getListenAddress(), "localhost"),
						config.getNativeTransportPort());
			}
			else if (config.isStartRpc()) {
				return tryConnect(
						ObjectUtils.defaultIfNull(config.getRpcAddress(), "localhost"),
						config.getRpcPort());
			}
			return true;
		}

		private boolean tryConnect(String host, int port) {
			try (Socket ignored = new Socket(host, port)) {
				return true;
			}
			catch (IOException ex) {
				return false;
			}
		}

	}

	/**
	 * Utility class for destroying cassandra process.
	 */
	private static final class ProcessKiller {

		private final Distribution distribution;

		private final IRuntimeConfig runtimeConfig;

		private final CassandraConfig cassandraConfig;

		private ProcessKiller(Distribution distribution, IRuntimeConfig runtimeConfig,
				CassandraConfig cassandraConfig) {
			this.distribution = distribution;
			this.runtimeConfig = runtimeConfig;
			this.cassandraConfig = cassandraConfig;
		}

		boolean kill(long pid) {
			Platform platform = this.distribution.getPlatform();
			if (platform.isUnixLike()) {
				return killProcess(pid);
			}
			return taskKill(pid);

		}

		private boolean killProcess(long pid) {
			IStreamProcessor output = StreamToLineProcessor
					.wrap(this.runtimeConfig.getProcessOutput().getCommands());
			ISupportConfig sc = this.cassandraConfig.supportConfig();
			Platform pl = this.distribution.getPlatform();
			return Processes.killProcess(sc, pl, output, pid)
					|| Processes.termProcess(sc, pl, output, pid)
					|| ProcessControl.executeCommandLine(sc, "[kill process]",
							new ProcessConfig(Arrays.asList("kill", "-9", "" + pid),
									output));
		}

		private boolean taskKill(long pid) {
			IStreamProcessor output = StreamToLineProcessor
					.wrap(this.runtimeConfig.getProcessOutput().getCommands());
			ISupportConfig supportConfig = this.cassandraConfig.supportConfig();
			return ProcessControl.executeCommandLine(supportConfig, "[taskkill process]",
					new ProcessConfig(
							Arrays.asList("taskkill", "/F", "/T", "/pid", "" + pid),
							output));
		}

	}

	/**
	 * Utility class for building command line.
	 */
	private static final class ProcessCommand {

		private final Distribution distribution;

		private final CassandraConfig processConfig;

		private final IExtractedFileSet fileSet;

		ProcessCommand(Distribution distribution, CassandraConfig processConfig,
				IExtractedFileSet fileSet) {
			this.distribution = distribution;
			this.processConfig = processConfig;
			this.fileSet = fileSet;
		}

		List<String> getArgs() throws IOException {
			List<String> args = new ArrayList<>();
			if (this.distribution.getPlatform() == Platform.Windows) {
				args.add("powershell");
				args.add("-ExecutionPolicy");
				args.add("Bypass");
			}
			args.add(this.fileSet.executable().getAbsolutePath());
			args.add("-f");
			args.add(getConfig());
			return args;
		}

		private String getConfig() throws IOException {
			File configurationFile = new File(this.fileSet.baseDir(),
					"cassandra-" + UUID.randomUUID() + ".yaml");
			try (FileWriter writer = new FileWriter(configurationFile)) {
				YamlUtils.serialize(this.processConfig.getConfig(), writer);
			}
			StringBuilder arg = new StringBuilder();
			if (this.distribution.getPlatform() == Platform.Windows) {
				arg.append("`");
			}
			arg.append("-Dcassandra.config=file:");
			arg.append(StringUtils.repeat(File.separatorChar, 3));
			arg.append(configurationFile.getAbsolutePath());
			return arg.toString();
		}

	}

}
