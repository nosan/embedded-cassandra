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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProcessId}.
 *
 * @author Dmytro Nosan
 */
class ProcessIdTests {

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void getPid() throws IOException {
		Process process = new ProcessBuilder("echo", "Hello world").start();
		ProcessId processId = new ProcessId(process);
		assertThat(processId.getPid()).isGreaterThan(0);
		assertThat(processId.getProcess()).isSameAs(process);
	}

	@Test
	void getPidFromFile() throws URISyntaxException {
		Path pidFile = Paths.get(getClass().getResource("/pid.file").toURI());
		Process process = new Process() {

			@Override
			public OutputStream getOutputStream() {
				throw new UnsupportedOperationException();
			}

			@Override
			public InputStream getInputStream() {
				throw new UnsupportedOperationException();
			}

			@Override
			public InputStream getErrorStream() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int waitFor() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int exitValue() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void destroy() {
				throw new UnsupportedOperationException();
			}
		};
		ProcessId processId = new ProcessId(process, pidFile);
		assertThat(processId.getPid()).isEqualTo(5141);
		assertThat(processId.getPidFile()).isSameAs(pidFile);
	}

}
