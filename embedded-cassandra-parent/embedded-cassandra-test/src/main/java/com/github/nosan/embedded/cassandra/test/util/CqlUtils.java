/*
 * Copyright 2018-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

/**
 * Utility class for dealing with {@code CQL}.
 *
 * @author Dmytro Nosan
 * @since 1.0.6
 */
@API(since = "1.0.6", status = API.Status.MAINTAINED)
public abstract class CqlUtils {

	private static final Logger log = LoggerFactory.getLogger(CqlUtils.class);

	/**
	 * Delete all rows from the specified tables.
	 *
	 * @param session a session
	 * @param tableNames the names of the tables to delete from
	 */
	public static void deleteFromTables(Session session, String... tableNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableNames, "Tables must not be null");
		for (String tableName : tableNames) {
			executeStatement(session, String.format("TRUNCATE TABLE %s", tableName));
		}
	}

	/**
	 * Delete all rows from the non system tables.
	 *
	 * @param session a session
	 * @since 1.4.3
	 */
	@API(since = "1.4.3", status = API.Status.EXPERIMENTAL)
	public static void deleteFromAllNonSystemTables(Session session) {
		Objects.requireNonNull(session, "Session must not be null");
		deleteFromTables(session, getNonSystemTables(session));
	}

	/**
	 * Drop the specified tables.
	 *
	 * @param session a session
	 * @param tableNames the names of the tables to drop
	 */
	public static void dropTables(Session session, String... tableNames) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableNames, "Tables must not be null");
		for (String tableName : tableNames) {
			executeStatement(session, String.format("DROP TABLE IF EXISTS %s", tableName));
		}
	}

	/**
	 * Drop all non system tables.
	 *
	 * @param session a session
	 * @since 1.4.3
	 */
	@API(since = "1.4.3", status = API.Status.EXPERIMENTAL)
	public static void dropAllNonSystemTables(Session session) {
		Objects.requireNonNull(session, "Session must not be null");
		dropTables(session, getNonSystemTables(session));
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
		for (String keyspace : keyspaceNames) {
			executeStatement(session, String.format("DROP KEYSPACE IF EXISTS %s", keyspace));
		}
	}

	/**
	 * Drop all non system keyspaces.
	 *
	 * @param session a session
	 * @since 1.4.3
	 */
	@API(since = "1.4.3", status = API.Status.EXPERIMENTAL)
	public static void dropAllNonSystemKeyspaces(Session session) {
		Objects.requireNonNull(session, "Session must not be null");
		dropKeyspaces(session, getNonSystemKeyspaces(session));
	}

	/**
	 * Count the rows in the given table.
	 *
	 * @param tableName name of the table to count rows in
	 * @param session a session
	 * @return the number of rows in the table
	 */
	public static long getRowCount(Session session, String tableName) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(tableName, "Table must not be null");
		long[] count = new long[1];
		ResultSet resultSet = executeStatement(session, String.format("SELECT COUNT(*) as total FROM %s", tableName));
		resultSet.forEach(row -> count[0] += row.getLong("total"));
		return count[0];
	}

	/**
	 * Executes the provided query.
	 *
	 * @param session a session
	 * @param statement the CQL query to execute
	 * @return the result of the query. That result will never be null
	 * but can be empty (and will be for any non SELECT query).
	 */
	public static ResultSet executeStatement(Session session, String statement) {
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
	public static ResultSet executeStatement(Session session, String statement, @Nullable Object... args) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(statement, "Statement must not be null");
		if (log.isDebugEnabled()) {
			if (args != null && args.length > 0) {
				log.debug("Execute Statement: {} Values: {}", statement, Arrays.toString(args));
			}
			else {
				log.debug("Execute Statement: {}", statement);
			}
		}
		return session.execute(new SimpleStatement(statement, args));
	}

	/**
	 * Executes the provided statement.
	 *
	 * @param session a session
	 * @param statement the CQL statement to execute
	 * @return the result of the statement. That result will never be null
	 * but can be empty (and will be for any non SELECT query).
	 * @since 1.2.8
	 */
	public static ResultSet executeStatement(Session session, Statement statement) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(statement, "Statement must not be null");
		if (log.isDebugEnabled()) {
			log.debug("Execute Statement: {}", statement);
		}
		return session.execute(statement);
	}

	private static String[] getNonSystemTables(Session session) {
		List<String> tables = new ArrayList<>();
		for (String keyspace : getNonSystemKeyspaces(session)) {
			ResultSet resultSet = getTables(session, keyspace);
			resultSet.forEach(row -> tables
					.add(String.format("%s.%s", row.getString("keyspace_name"), row.getString("table_name"))));
		}
		Collections.reverse(tables);
		return tables.toArray(new String[0]);
	}

	private static String[] getNonSystemKeyspaces(Session session) {
		List<String> keyspaces = new ArrayList<>();
		ResultSet resultSet = getKeyspaces(session);
		resultSet.forEach(row -> {
			String name = row.getString("keyspace_name");
			if (!name.equals("system") && !name.startsWith("system_")) {
				keyspaces.add(name);
			}
		});
		Collections.reverse(keyspaces);
		return keyspaces.toArray(new String[0]);
	}

	private static ResultSet getTables(Session session, String keyspace) {
		try {
			return executeStatement(session,
					"SELECT keyspace_name, table_name FROM system_schema.tables WHERE keyspace_name = ?", keyspace);
		}
		catch (InvalidQueryException ex) {
			try {
				return executeStatement(session, "SELECT keyspace_name, columnfamily_name as table_name"
						+ " FROM system.schema_columnfamilies WHERE keyspace_name = ?", keyspace);
			}
			catch (InvalidQueryException ex1) {
				ex.addSuppressed(ex1);
				throw ex;
			}
		}
	}

	private static ResultSet getKeyspaces(Session session) {
		try {
			return executeStatement(session, "SELECT keyspace_name FROM system_schema.keyspaces");
		}
		catch (InvalidQueryException ex) {
			try {
				return executeStatement(session, "SELECT keyspace_name FROM system.schema_keyspaces");
			}
			catch (InvalidQueryException ex1) {
				ex.addSuppressed(ex1);
				throw ex;
			}
		}
	}

}
