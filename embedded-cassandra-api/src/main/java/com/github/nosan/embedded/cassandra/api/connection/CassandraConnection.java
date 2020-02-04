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

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * A connection to the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see CqlSessionCassandraConnection
 * @see ClusterCassandraConnection
 * @since 3.0.0
 */
public interface CassandraConnection extends AutoCloseable {

	/**
	 * Executes a CQL query.
	 *
	 * @param query the CQL query to execute
	 * @return the result of the query
	 */
	Object execute(String query);

	/**
	 * Executes the provided CQL query using the provided values.
	 *
	 * @param query the CQL query to execute
	 * @param values values required for the execution of {@code query}
	 * @return the result of the query
	 */
	Object execute(String query, Object... values);

	/**
	 * Returns the underlying native connection.
	 *
	 * @return a native connection
	 */
	Object getConnection();

	/**
	 * Closes the current connection.
	 */
	@Override
	void close();

}
