/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.test.util;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for dealing with {@code CQL}.
 *
 * @author Dmytro Nosan
 * @since 1.0.6
 */
public abstract class CqlUtils {

	private static final Logger log = LoggerFactory.getLogger(CqlUtils.class);

	/**
	 * Delete all rows from the specified tables.
	 *
	 * @param session a session
	 * @param tableNames the names of the tables to delete from
	 */
	public static void deleteFromTables(@Nonnull Session session, @Nonnull String... tableNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableNames, "Tables must not be null");
		for (String tableName : tableNames) {
			executeStatement(session, "TRUNCATE TABLE " + tableName);
		}
	}

	/**
	 * Drop the specified tables.
	 *
	 * @param session a session
	 * @param tableNames the names of the tables to drop
	 */
	public static void dropTables(@Nonnull Session session, @Nonnull String... tableNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableNames, "Tables must not be null");
		for (String tableName : tableNames) {
			executeStatement(session, "DROP TABLE IF EXISTS " + tableName);
		}
	}

	/**
	 * Drop the specified keyspaces.
	 *
	 * @param session a session
	 * @param keyspaceNames the names of the keyspaces to drop
	 */
	public static void dropKeyspaces(@Nonnull Session session, @Nonnull String... keyspaceNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(keyspaceNames, "Keyspaces must not be null");
		for (String keyspace : keyspaceNames) {
			executeStatement(session, "DROP KEYSPACE IF EXISTS " + keyspace);
		}
	}

	/**
	 * Count the rows in the given table.
	 *
	 * @param tableName name of the table to count rows in
	 * @param session a session
	 * @return the number of rows in the table
	 */
	public static long getRowCount(@Nonnull Session session, @Nonnull String tableName) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableName, "Table must not be null");
		return executeStatement(session, "SELECT COUNT(0) FROM " + tableName).one().getLong(0);
	}

	/**
	 * Executes the provided query.
	 *
	 * @param session a session
	 * @param statement the CQL query to execute
	 * @return the result of the query. That result will never be null
	 * but can be empty (and will be for any non SELECT query).
	 */
	@Nonnull
	public static ResultSet executeStatement(@Nonnull Session session, @Nonnull String statement) {
		return executeStatement(session, statement, new Object[0]);
	}

	/**
	 * Executes the provided query using the provided values.
	 *
	 * @param session a session
	 * @param statement the CQL query to execute.
	 * @param args values required for the execution of {@code query}. See {@link
	 * SimpleStatement#SimpleStatement(String, Object...)} for more details.
	 * @return the result of the query. That result will never be null but
	 * can be empty (and will be for any non SELECT query).
	 */
	@Nonnull
	public static ResultSet executeStatement(@Nonnull Session session, @Nonnull String statement,
			@Nullable Object... args) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(statement, "Statement must not be null");
		SimpleStatement simpleStatement = new SimpleStatement(statement, args);
		if (log.isDebugEnabled()) {
			log.debug("Execute Statement: ({})", simpleStatement);
		}
		return session.execute(simpleStatement);
	}
}
