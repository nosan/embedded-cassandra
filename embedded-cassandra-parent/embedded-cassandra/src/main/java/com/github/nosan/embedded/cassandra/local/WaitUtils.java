/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Utility class to {@code wait} an action.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
abstract class WaitUtils {


	/**
	 * Wait a given action. {@code true} result or {@code exception} breaks a waiting.
	 * <p> Blocks the current {@code thread}.
	 *
	 * @param action action to wait
	 * @param timeout time to wait
	 * @return {@code true} if action has returned true, otherwise {@code false}
	 * @throws Exception if callable throw an exception
	 * @throws InterruptedException if any thread has interrupted the current thread.
	 */
	static boolean await(@Nonnull Duration timeout, @Nonnull Callable<Boolean> action)
			throws InterruptedException, Exception {
		Objects.requireNonNull(timeout, "Timeout must not be null");
		Objects.requireNonNull(action, "Action must not be null");
		long start = System.nanoTime();
		long rem = timeout.toNanos();
		do {
			Boolean call = action.call();
			if (call != null && call) {
				return true;
			}
			if (rem > 0) {
				Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
			}
			rem = timeout.toNanos() - (System.nanoTime() - start);
		}
		while (rem > 0);
		return false;
	}

}
