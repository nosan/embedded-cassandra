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

package examples.junit4.configuration.factory;

// tag::source[]

import org.junit.ClassRule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraBuilder;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.github.nosan.embedded.cassandra.junit4.test.CassandraRule;

public class CassandraCustomFactoryJUnit4Tests {

	@ClassRule
	public static final CassandraRule CASSANDRA_RULE = new CassandraRule()
			.withCassandraFactory(new EmbeddedCassandraBuilder()
					.withArtifact(Artifact.ofVersion("4.0-beta1")));

	@Test
	public void testCassandra() {
		Cassandra cassandra = CASSANDRA_RULE.getCassandra();
		CassandraConnection cassandraConnection = CASSANDRA_RULE.getCassandraConnection();
	}

}
// end::source[]
