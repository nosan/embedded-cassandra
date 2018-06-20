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
import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.runtime.AbstractProcess;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractProcess} for an embedded cassandra server.
 *
 * @author Dmytro Nosan
 */
public class CassandraServerProcess extends
		AbstractProcess<CassandraProcessConfig, CassandraServerExecutable, CassandraServerProcess> {

	private static final Logger log = LoggerFactory
			.getLogger(CassandraServerProcess.class);

	CassandraServerProcess(Distribution distribution, CassandraProcessConfig config,
			IRuntimeConfig runtimeConfig, CassandraServerExecutable executable)
			throws IOException {
		super(distribution, config, runtimeConfig, executable);
	}

	@Override
	protected List<String> getCommandLine(Distribution distribution,
			CassandraProcessConfig cassandraProcessConfig, IExtractedFileSet fileSet)
			throws IOException {
		List<String> args = new ArrayList<>();

		File configurationFile = new File(fileSet.baseDir(),
				"cassandra-" + UUID.randomUUID() + ".yaml");
		try (FileWriter writer = new FileWriter(configurationFile)) {
			YamlUtils.serialize(cassandraProcessConfig.getConfig(), writer);
		}
		args.add(fileSet.executable().getAbsolutePath());
		args.add("-f");
		args.add("-Dcassandra.config=" + configurationFile.toURI());
		log.info("Starting the new cassandra server using directory '" + fileSet.baseDir()
				+ "' with arguments " + args);
		return args;
	}

	@Override
	protected void onAfterProcessStart(ProcessControl process,
			IRuntimeConfig runtimeConfig) throws IOException {

		ProcessOutput outputConfig = runtimeConfig.getProcessOutput();
		CassandraProcessConfig cassandraProcessConfig = getConfig();
		Duration timeout = cassandraProcessConfig.getTimeout();
		CassandraConfig config = cassandraProcessConfig.getConfig();

		LogWatchStreamProcessor logWatch = new LogWatchStreamProcessor(getSuccess(config),
				getFailures(), StreamToLineProcessor.wrap(outputConfig.getOutput()));

		Processors.connect(process.getReader(), logWatch);
		Processors.connect(process.getError(),
				StreamToLineProcessor.wrap(outputConfig.getError()));

		setProcessId(getProcessId());

		logWatch.waitForResult(timeout.toMillis());

		if (!logWatch.isInitWithSuccess()) {
			String failureFound = logWatch.getFailureFound();
			throw new IOException("Could not start process. "
					+ ObjectUtils.defaultIfNull(failureFound, ""));
		}

		if (!tryConnect(timeout, config)) {
			throw new IOException(
					"Could not start process. Please increase your " + "startup timeout");
		}

		log.info("Embedded Cassandra has been started.");

	}

	@Override
	protected void stopInternal() {
		log.info("Stopping the cassandra server...");
		if (!tryStopProcess()) {
			log.warn("Could not stop cassandra server.");
		}
		stopProcess();
	}

	@Override
	protected void cleanupInternal() {

	}

	private LinkedHashSet<String> getFailures() {
		return new LinkedHashSet<>(Arrays.asList("encountered during startup",
				"Missing required", "Address already in use", "Port already in use"));
	}

	private String getSuccess(CassandraConfig config) {

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

	private boolean tryConnect(Duration timeout, CassandraConfig config) {
		long startTime = System.nanoTime();
		long rem = timeout.toNanos();
		do {
			if (tryConnect(config)) {
				return true;
			}
			if (rem > 0) {
				try {
					Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 500));
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

	private boolean tryConnect(CassandraConfig config) {
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

	private boolean tryStopProcess() {
		return sendKillToProcess() || sendTermToProcess() || tryKillToProcess();
	}

}
