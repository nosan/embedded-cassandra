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

package com.github.nosan.embedded.cassandra.junit4.test;

import com.datastax.driver.core.Cluster;
import org.junit.ClassRule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.api.connection.ClusterCassandraConnectionFactory;
import com.github.nosan.embedded.cassandra.api.cql.CqlDataSet;

/**
 * Tests for {@link CassandraRule}.
 *
 * @author Dmytro Nosan
 */
public class CassandraRuleTests {

	@ClassRule
	public static final CassandraRule rule = new CassandraRule()
			.withCqlDataSet(CqlDataSet.ofClasspaths("schema.cql"))
			.withCassandraConnectionFactory(new ClusterCassandraConnectionFactory());

	@Test
	public void testCassandra() {
		Cluster cluster = rule.getConnection(Cluster.class);
		cluster.connect().execute("SELECT * FROM test.roles");
	}

}
