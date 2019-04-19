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
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to run a {@link Process}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class RunProcess {

	private static final Logger log = LoggerFactory.getLogger(RunProcess.class);

	private final ProcessBuilder builder;

	RunProcess(ProcessBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Starts a new process.
	 *
	 * @return a new process
	 * @throws IOException if an I/O error occurs
	 */
	Process run() throws IOException {
		ProcessBuilder builder = this.builder;
		if (log.isDebugEnabled()) {
			String message = String.format("Execute '%s' within a directory '%s'", String.join(" ", builder.command()),
					builder.directory());
			log.debug(message);
		}
		return builder.start();
	}

	/**
	 * Starts a new process.
	 *
	 * @param threadFactory a thread factory to create a thread to read process output
	 * @param consumer the process output consumer
	 * @return the exit value
	 * @throws InterruptedException if the current thread is interrupted by another thread while it is waiting, then the
	 * wait is ended and an InterruptedException is thrown.
	 */
	int runAndWait(ThreadFactory threadFactory, Consumer<? super String> consumer) throws InterruptedException {
		try {
			Process process = run();
			Thread thread = threadFactory.newThread(() -> ProcessUtils.read(process, consumer));
			thread.start();
			int exit = process.waitFor();
			thread.join(1000);
			return exit;
		}
		catch (IOException ex) {
			log.error(String.format("Can not execute '%s'", String.join(" ", this.builder.command())), ex);
			return -1;
		}
	}

}
