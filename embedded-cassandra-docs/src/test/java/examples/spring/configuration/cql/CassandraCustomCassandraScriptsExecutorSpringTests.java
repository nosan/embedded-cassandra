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

package examples.spring.configuration.cql;

// tag::source[]

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.spring.test.CassandraScripts;
import com.github.nosan.embedded.cassandra.spring.test.CassandraScriptsExecutor;
import com.github.nosan.embedded.cassandra.spring.test.EmbeddedCassandra;

@EmbeddedCassandra
@CassandraScripts("schema.cql")
@ExtendWith(SpringExtension.class)
class CassandraCustomCassandraScriptsExecutorSpringTests {

	@Test
	void testCassandra(@Autowired Cassandra cassandra) {
		//
	}

	@Configuration
	static class TestConfig {

		@Bean
		CassandraScriptsExecutor cassandraScriptsExecutor() {
			return (cassandra, scripts) -> {
				try (Cluster cluster = Cluster.builder().addContactPoints(cassandra.getAddress()).withPort(
						cassandra.getPort()).build()) {
					Session session = cluster.connect();
					for (CqlScript script : scripts) {
						script.forEach(session::execute);
					}
				}
			};
		}

	}

}
// end::source[]
