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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ThreadUtils}.
 *
 * @author Dmytro Nosan
 */
public class ThreadUtilsTests {

	private static final Runnable TARGET = () -> {
		try {
			Thread.sleep(1500);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	};

	@Test
	public void join() throws InterruptedException {
		long millis = System.currentTimeMillis();
		Thread thread = new Thread(TARGET);
		thread.start();
		ThreadUtils.join(thread);
		long elapsed = System.currentTimeMillis() - millis;
		assertThat(elapsed).isGreaterThanOrEqualTo(1500);

	}

	@Test
	public void interrupt() throws InterruptedException {
		long millis = System.currentTimeMillis();
		Thread thread = new Thread(TARGET);
		thread.start();
		ThreadUtils.interrupt(thread);
		ThreadUtils.join(thread);
		long elapsed = System.currentTimeMillis() - millis;
		assertThat(elapsed).isLessThan(3000);
	}
}
