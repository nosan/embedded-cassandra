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

package com.github.nosan.embedded.cassandra.testng;

import com.datastax.driver.core.Cluster;
import org.testng.annotations.Test;

import com.github.nosan.embedded.cassandra.api.connection.ClusterCassandraConnection;
import com.github.nosan.embedded.cassandra.api.connection.ClusterCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;

/**
 * Tests for {@link AbstractCassandraTests}.
 *
 * @author Dmytro Nosan
 */
public class AbstractCassandraTestsTests extends AbstractCassandraTests {

	public AbstractCassandraTestsTests() {
		setCqlDataSet(CqlDataSet.ofClasspaths("schema.cql"));
		setCassandraConnectionFactory(new ClusterCassandraConnectionFactory());
	}

	@Test
	public void testCassandra() {
		ClusterCassandraConnection connection = (ClusterCassandraConnection) getCassandraConnection();
		Cluster cluster = connection.getConnection();
		cluster.connect().execute("SELECT * FROM test.roles");
	}

}
