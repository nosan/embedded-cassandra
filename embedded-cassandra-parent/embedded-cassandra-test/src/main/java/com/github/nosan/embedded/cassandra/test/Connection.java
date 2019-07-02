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

package com.github.nosan.embedded.cassandra.test;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * A connection to the {@link Cassandra}. This interface just a wrapper on a {@code native} connection.
 * To get a {@code native} connection, {@link #get()} method can be used.
 *
 * @author Dmytro Nosan
 * @see CqlSessionConnection
 * @see ClusterConnection
 * @since 2.0.2
 */
public interface Connection extends AutoCloseable {

	/**
	 * Executes the given {@link CqlScript scripts}.
	 *
	 * @param scripts the scripts
	 */
	void execute(CqlScript... scripts);

	/**
	 * Returns the native {@code connection}, e.g. a {@code Cluster} or a {@code CqlSession} instance.
	 *
	 * @return a native connection, never {@code null}
	 */
	Object get();

	/**
	 * Closes the current {@code connection}.
	 */
	@Override
	default void close() {
	}

	/**
	 * Whether this connection instance has been closed.
	 *
	 * @return {@code true} if this connection has been closed, {@code false} otherwise.
	 * @since 2.0.4
	 */
	default boolean isClosed() {
		return false;
	}

}
