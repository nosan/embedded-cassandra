/*
 * Copyright 2018-2020 the original author or authors.
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

package examples;
// tag::source[]

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.DefaultCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;

public class QuickStart {

	public static void main(String[] args) {
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		Cassandra cassandra = cassandraFactory.create();
		cassandra.start();
		DefaultCassandraConnectionFactory cassandraConnectionFactory = new DefaultCassandraConnectionFactory();
		try (CassandraConnection connection = cassandraConnectionFactory.create(cassandra)) {
			CqlDataSet.ofClasspaths("schema.cql").forEachStatement(connection::execute);
		}
		finally {
			cassandra.stop();
		}
	}

}
// end::source[]
