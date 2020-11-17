/*
 * Copyright 2020 the original author or authors.
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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * JUnit4 Example.
 *
 * @author Dmytro Nosan
 */
public class CassandraJUnit4Example {

	// tag::code[]
	private static final Cassandra CASSANDRA = new CassandraBuilder().build();

	@BeforeClass
	public static void start() {
		CASSANDRA.start();
		Settings settings = CASSANDRA.getSettings();
		try (Cluster cluster = Cluster.builder().addContactPoints(settings.getAddress())
				.withPort(settings.getPort()).build()) {
			Session session = cluster.connect();
			CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
		}
	}

	@AfterClass
	public static void stop() {
		CASSANDRA.stop();
	}

	@Test
	public void testCassandra() {

	}

	// end::code[]

}
