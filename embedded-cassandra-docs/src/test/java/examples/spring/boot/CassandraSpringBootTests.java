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

package examples.spring.boot;

// tag::source[]

import com.datastax.driver.core.Cluster;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.spring.test.CassandraScripts;
import com.github.nosan.embedded.cassandra.spring.test.EmbeddedCassandra;

@EmbeddedCassandra
@CassandraScripts("schema.cql")
@SpringBootTest(properties = {"spring.data.cassandra.port=${embedded.cassandra.port}",
		"spring.data.cassandra.contact-points=${embedded.cassandra.address}",
		"spring.data.cassandra.keyspace-name=test"})
class CassandraSpringBootTests {

	@Test
	void testCassandra(@Autowired Cassandra cassandra, @Autowired Cluster cluster) {

	}

}
// end::source[]
