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

package com.github.nosan.embedded.cassandra.test.util;

import java.util.Arrays;
import java.util.Objects;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Utility class for dealing with {@code CQL}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public abstract class SessionUtils {

	private static final Logger log = LoggerFactory.getLogger(SessionUtils.class);

	/**
	 * Delete all rows from the specified tables.
	 *
	 * @param session a session
	 * @param tableNames the full names of the tables to delete from
	 */
	public static void truncateTables(Session session, String... tableNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableNames, "Tables must not be null");
		for (String tableName : tableNames) {
			execute(session, String.format("TRUNCATE TABLE %s", tableName));
		}
	}

	/**
	 * Drop the specified tables.
	 *
	 * @param session a session
	 * @param tableNames the full names of the tables to drop
	 */
	public static void dropTables(Session session, String... tableNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableNames, "Tables must not be null");
		for (String tableName : tableNames) {
			execute(session, String.format("DROP TABLE IF EXISTS %s", tableName));
		}
	}

	/**
	 * Drop the specified keyspaces.
	 *
	 * @param session a session
	 * @param keyspaceNames the names of the keyspaces to drop
	 */
	public static void dropKeyspaces(Session session, String... keyspaceNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(keyspaceNames, "Keyspaces must not be null");
		for (String keyspaceName : keyspaceNames) {
			execute(session, String.format("DROP KEYSPACE IF EXISTS %s", keyspaceName));
		}
	}

	/**
	 * Count the rows in the given table.
	 *
	 * @param tableName the full name of the table to count rows in
	 * @param session a session
	 * @return the number of rows in the table
	 */
	public static long getCount(Session session, String tableName) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableName, "Table must not be null");
		long count = 0;
		ResultSet resultSet = execute(session, String.format("SELECT COUNT(*) as total FROM %s", tableName));
		for (Row row : resultSet) {
			count += row.getLong("total");
		}
		return count;
	}

	/**
	 * Executes the provided query.
	 *
	 * @param session a session
	 * @param statement the CQL query to execute
	 * @return the result of the query. That result will never be null but can be empty (and will be for any non SELECT
	 * query).
	 */
	public static ResultSet execute(Session session, String statement) {
		return execute(session, statement, new Object[0]);
	}

	/**
	 * Executes the provided query using the provided values.
	 *
	 * @param session a session
	 * @param statement the CQL query to execute.
	 * @param args values required for the execution of {@code query}. See {@link SimpleStatement} for more details.
	 * @return the result of the query. That result will never be null but can be empty (and will be for any non
	 * SELECT query).
	 */
	public static ResultSet execute(Session session, String statement, Object... args) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(statement, "Statement must not be null");
		Objects.requireNonNull(args, "Args must not be null");
		return execute(session, new SimpleStatement(statement, args));
	}

	/**
	 * Executes the provided statement.
	 *
	 * @param session a session
	 * @param statement the CQL statement to execute
	 * @return the result of the statement. That result will never be null but can be empty (and will be for any non
	 * SELECT query).
	 */
	public static ResultSet execute(Session session, Statement statement) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(statement, "Statement must not be null");
		if (log.isDebugEnabled()) {
			log.debug("Execute Statement: {}", getCql(statement));
		}
		return session.execute(statement);
	}

	/**
	 * Executes the given scripts.
	 *
	 * @param scripts the CQL scripts to execute.
	 * @param session a session
	 * @see CqlScript
	 */
	public static void executeScripts(Session session, CqlScript... scripts) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(scripts, "Scripts must not be null");
		if (log.isDebugEnabled()) {
			log.debug("Executing CQL Scripts: '{}'", Arrays.toString(scripts));
		}
		for (CqlScript script : scripts) {
			for (String statement : script.getStatements()) {
				SessionUtils.execute(session, statement);
			}
		}
	}

	private static String getCql(Statement statement) {
		if (statement instanceof SimpleStatement) {
			return ((SimpleStatement) statement).getQueryString();
		}
		return statement.toString();
	}

}
