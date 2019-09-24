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

package com.github.nosan.embedded.cassandra.api.connection;

import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * {@link CassandraConnection} to {@link Cassandra} with the specified {@link CqlSession}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class CqlSessionCassandraConnection implements CassandraConnection {

	private final CqlSession session;

	/**
	 * Creates a {@link ClusterCassandraConnection} with the specified {@link CqlSession}.
	 *
	 * @param session a session
	 */
	public CqlSessionCassandraConnection(CqlSession session) {
		this.session = Objects.requireNonNull(session, "'session' must not be null");
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
		return this.session.execute(SimpleStatement.newInstance(query, values));
	}

	@Override
	public CqlSession getConnection() {
		return this.session;
	}

	@Override
	public void close() {
		this.session.close();
	}

}
