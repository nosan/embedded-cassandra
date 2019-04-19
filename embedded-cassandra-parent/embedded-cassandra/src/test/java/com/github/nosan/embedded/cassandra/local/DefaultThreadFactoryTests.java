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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultThreadFactory}.
 *
 * @author Dmytro Nosan
 */
class DefaultThreadFactoryTests {

	private final ThreadFactory threadFactory = new DefaultThreadFactory("mythread", 1);

	@BeforeEach
	void setUp() {
		MDC.put("X", "Y");
	}

	@AfterEach
	void tearDown() {
		MDC.remove("X");
	}

	@Test
	void createThread() throws InterruptedException {
		AtomicReference<String> value = new AtomicReference<>();
		Thread thread = this.threadFactory.newThread(() -> value.set(MDC.get("X")));
		assertThat(thread.getName()).isEqualTo("mythread-1-T-1");
		assertThat(thread.isDaemon()).isTrue();
		thread.start();
		thread.join();
		assertThat(value).hasValue("Y");
	}

}
