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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.ConsoleOutputStreamProcessor;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.NamedOutputStreamProcessor;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.Slf4jStreamProcessor;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.ArtifactStore;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RuntimeConfigBuilder}.
 *
 * @author Dmytro Nosan
 */
public class RuntimeConfigBuilderTests {

	private static final Logger log = LoggerFactory
			.getLogger(RuntimeConfigBuilderTests.class);

	@Test
	public void defaults() throws Exception {
		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().build();

		assertThat(runtimeConfig.isDaemonProcess()).isTrue();
		assertThat(runtimeConfig.getCommandLinePostProcessor())
				.isInstanceOf(ICommandLinePostProcessor.Noop.class);

		ProcessOutput processOutput = runtimeConfig.getProcessOutput();

		assertConsoleOutputs(processOutput.getOutput(), "[Cassandra > output]");
		assertConsoleOutputs(processOutput.getError(), "[Cassandra > error]");
		assertConsoleOutputs(processOutput.getCommands(), "[Cassandra > commands]");

		IArtifactStore artifactStore = runtimeConfig.getArtifactStore();

		assertThat(artifactStore).isInstanceOf(ArtifactStore.class);

	}

	@Test
	public void defaultsLogger() throws Exception {

		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder(log).build();

		assertThat(runtimeConfig.isDaemonProcess()).isTrue();
		assertThat(runtimeConfig.getCommandLinePostProcessor())
				.isInstanceOf(ICommandLinePostProcessor.Noop.class);

		ProcessOutput processOutput = runtimeConfig.getProcessOutput();
		assertLoggerOutputs(processOutput.getOutput(), Slf4jLevel.INFO);
		assertLoggerOutputs(processOutput.getError(), Slf4jLevel.ERROR);
		assertLoggerOutputs(processOutput.getCommands(), Slf4jLevel.DEBUG);

		IArtifactStore artifactStore = runtimeConfig.getArtifactStore();

		assertThat(artifactStore).isInstanceOf(ArtifactStore.class);

	}

	private void assertLoggerOutputs(IStreamProcessor stream, Slf4jLevel level)
			throws NoSuchFieldException, IllegalAccessException {
		assertThat(stream).isInstanceOf(Slf4jStreamProcessor.class);
		Assertions.assertThat(ReflectionUtils.getField("logger", stream))
				.isEqualTo(RuntimeConfigBuilderTests.log);
		assertThat(ReflectionUtils.getField("level", stream)).isEqualTo(level);
	}

	private void assertConsoleOutputs(IStreamProcessor stream, String name)
			throws NoSuchFieldException, IllegalAccessException {
		assertThat(stream).isInstanceOf(NamedOutputStreamProcessor.class);
		NamedOutputStreamProcessor processor = (NamedOutputStreamProcessor) stream;

		assertThat(ReflectionUtils.getField("name", processor)).isEqualTo(name);
		assertThat(ReflectionUtils.getField("destination", processor))
				.isInstanceOf(ConsoleOutputStreamProcessor.class);
	}

}
