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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTestCassandraTests {

	private final TestCassandra cassandra;

	@Nullable
	private Connection connection;

	AbstractTestCassandraTests(Version version) {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setVersion(version);
		this.cassandra = new TestCassandra(factory, CqlScript.classpath("init.cql"));
	}

	@BeforeAll
	void startCassandra() {
		this.cassandra.start();
		this.connection = this.cassandra.getConnection();
	}

	@AfterAll
	void stopCassandra() {
		this.cassandra.stop();
		if (this.connection != null) {
			assertThat(this.connection.isClosed()).isTrue();
		}
	}

	@Test
	void shouldCountRows() {
		CqlSession session = this.cassandra.getNativeConnection(CqlSession.class);
		Row resultSet = session.execute("SELECT COUNT(*) FROM test.users").one();
		assertThat(resultSet).isNotNull();
		assertThat(resultSet.getLong(0)).isEqualTo(1);
	}

}
