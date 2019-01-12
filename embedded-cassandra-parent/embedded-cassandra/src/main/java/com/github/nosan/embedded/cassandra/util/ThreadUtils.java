/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.util;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for dealing with {@link Thread}.
 *
 * @author Dmytro Nosan
 * @since 1.2.8
 */
@API(since = "1.2.8", status = API.Status.INTERNAL)
public abstract class ThreadUtils {

	private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

	/**
	 * Waits for a thread to die.
	 *
	 * @param thread thread to join
	 * @throws InterruptedException if any thread has interrupted the current thread.
	 */
	public static void join(@Nullable Thread thread) throws InterruptedException {
		if (thread != null && thread.isAlive()) {
			if (log.isTraceEnabled()) {
				log.trace("{} <join to> {}", Thread.currentThread(), thread);
			}
			thread.join();
		}

	}

	/**
	 * Interrupts a thread.
	 *
	 * @param thread thread to interrupt
	 * @throws SecurityException – if the current thread cannot modify this thread
	 */
	public static void interrupt(@Nullable Thread thread) throws SecurityException {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
			if (log.isTraceEnabled()) {
				log.trace("{} is interrupted by {}", thread, Thread.currentThread());
			}
		}
	}
}
