/*
 * Copyright 2020-2025 the original author or authors.
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Represents a wrapper around an operating system process.
 *
 * @author Dmytro Nosan
 */
interface ProcessWrapper {

	/**
	 * Gets the name of the process.
	 *
	 * @return the process name
	 */
	String getName();

	/**
	 * Gets the process identifier (PID).
	 *
	 * @return the PID of the process
	 */
	long getPid();

	/**
	 * Attempts to gracefully terminate the process.
	 *
	 * @return this {@link ProcessWrapper} instance
	 */
	ProcessWrapper destroy();

	/**
	 * Forcibly terminates the process.
	 *
	 * @return this {@link ProcessWrapper} instance
	 */
	ProcessWrapper destroyForcibly();

	/**
	 * Checks if the process is currently running.
	 *
	 * @return {@code true} if the process is alive; otherwise {@code false}
	 */
	boolean isAlive();

	/**
	 * Waits for the process to complete.
	 *
	 * @return the exit value of the process
	 */
	int waitFor();

	/**
	 * Waits for the process to complete within the specified timeout.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * @return {@code true} if the process has exited; {@code false} if the timeout elapsed
	 */
	boolean waitFor(int timeout, TimeUnit unit);

	/**
	 * Returns a {@link CompletableFuture} that completes when the process exits.
	 *
	 * @return a {@code CompletableFuture} that is completed when the process terminates
	 */
	CompletableFuture<? extends ProcessWrapper> onExit();

	/**
	 * Gets the standard output stream of the process.
	 *
	 * @return the {@link Output} representing the process's standard output
	 */
	Output getStdOut();

	/**
	 * Gets the standard error stream of the process.
	 *
	 * @return the {@link Output} representing the process's standard error
	 */
	Output getStdErr();

	/**
	 * Represents a handler for the output stream (standard output or error) of a process.
	 */
	interface Output {

		/**
		 * Attaches a consumer to the process's output stream.
		 *
		 * <p>The provided consumer is invoked whenever new data is available in the stream.</p>
		 *
		 * @param consumer the consumer to attach (must not be {@code null})
		 */
		void attach(Consumer<? super String> consumer);

		/**
		 * Detaches a previously attached consumer from the process's output stream.
		 *
		 * @param consumer the consumer to detach (must not be {@code null})
		 */
		void detach(Consumer<? super String> consumer);

	}

}
