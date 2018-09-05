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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WaitUtils}.
 *
 * @author Dmytro Nosan
 */
public class WaitUtilsTests {

	@Test
	public void shouldAwaitFalse() throws Exception {
		long start = System.currentTimeMillis();
		assertThat(WaitUtils.await(Duration.ofMillis(1500), () -> false)).isFalse();
		assertThat(System.currentTimeMillis() - start).isBetween(1500L, 2000L);
	}

	@Test
	public void shouldAwaitTrue() throws Exception {
		long start = System.currentTimeMillis();
		assertThat(WaitUtils.await(Duration.ofMillis(1500), () -> true)).isTrue();
		assertThat(System.currentTimeMillis() - start).isBetween(0L, 1000L);
	}
}
