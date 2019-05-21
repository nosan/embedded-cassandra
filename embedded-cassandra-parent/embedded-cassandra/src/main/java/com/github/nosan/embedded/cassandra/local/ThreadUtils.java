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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility classes for dealing with threads.
 *
 * @author Dmytro Nosan
 * @since 2.0.1
 */
abstract class ThreadUtils {

	private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

	/**
	 * Force joins to the given thread.
	 *
	 * @param thread a thread
	 * @throws InterruptedException if any thread has interrupted the current thread.
	 */
	static void forceJoin(Thread thread) throws InterruptedException {
		boolean interrupted = false;
		while (true) {
			try {
				thread.join();
				break;
			}
			catch (InterruptedException ex) {
				interrupted = true;
			}
		}
		if (interrupted) {
			throw new InterruptedException("join interrupted");
		}
	}

	/**
	 * Interrupts the given thread.
	 *
	 * @param thread a thread
	 */
	static void interrupt(Thread thread) {
		try {
			thread.interrupt();
		}
		catch (Exception ex) {
			log.error(String.format("Can not interrupt a thread '%s'", thread.getName()), ex);
		}
	}

}
