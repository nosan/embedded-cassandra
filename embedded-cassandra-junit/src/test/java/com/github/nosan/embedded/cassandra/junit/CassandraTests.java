/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.junit;

import com.datastax.driver.core.Session;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
public class CassandraTests {

	@ClassRule
	public static Cassandra cassandra = new Cassandra();

	@Before
	public void setUp() throws Exception {
		CqlScriptUtils.executeScripts(cassandra.getSession(), "init.cql");
	}

	@After
	public void tearDown() throws Exception {
		CqlScriptUtils.executeScripts(cassandra.getSession(), "drop.cql");
	}

	@Test
	public void select() {
		Session session = cassandra.getSession();
		assertThat(session.execute("SELECT * FROM  test.roles").wasApplied()).isTrue();
	}

}
