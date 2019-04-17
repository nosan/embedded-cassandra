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

import java.util.Optional;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.util.CqlSessionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("ConstantConditions")
abstract class AbstractTestCassandraTests {

	private final TestCassandra cassandra;

	@Nullable
	private CqlSession session;

	AbstractTestCassandraTests(Version version) {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setVersion(version);
		this.cassandra = new TestCassandra(factory);
	}

	@BeforeAll
	void startCassandra() {
		this.cassandra.start();
		this.session = new CqlSessionFactory().create(this.cassandra.getSettings());
	}

	@AfterAll
	void stopCassandra() {
		this.cassandra.stop();
		this.session.close();
	}

	@BeforeEach
	void initAllKeyspaces() {
		CqlSessionUtils.executeScripts(this.session, CqlScript.classpath("init.cql"));
	}

	@AfterEach
	void dropAllKeyspaces() {
		CqlSessionUtils.dropKeyspaces(this.session, "test");
	}

	@Test
	void dropTables() {
		Optional<KeyspaceMetadata> keyspace = this.session.getMetadata().getKeyspace("test");
		assertThat(keyspace).isPresent();
		assertThat(keyspace.get().getTable("users")).isPresent();
		CqlSessionUtils.dropTables(this.session, "test.users");
		assertThat(keyspace.get().getTable("users")).isPresent();
	}

	@Test
	void getCount() {
		assertThat(CqlSessionUtils.count(this.session, "test.users")).isEqualTo(1);
	}

	@Test
	void deleteFromTables() {
		assertThat(CqlSessionUtils.count(this.session, "test.users")).isEqualTo(1);
		CqlSessionUtils.truncateTables(this.session, "test.users");
		assertThat(CqlSessionUtils.count(this.session, "test.users")).isZero();
	}

}
