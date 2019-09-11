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

package examples.testng;
// tag::source[]

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.testng.AbstractCassandraTests;

public class CassandraTestNGTests extends AbstractCassandraTests {

	@BeforeClass
	public void prepare() {
		Cassandra cassandra = getCassandra();
		try (Cluster cluster = Cluster.builder().addContactPoints(cassandra.getAddress()).withPort(cassandra.getPort())
				.build()) {
			Session session = cluster.connect();
			CqlScript.ofResources(new ClassPathResource("schema.cql")).forEach(session::execute);
		}
	}

	@Test
	public void test() {
		Cassandra cassandra = getCassandra();
		//
	}

}

// end::source[]
