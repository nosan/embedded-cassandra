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

package com.github.nosan.embedded.cassandra.test;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandraBuilder}.
 *
 * @author Dmytro Nosan
 */
class TestCassandraBuilderTests {

	@Test
	void configureTestCassandra() {
		TestCassandra testCassandra = new TestCassandraBuilder()
				.scripts(CqlScript.classpath("schema.cql"))
				.cassandraFactory(MockCassandra::new)
				.connectionFactory(new MockConnectionFactory())
				.build();
		assertThat(testCassandra).extracting("cassandra").first().isInstanceOf(MockCassandra.class);
		assertThat(testCassandra).extracting("connectionFactory").first().isInstanceOf(MockConnectionFactory.class);
		assertThat(testCassandra).extracting("scripts").first().asList()
				.containsExactly(CqlScript.classpath("schema.cql"));
	}

	private static final class MockConnectionFactory implements ConnectionFactory {

		@Override
		public Connection create(Settings settings) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class MockCassandra implements Cassandra {

		@Override
		public void start() throws CassandraException {

		}

		@Override
		public void stop() throws CassandraException {

		}

		@Override
		public Settings getSettings() throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Version getVersion() {
			throw new UnsupportedOperationException();
		}

		@Override
		public State getState() {
			throw new UnsupportedOperationException();
		}

	}

}
