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

package com.github.nosan.embedded.cassandra.junit5;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.CqlSessionFactory;
import com.github.nosan.embedded.cassandra.test.junit5.CassandraExtension;
import com.github.nosan.embedded.cassandra.test.util.CqlSessionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraExtension}.
 *
 * @author Dmytro Nosan
 */
class CassandraExtensionTests {

	@RegisterExtension
	static final CassandraExtension cassandra = new CassandraExtension(CqlScript.classpath("init.cql"));

	@Test
	void selectRoles() {
		try (CqlSession session = new CqlSessionFactory()
				.create(cassandra.getSettings())) {
			assertThat(CqlSessionUtils.execute(session, "SELECT * FROM  test.roles")).isEmpty();
		}
	}

}
