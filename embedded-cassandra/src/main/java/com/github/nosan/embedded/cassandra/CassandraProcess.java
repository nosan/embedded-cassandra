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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.config.Config;
import com.github.nosan.embedded.cassandra.config.ExecutableConfig;
import com.github.nosan.embedded.cassandra.customizer.FileCustomizer;
import com.github.nosan.embedded.cassandra.customizer.JVMOptionsCustomizer;
import com.github.nosan.embedded.cassandra.customizer.JavaCompatibilityCustomizer;
import com.github.nosan.embedded.cassandra.customizer.JmxPortCustomizer;
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
		extends AbstractProcess<ExecutableConfig, CassandraExecutable, CassandraProcess> {

	private static final Logger log = LoggerFactory.getLogger(CassandraProcess.class);

	private IRuntimeConfig runtimeConfig;

	private Distribution distribution;

	private ProcessControl process;

	CassandraProcess(Distribution distribution, ExecutableConfig config,
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
			ExecutableConfig executableConfig, IExtractedFileSet fileSet)
			throws IOException {

		this.distribution = distribution;

		FileCustomizers fileCustomizers = new FileCustomizers(distribution,
				executableConfig, fileSet);
		fileCustomizers.customize();

		File configurationFile = new File(fileSet.baseDir(), "cassandra.yaml");
		try (FileWriter writer = new FileWriter(configurationFile)) {
			YamlUtils.serialize(executableConfig.getConfig(), writer);
		}

		ProcessArguments processArguments = new ProcessArguments(distribution, fileSet,
				configurationFile);

		List<String> args = processArguments.get();
		log.info("Starting the new cassandra server using directory '" + fileSet.baseDir()
				+ "' with arguments " + args);
		return args;
	}

	@Override
	protected void onAfterProcessStart(ProcessControl process,
			IRuntimeConfig runtimeConfig) throws IOException {
		this.process = process;
		Config config = getConfig().getConfig();
		Duration duration = getConfig().getTimeout();

		ProcessOutput processOutput = runtimeConfig.getProcessOutput();
		ProcessLogWait processLogWait = new ProcessLogWait(processOutput.getOutput(),
				config);
		Processors.connect(process.getReader(),
				StreamToLineProcessor.wrap(processLogWait));
		Processors.connect(process.getError(),
				StreamToLineProcessor.wrap(processOutput.getError()));

		long start = System.currentTimeMillis();

		if (!processLogWait.await(duration)) {
			if (processLogWait.hasError()) {
				throw new IOException(
						"Could not start process. " + processLogWait.getError());
			}
			throw new IOException(
					"Could not start process. Please increase your startup timeout");
		}
		long end = System.currentTimeMillis() - start;

		ProcessNetworkWait processNetworkWait = new ProcessNetworkWait(config);
		if (!processNetworkWait.await(duration.minusMillis(end))) {
			throw new IOException(
					"Could not start process. Please increase your startup timeout");
		}
		log.info("Cassandra server has been started.");

	}

	@Override
	protected void stopInternal() {
		ProcessKiller processKiller = new ProcessKiller(this.distribution,
				this.runtimeConfig, getConfig(), this.process);
		processKiller.kill();
		log.info("Cassandra server has been stopped.");
	}

	@Override
	protected void cleanupInternal() {

	}

	/**
	 * Utility class for running file customizers.
	 */
	static final class FileCustomizers {

		private final Distribution distribution;

		private final ExecutableConfig executableConfig;

		private final IExtractedFileSet fileSet;

		FileCustomizers(Distribution distribution, ExecutableConfig executableConfig,
				IExtractedFileSet fileSet) {
			this.distribution = distribution;
			this.executableConfig = executableConfig;
			this.fileSet = fileSet;
		}

		/**
		 * Invokes {@link FileCustomizer} for each {@link FileType#Library} file.
		 * @throws IOException Something happened during File customization.
		 */
		void customize() throws IOException {

			ExecutableConfig config = this.executableConfig;

			List<FileCustomizer> fileCustomizers = new ArrayList<>(
					getDefaultCustomizers());

			fileCustomizers.add(new JmxPortCustomizer(config.getJmxPort()));
			fileCustomizers.add(new JVMOptionsCustomizer(config.getJvmOptions()));
			fileCustomizers.addAll(config.getFileCustomizers());
			for (File file : this.fileSet.files(FileType.Library)) {
				for (FileCustomizer fileCustomizer : fileCustomizers) {
					fileCustomizer.customize(file, this.distribution);
				}
			}
		}

		private Collection<? extends FileCustomizer> getDefaultCustomizers() {
			return Collections.singletonList(new JavaCompatibilityCustomizer());
		}

	}

	/**
	 * Utility class for building arguments.
	 */
	static final class ProcessArguments implements Supplier<List<String>> {

		private final Distribution distribution;

		private final IExtractedFileSet fileSet;

		private final File configurationFile;

		ProcessArguments(Distribution distribution, IExtractedFileSet fileSet,
				File configurationFile) {
			this.distribution = distribution;
			this.fileSet = fileSet;
			this.configurationFile = configurationFile;
		}

		@Override
		public List<String> get() {
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

		private String getConfig() {
			StringBuilder arg = new StringBuilder();
			if (this.distribution.getPlatform() == Platform.Windows) {
				arg.append("`");
			}
			arg.append("-Dcassandra.config=file:");
			arg.append(StringUtils.repeat(File.separatorChar, 3));
			arg.append(this.configurationFile.getAbsolutePath());
			return arg.toString();
		}

	}

	/**
	 * Utility class for watching cassandra's logs and looking for error/success messages.
	 */
	static final class ProcessLogWait implements IStreamProcessor {

		private final LogWatchStreamProcessor logWatch;

		ProcessLogWait(IStreamProcessor reader, Config config) {
			this.logWatch = new LogWatchStreamProcessor(getSuccess(config), getFailures(),
					reader);
		}

		boolean await(Duration timeout) {
			this.logWatch.waitForResult(timeout.toMillis());
			return this.logWatch.isInitWithSuccess();
		}

		String getError() {
			return this.logWatch.getFailureFound();
		}

		boolean hasError() {
			return getError() != null;
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
					"ConfigurationException", "syntax error near unexpected"));
		}

	}

	/**
	 * Utility class for waiting while cassandra's network is ready.
	 */
	static final class ProcessNetworkWait {

		private final Config config;

		ProcessNetworkWait(Config config) {
			this.config = config;
		}

		boolean await(Duration timeout) {
			long startTime = System.nanoTime();
			long rem = timeout.toNanos();
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
				rem = timeout.toNanos() - (System.nanoTime() - startTime);
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
	static final class ProcessKiller {

		private static final Logger log = LoggerFactory.getLogger(ProcessKiller.class);

		private final Distribution distribution;

		private final IRuntimeConfig runtimeConfig;

		private final ExecutableConfig executableConfig;

		private final ProcessControl process;

		ProcessKiller(Distribution distribution, IRuntimeConfig runtimeConfig,
				ExecutableConfig executableConfig, ProcessControl process) {
			this.distribution = distribution;
			this.runtimeConfig = runtimeConfig;
			this.executableConfig = executableConfig;
			this.process = process;
		}

		/**
		 * Trying to stop cassandra process.
		 */
		void kill() {
			if (this.process != null && this.process.getPid() != null) {
				long pid = this.process.getPid();
				Platform platform = this.distribution.getPlatform();
				if (Processes.isProcessRunning(platform, pid)) {
					boolean killed = (platform != Platform.Windows ? killProcess(pid)
							: taskKill(pid));
					if (!killed) {
						log.warn("Process has not been stopped gracefully.");
					}
					this.process.stop();
				}
			}
		}

		private boolean killProcess(long pid) {
			IStreamProcessor output = StreamToLineProcessor
					.wrap(this.runtimeConfig.getProcessOutput().getCommands());
			ISupportConfig sc = this.executableConfig.supportConfig();
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
			ISupportConfig supportConfig = this.executableConfig.supportConfig();
			return ProcessControl.executeCommandLine(supportConfig, "[taskkill process]",
					new ProcessConfig(
							Arrays.asList("taskkill", "/F", "/T", "/pid", "" + pid),
							output));
		}

	}

}
