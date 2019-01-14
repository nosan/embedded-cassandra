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

import javax.annotation.Nonnull;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandraBuilder}.
 *
 * @author Dmytro Nosan
 */
public class TestCassandraBuilderTests {

	@Test
	public void shouldBuildTestCassandra() {
		ClusterFactory clusterFactory = (settings) -> null;
		Cassandra cassandra = new Cassandra() {
			@Override
			public void start() throws CassandraException {

			}

			@Override
			public void stop() throws CassandraException {

			}

			@Nonnull
			@Override
			public Settings getSettings() throws CassandraException {
				return null;
			}
		};

		CassandraFactory cassandraFactory = () -> cassandra;
		CqlScript script = () -> null;
		TestCassandra testCassandra = new TestCassandraBuilder()
				.addScripts(script)
				.setRegisterShutdownHook(false)
				.setClusterFactory(clusterFactory)
				.setCassandraFactory(cassandraFactory)
				.build();

		assertThat(ReflectionUtils.getField(testCassandra, "cassandra")).isEqualTo(cassandra);
		assertThat(ReflectionUtils.getField(testCassandra, "registerShutdownHook")).isEqualTo(false);
		assertThat(ReflectionUtils.getField(testCassandra, "scripts")).isEqualTo(new CqlScript[]{script});
		assertThat(ReflectionUtils.getField(testCassandra, "clusterFactory")).isEqualTo(clusterFactory);
	}
}
