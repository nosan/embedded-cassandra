/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WindowsCassandraDatabase}.
 *
 * @author Dmytro Nosan
 */
class WindowsCassandraDatabaseTests {

	private final Process process = mock(Process.class);

	private WindowsCassandraDatabase database;

	@BeforeEach
	void setUp(@TempDir Path workingDirectory) {
		this.database = spy(new WindowsCassandraDatabase("test", CassandraBuilder.DEFAULT_VERSION,
				workingDirectory.resolve("conf/cassandra.yaml"), workingDirectory,
				Collections.singletonMap("TEST", "TEST"), new LinkedHashMap<>(), new LinkedHashMap<>(),
				new LinkedHashSet<>(), workingDirectory.resolve("bin/pid")));

	}

	@Test
	void doStartPowershell() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();

		doReturn(this.process).when(database).start(eq("test:bin/cassandra.ps1"), any());

		database.start();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database).start(eq("test:bin/cassandra.ps1"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getValue();
		assertThat(processBuilder.command()).containsExactly("powershell",
				"-ExecutionPolicy", "Unrestricted", workingDirectory.resolve("bin/cassandra.ps1").toString(), "-p",
				workingDirectory.resolve("bin/pid").toString(), "-a", "-f");
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());

	}

	@Test
	void doStartBat() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();

		doThrow(new IOException("powershell")).when(database).start(eq("test:bin/cassandra.ps1"), any());
		doReturn(this.process).when(database).start(eq("test:bin/cassandra.bat"), any());

		database.start();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database).start(eq("test:bin/cassandra.bat"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getValue();
		assertThat(processBuilder.command()).containsExactly(workingDirectory.resolve("bin/cassandra.bat").toString(),
				"-p", workingDirectory.resolve("bin/pid").toString(), "-a", "-f");
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStartFail() throws IOException {
		doThrow(IOException.class).when(this.database).start(any(), any());
		assertThatThrownBy(this.database::start).isInstanceOf(IOException.class);
	}

	@Test
	void doStopPowershell() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));

		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
		doReturn(0).when(database).exec(eq("test:bin/stop-server.ps1"), any());

		database.start();
		database.stop();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database).exec(eq("test:bin/stop-server.ps1"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getValue();
		assertThat(processBuilder.command()).containsExactly("powershell",
				"-ExecutionPolicy", "Unrestricted", workingDirectory.resolve("bin/stop-server.ps1").toString(), "-p",
				workingDirectory.resolve("bin/pid").toString());
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStopBat() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.bat"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));

		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
		doThrow(new IOException("powershell")).when(database).exec(eq("test:bin/stop-server.ps1"), any());
		doReturn(0).when(database).exec(eq("test:bin/stop-server.bat"), any());

		database.start();
		database.stop();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database).exec(eq("test:bin/stop-server.bat"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getValue();
		assertThat(processBuilder.command()).containsExactly(workingDirectory.resolve("bin/stop-server.bat").toString(),
				"-p", workingDirectory.resolve("bin/pid").toString());
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStopServerPowershellForce() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.bat"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));

		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
		doReturn(1).doReturn(0).when(database).exec(eq("test:bin/stop-server.ps1"), any());
		doReturn(1).when(database).exec(eq("test:bin/stop-server.bat"), any());

		database.start();
		database.stop();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database, times(2)).exec(eq("test:bin/stop-server.ps1"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getAllValues().get(1);
		assertThat(processBuilder.command()).containsExactly("powershell",
				"-ExecutionPolicy", "Unrestricted", workingDirectory.resolve("bin/stop-server.ps1").toString(), "-p",
				workingDirectory.resolve("bin/pid").toString(), "-f");
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStopServerBatForce() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.bat"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));

		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
		doThrow(new IOException("powershell")).when(database).exec(eq("test:bin/stop-server.ps1"), any());
		doReturn(1).doReturn(0).when(database).exec(eq("test:bin/stop-server.bat"), any());

		database.start();
		database.stop();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database, times(2)).exec(eq("test:bin/stop-server.bat"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getAllValues().get(1);
		assertThat(processBuilder.command()).containsExactly(workingDirectory.resolve("bin/stop-server.bat").toString(),
				"-p", workingDirectory.resolve("bin/pid").toString(), "-f");
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStopTaskKill() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.bat"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));
		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.getPid()).thenReturn(100L);
		doThrow(new IOException("I/O")).when(database).exec(eq("test:bin/stop-server.ps1"), any());
		doReturn(1).doReturn(1).when(database).exec(eq("test:bin/stop-server.bat"), any());
		doReturn(0).when(database).exec(eq("test:taskkill"), any());

		database.start();
		database.stop();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database).exec(eq("test:taskkill"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getValue();
		assertThat(processBuilder.command()).containsExactly("taskkill", "/T", "/PID", "100");
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStopForceTaskKill() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));

		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.waitFor(10, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.getPid()).thenReturn(100L);
		doThrow(new IOException("I/O")).when(database).exec(eq("test:bin/stop-server.ps1"), any());
		doReturn(1).doReturn(0).when(database).exec(eq("test:taskkill"), any());

		database.start();
		database.stop();

		ArgumentCaptor<ProcessBuilder> pbCapture = ArgumentCaptor.forClass(ProcessBuilder.class);
		verify(database, times(2)).exec(eq("test:taskkill"), pbCapture.capture());
		ProcessBuilder processBuilder = pbCapture.getAllValues().get(1);
		assertThat(processBuilder.command()).containsExactly("taskkill", "/T", "/F", "/PID", "100");
		assertThat(processBuilder.environment()).containsAllEntriesOf(database.getEnvironmentVariables());
		assertThat(processBuilder.directory()).isEqualTo(workingDirectory.toFile());
	}

	@Test
	void doStopDestroyProcess() throws IOException {
		doReturn(this.process).when(this.database).doStart();
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(true);
		this.database.start();
		this.database.stop();
		verify(this.process, times(2)).destroy();
		verify(this.process, times(0)).destroyForcibly();
	}

	@Test
	void doStopDestroyForciblyProcess() throws IOException {
		doReturn(this.process).when(this.database).doStart();
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.waitFor(5, TimeUnit.SECONDS)).thenReturn(false);
		when(this.process.waitFor(3, TimeUnit.SECONDS)).thenReturn(true);
		when(this.process.destroyForcibly()).thenReturn(this.process);
		this.database.start();
		this.database.stop();
		verify(this.process, times(2)).destroy();
		verify(this.process).destroyForcibly();
	}

	@Test
	void doStopFail() throws IOException {
		doReturn(this.process).when(this.database).doStart();
		when(this.process.isAlive()).thenReturn(true);
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.destroyForcibly()).thenReturn(this.process);
		this.database.start();
		assertThatThrownBy(() -> this.database.stop()).hasMessageContaining("Unable to stop ");
	}

	@Test
	void doStopServerFails() throws IOException {
		WindowsCassandraDatabase database = this.database;
		Path workingDirectory = database.getWorkingDirectory();
		Files.createDirectory(workingDirectory.resolve("bin"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.bat"));
		Files.createFile(workingDirectory.resolve("bin/stop-server.ps1"));
		doThrow(new IOException("powershell")).when(database).exec(eq("test:bin/stop-server.ps1"), any());
		doThrow(new IOException("bat")).when(database).exec(eq("test:bin/stop-server.bat"), any());
		doReturn(this.process).when(this.database).doStart();
		when(this.process.destroy()).thenReturn(this.process);
		when(this.process.isAlive()).thenReturn(true);
		database.start();
		assertThatThrownBy(database::stop).hasStackTraceContaining("powershell", "bat");
	}

}
