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

package com.github.nosan.embedded.cassandra.jupiter;

import com.datastax.driver.core.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraExtension}.
 *
 * @author Dmytro Nosan
 */
public class CassandraExtensionTests {

	@RegisterExtension
	public static CassandraExtension cassandra = new CassandraExtension();

	@BeforeEach
	public void setUp() throws Exception {
		CqlScriptUtils.executeScripts(cassandra.getSession(), "init.cql");
	}

	@AfterEach
	public void tearDown() throws Exception {
		CqlScriptUtils.executeScripts(cassandra.getSession(), "drop.cql");
	}

	@Test
	public void select() {
		Session session = cassandra.getSession();
		assertThat(session.execute("SELECT * FROM  test.roles").wasApplied()).isTrue();
	}

}
