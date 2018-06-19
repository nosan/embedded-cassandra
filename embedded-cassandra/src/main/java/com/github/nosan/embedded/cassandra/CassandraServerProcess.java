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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import com.github.nosan.embedded.cassandra.config.CassandraProcessConfig;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.io.LogWatchStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.StreamToLineProcessor;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.runtime.AbstractProcess;
import de.flapdoodle.embed.process.runtime.ProcessControl;
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

	private File baseDir;

	public CassandraServerProcess(Distribution distribution,
			CassandraProcessConfig config, IRuntimeConfig runtimeConfig,
			CassandraServerExecutable executable) throws IOException {
		super(distribution, config, runtimeConfig, executable);
	}

	@Override
	protected void onAfterProcessStart(ProcessControl process,
			IRuntimeConfig runtimeConfig) throws IOException {

		setProcessId(getProcessId());

		ProcessOutput outputConfig = runtimeConfig.getProcessOutput();

		CassandraProcessConfig cassandraProcessConfig = getConfig();
		CassandraConfig config = cassandraProcessConfig.getConfig();

		LogWatchStreamProcessor logWatch = new LogWatchStreamProcessor(getSuccess(config),
				getFailures(), StreamToLineProcessor.wrap(outputConfig.getOutput()));
		Processors.connect(process.getReader(), logWatch);
		Processors.connect(process.getError(),
				StreamToLineProcessor.wrap(outputConfig.getError()));

		logWatch.waitForResult(cassandraProcessConfig.getTimeout().toMillis());

		if (!logWatch.isInitWithSuccess()) {
			String failureFound = logWatch.getFailureFound();
			if (failureFound == null) {
				failureFound = "\n" + "----------------------\n"
						+ "Hmm.. no failure message.. \n"
						+ "...the cause must be somewhere in the process output\n"
						+ "----------------------\n" + "" + logWatch.getOutput();
			}
			throw new IOException("Could not start process: " + failureFound);
		}
	}

	@Override
	protected List<String> getCommandLine(Distribution distribution,
			CassandraProcessConfig cassandraProcessConfig, IExtractedFileSet fileSet)
			throws IOException {
		this.baseDir = fileSet.baseDir();
		List<String> args = new ArrayList<>();

		File configurationFile = new File(this.baseDir, "cassandra-generated.yaml");
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
	protected void stopInternal() {
		log.info("Stopping the cassandra server...");
		if (!sendKillToProcess()) {
			if (!tryKillToProcess()) {
				stopProcess();
			}
		}
	}

	@Override
	protected void cleanupInternal() {
		if (this.baseDir != null) {
			Files.forceDelete(this.baseDir);
		}

	}

	private LinkedHashSet<String> getFailures() {
		return new LinkedHashSet<>(Arrays.asList("Exception encountered during startup",
				"Missing required", "Address already in use", "Port already in use"));
	}

	private String getSuccess(CassandraConfig config) {
		if (config.isStartNativeTransport()) {
			return "Starting listening for CQL";
		}
		if (config.isStartRpc()) {
			return "Listening for thrift clients";
		}
		return "Not starting";
	}

}
