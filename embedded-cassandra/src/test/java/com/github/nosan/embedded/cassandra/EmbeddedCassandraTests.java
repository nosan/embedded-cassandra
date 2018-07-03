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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;

import com.datastax.driver.core.Session;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import org.junit.Test;

/**
 * Tests for {@link EmbeddedCassandra}.
 *
 * @author Dmytro Nosan
 *
 */
public class EmbeddedCassandraTests {

	@Test
	public void initScriptUsingSession() throws IOException {
		EmbeddedCassandra embeddedCassandra = new EmbeddedCassandra();
		try {
			embeddedCassandra.start();
			Session session = embeddedCassandra.getSession();
			CqlScriptUtils.executeScripts(session, "init.cql");
		}
		finally {
			embeddedCassandra.stop();
		}
	}

	@Test
	public void initScriptUsingCluster() throws IOException {
		EmbeddedCassandra embeddedCassandra = new EmbeddedCassandra();
		try {
			embeddedCassandra.start();
			Session session = embeddedCassandra.getCluster().connect();
			CqlScriptUtils.executeScripts(session, "init.cql");
		}
		finally {
			embeddedCassandra.stop();
		}
	}

}
