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

package com.github.nosan.embedded.cassandra.spring.test;

import com.datastax.driver.core.Cluster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.api.connection.CassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.ClusterCassandraConnection;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandra} with {@link CqlDataSet}.
 *
 * @author Dmytro Nosan
 */
@EmbeddedCassandra(scripts = "schema.cql")
@ExtendWith(SpringExtension.class)
@DirtiesContext
class EmbeddedCassandraScriptsTests {

	@Test
	void testCqlScripts(@Autowired CassandraConnection cassandraConnection) {
		assertThat(cassandraConnection).isInstanceOf(ClusterCassandraConnection.class);
		Cluster cluster = (Cluster) cassandraConnection.getConnection();
		assertThat(cluster.getMetadata().getKeyspace("test")).isNotNull();
	}

}
