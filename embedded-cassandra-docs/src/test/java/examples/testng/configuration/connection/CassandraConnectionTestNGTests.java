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

package examples.testng.configuration.connection;
// tag::source[]

import org.testng.annotations.Test;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.CqlSessionCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.testng.AbstractCassandraTests;

public class CassandraConnectionTestNGTests extends AbstractCassandraTests {

	public CassandraConnectionTestNGTests() {
		setCassandraConnectionFactory(new CqlSessionCassandraConnectionFactory());
	}

	@Test
	public void test() {
		Cassandra cassandra = getCassandra();
		CassandraConnection cassandraConnection = getCassandraConnection();
	}

}

// end::source[]
