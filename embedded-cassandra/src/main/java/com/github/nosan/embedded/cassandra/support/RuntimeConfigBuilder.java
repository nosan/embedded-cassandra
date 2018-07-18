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

package com.github.nosan.embedded.cassandra.support;

import java.util.Objects;

import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import org.slf4j.Logger;

/**
 * {@link RuntimeConfigBuilder Runtime Config Builder} with default behaviour.
 *
 * @author Dmytro Nosan
 */
public class RuntimeConfigBuilder
		extends de.flapdoodle.embed.process.config.RuntimeConfigBuilder {

	/**
	 * Configure builder with default settings. Process output will be redirected to the
	 * console.
	 */
	public RuntimeConfigBuilder() {
		artifactStore().overwriteDefault(new ArtifactStoreBuilder().build());
		commandLinePostProcessor().overwriteDefault(new ICommandLinePostProcessor.Noop());
		ProcessOutput processOutput = new ProcessOutput(
				Processors.namedConsole("[Cassandra > output]"),
				Processors.namedConsole("[Cassandra > error]"),
				Processors.namedConsole("[Cassandra > commands]"));
		processOutput().overwriteDefault(processOutput);
	}

	/**
	 * Configure builder with default settings and logger.
	 *
	 * @param logger logger for process outputs.
	 */
	public RuntimeConfigBuilder(Logger logger) {
		Objects.requireNonNull(logger, "Logger must not be null");
		artifactStore().overwriteDefault(new ArtifactStoreBuilder(logger).build());
		commandLinePostProcessor().overwriteDefault(new ICommandLinePostProcessor.Noop());
		ProcessOutput processOutput = new ProcessOutput(
				Processors.logTo(logger, Slf4jLevel.INFO),
				Processors.logTo(logger, Slf4jLevel.ERROR),
				Processors.logTo(logger, Slf4jLevel.DEBUG));
		processOutput().overwriteDefault(processOutput);
	}

}
