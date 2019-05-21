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

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ThreadUtils}.
 *
 * @author Dmytro Nosan
 */
class ThreadUtilsTests {

	@Test
	void shouldInterrupt() {
		Assertions.assertTimeout(Duration.ofMillis(750), () -> {
			Thread runThread = new Thread(() -> {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			});

			runThread.start();
			ThreadUtils.interrupt(runThread);
			runThread.join();
		});

	}

	@Test
	void shouldJoinForce() {
		assertThatThrownBy(() -> Assertions.assertTimeout(Duration.ofMillis(1250), () -> {
			Thread runThread = new Thread(() -> {
				try {
					Thread.sleep(1500);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			});
			Thread joinThread = new Thread(() -> {
				try {
					ThreadUtils.forceJoin(runThread);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			});
			runThread.start();
			joinThread.start();
			ThreadUtils.interrupt(joinThread);
			joinThread.join();
		})).isInstanceOf(AssertionFailedError.class).hasStackTraceContaining("execution exceeded timeout");
	}

}
