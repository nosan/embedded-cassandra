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

package com.github.nosan.embedded.cassandra.commons;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MDCThreadFactory}.
 *
 * @author Dmytro Nosan
 */
class MDCThreadFactoryTests {

	@BeforeEach
	void setUp() {
		MDC.put("X", "Y");
	}

	@AfterEach
	void tearDown() {
		MDC.remove("X");
	}

	@Test
	void createThreadAndSetMDCContext() throws InterruptedException {
		AtomicReference<String> mdcValue = new AtomicReference<>();
		Thread thread = new MDCThreadFactory().newThread(() -> mdcValue.set(MDC.get("X")));
		thread.start();
		thread.join(1000);
		assertThat(mdcValue).hasValue("Y");
	}

}
