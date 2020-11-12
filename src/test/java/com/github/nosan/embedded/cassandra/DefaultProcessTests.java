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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultProcess}.
 *
 * @author Dmytro Nosan
 */
@DisabledOnOs(OS.WINDOWS)
class DefaultProcessTests {

	private final String message = "Hello Process!";

	@Test
	void getName() throws IOException {
		Process process = echo();
		assertThat(process.getName()).isEqualTo("echo");
	}

	@Test
	void getPid() throws IOException {
		Process process = echo();
		assertThat(process.getPid()).isPositive();
	}

	@Test
	@Timeout(5)
	void destroy() throws IOException {
		Process process = echo(10);
		process.destroy();
		process.waitFor();
	}

	@Test
	@Timeout(5)
	void destroyForcibly() throws IOException {
		Process process = echo(10);
		process.destroyForcibly();
		process.waitFor();
	}

	@Test
	void uninterruptedWaitFor() throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		Process process = echo(1);
		Thread thread = new Thread(process::waitFor);
		thread.start();
		thread.join(100);
		thread.interrupt();
		thread.join();
		assertThat(System.currentTimeMillis() - start).isBetween(500L, 1500L);
	}

	@Test
	void uninterruptedWaitForTimeout() throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		Process process = echo(1);
		Thread thread = new Thread(() -> process.waitFor(2, TimeUnit.SECONDS));
		thread.start();
		thread.join(100);
		thread.interrupt();
		thread.join();
		assertThat(System.currentTimeMillis() - start).isBetween(500L, 1500L);
	}

	@Test
	void isAlive() throws IOException {
		Process process = echo(1);
		assertThat(process.isAlive()).isTrue();
		assertThat(process.waitFor()).isZero();
		assertThat(process.isAlive()).isFalse();
	}

	@Test
	void waitForTimeout() throws IOException {
		Process process = echo(1);
		assertThat(process.waitFor(100, TimeUnit.MILLISECONDS)).isFalse();
		assertThat(process.waitFor(2, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void onExit() throws IOException, InterruptedException {
		Process process = echo(1);
		CountDownLatch latch = new CountDownLatch(1);
		process.onExit().thenRun(latch::countDown);
		latch.await();
		assertThat(process.waitFor()).isZero();
	}

	@Test
	void getStdOut() throws IOException, InterruptedException {
		StringBuffer buffer = new StringBuffer();
		Process process = echo(0, Stream.STDOUT);
		process.getStdOut().attach(buffer::append);
		assertThat(process.waitFor()).isZero();
		((Thread) process.getStdOut()).join();
		assertThat(buffer).contains(this.message);
	}

	@Test
	void getStdErr() throws IOException, InterruptedException {
		StringBuffer buffer = new StringBuffer();
		Process process = echo(0, Stream.STDERR);
		process.getStdErr().attach(buffer::append);
		assertThat(process.waitFor()).isZero();
		((Thread) process.getStdErr()).join();
		assertThat(buffer).contains(this.message);
	}

	private Process echo() throws IOException {
		return echo(0);
	}

	private Process echo(int seconds) throws IOException {
		return echo(seconds, Stream.STDOUT);
	}

	private Process echo(int seconds, Stream stream) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("bash", "-c",
				args("sleep", Integer.toString(seconds), "&&", "echo", this.message, "1>&" + stream));
		return new DefaultProcess("echo", builder.start());
	}

	private String args(String... args) {
		return String.join(" ", args);
	}

	private enum Stream {
		STDOUT(1),
		STDERR(2);

		private final int value;

		Stream(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return Integer.toString(this.value);
		}
	}

}
