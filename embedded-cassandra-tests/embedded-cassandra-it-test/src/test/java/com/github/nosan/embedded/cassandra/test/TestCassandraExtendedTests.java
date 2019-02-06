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

package com.github.nosan.embedded.cassandra.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
public class TestCassandraExtendedTests {

	@Rule
	public final CaptureOutput output = new CaptureOutput();

	@Test
	public void shouldRegisterShutdownHookOnlyOnce() throws ClassNotFoundException {
		Set<Thread> beforeHooks = getHooks();
		TestCassandra testCassandra = new TestCassandra(true);
		try {
			testCassandra.start();
		}
		finally {
			testCassandra.stop();
		}
		try {
			testCassandra.start();
		}
		finally {
			testCassandra.stop();
		}
		Set<Thread> afterHooks = getHooks();
		afterHooks.removeAll(beforeHooks);
		assertThat(afterHooks).filteredOn(
				thread -> thread.getName().startsWith("Hook:TestCassandra"))
				.hasSize(1);
	}

	@Test
	public void shouldNotRegisterShutdownHook() throws ClassNotFoundException {
		Set<Thread> beforeHooks = getHooks();
		TestCassandra testCassandra = new TestCassandra(false);
		try {
			testCassandra.start();
		}
		finally {
			testCassandra.stop();
		}
		Set<Thread> afterHooks = getHooks();
		afterHooks.removeAll(beforeHooks);
		assertThat(afterHooks)
				.noneMatch(thread -> thread.getName().startsWith("Hook:TestCassandra"));
	}

	@SuppressWarnings("unchecked")
	private static Set<Thread> getHooks() throws ClassNotFoundException {
		return new LinkedHashSet<>(((Map<Thread, Thread>) ReflectionUtils
				.getStaticField(Class.forName("java.lang.ApplicationShutdownHooks"), "hooks"))
				.keySet());
	}
}
