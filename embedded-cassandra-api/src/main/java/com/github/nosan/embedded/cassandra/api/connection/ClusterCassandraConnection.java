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

package com.github.nosan.embedded.cassandra.api.connection;

import java.util.Objects;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * {@link CassandraConnection} to {@link Cassandra} with the specified {@link Cluster}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class ClusterCassandraConnection implements CassandraConnection {

	private final Cluster cluster;

	private final Session session;

	/**
	 * Creates a {@link ClusterCassandraConnection} with the specified {@link Cluster}.
	 *
	 * @param cluster a cluster
	 */
	public ClusterCassandraConnection(Cluster cluster) {
		this.cluster = Objects.requireNonNull(cluster, "'cluster' must not be null");
		this.session = this.cluster.connect();
	}

	@Override
	public ResultSet execute(String query) {
		Objects.requireNonNull(query, "'query' must not be null");
		return this.session.execute(query);
	}

	@Override
	public ResultSet execute(String query, Object... values) {
		Objects.requireNonNull(query, "'query' must not be null");
		Objects.requireNonNull(values, "'values' must not be null");
		return this.session.execute(query, values);
	}

	@Override
	public Cluster getConnection() {
		return this.cluster;
	}

	@Override
	public void close() {
		this.cluster.close();
	}

}
