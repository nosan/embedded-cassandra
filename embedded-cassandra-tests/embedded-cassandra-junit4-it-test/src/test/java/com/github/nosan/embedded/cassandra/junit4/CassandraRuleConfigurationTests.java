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

package com.github.nosan.embedded.cassandra.junit4;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.ClassRule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;
import com.github.nosan.embedded.cassandra.test.CqlSessionConnection;
import com.github.nosan.embedded.cassandra.test.CqlSessionConnectionFactory;
import com.github.nosan.embedded.cassandra.test.junit4.CassandraRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraRule}.
 *
 * @author Dmytro Nosan
 */
public class CassandraRuleConfigurationTests {

	@ClassRule
	public static final CassandraRule cassandra = new CassandraRule(() -> {
		LocalCassandraFactory factory = new LocalCassandraFactory();
		factory.setVersion("3.11.3");
		return factory.create();
	}, new CqlSessionConnectionFactory(), CqlScript.classpath("init.cql"));

	@Test
	public void testConfiguration() {
		assertThat(cassandra.getVersion()).isEqualTo(Version.parse("3.11.3"));
		assertThat(cassandra.getConnection()).isInstanceOf(CqlSessionConnection.class);
		CqlSession session = cassandra.getNativeConnection(CqlSession.class);
		assertThat(session.execute("SELECT * FROM  test.roles")).isEmpty();
	}

}
