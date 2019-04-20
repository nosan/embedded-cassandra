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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraRunner;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalCassandra}.
 *
 * @author Dmytro Nosan
 */
class LocalCassandraExtendedTests {

	@Test
	void shouldRegisterShutdownHookOnlyOnce() throws ClassNotFoundException {
		Set<Thread> beforeHooks = getHooks();
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setRegisterShutdownHook(true);
		Cassandra cassandra = factory.create();
		new CassandraRunner(cassandra).run();
		new CassandraRunner(cassandra).run();
		Set<Thread> afterHooks = getHooks();
		afterHooks.removeAll(beforeHooks);
		assertThat(afterHooks).filteredOn(thread -> thread.getName().startsWith("cassandra:")).hasSize(1);
	}

	@Test
	void shouldNotRegisterShutdownHook() throws ClassNotFoundException {
		Set<Thread> beforeHooks = getHooks();
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setRegisterShutdownHook(false);
		Cassandra cassandra = factory.create();
		new CassandraRunner(cassandra).run();
		Set<Thread> afterHooks = getHooks();
		afterHooks.removeAll(beforeHooks);
		assertThat(afterHooks).noneMatch(thread -> thread.getName().startsWith("cassandra:"));
	}

	@SuppressWarnings("unchecked")
	private static Set<Thread> getHooks() throws ClassNotFoundException {
		return new LinkedHashSet<>(((Map<Thread, Thread>) ReflectionUtils
				.getStaticField(Class.forName("java.lang.ApplicationShutdownHooks"), "hooks")).keySet());
	}

}
