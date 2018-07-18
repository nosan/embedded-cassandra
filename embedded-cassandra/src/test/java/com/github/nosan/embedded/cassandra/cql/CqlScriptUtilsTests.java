/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.cql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.CloseFuture;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScriptUtils}.
 *
 * @author Dmytro Nosan
 */
public class CqlScriptUtilsTests {

	@Test
	public void executeScripts() {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession, new ClassPathCqlScript("init.cql"),
				new ClassPathCqlScript("test.cql"));
		assertThat(mockSession.queries).hasSize(3);
		assertThat(mockSession.queries).containsExactly("CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION =" +
						" { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }",
				"CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )",
				"CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )");
	}

	private static class MockSession implements Session {

		private final List<String> queries = new ArrayList<>();

		@Override
		public String getLoggedKeyspace() {
			return null;
		}

		@Override
		public Session init() {
			return null;
		}

		@Override
		public ListenableFuture<Session> initAsync() {
			return null;
		}

		@Override
		public ResultSet execute(String query) {
			this.queries.add(query);
			return null;
		}

		@Override
		public ResultSet execute(String query, Object... values) {
			return null;
		}

		@Override
		public ResultSet execute(String query, Map<String, Object> values) {
			return null;
		}

		@Override
		public ResultSet execute(Statement statement) {
			return null;
		}

		@Override
		public ResultSetFuture executeAsync(String query) {
			return null;
		}

		@Override
		public ResultSetFuture executeAsync(String query, Object... values) {
			return null;
		}

		@Override
		public ResultSetFuture executeAsync(String query, Map<String, Object> values) {
			return null;
		}

		@Override
		public ResultSetFuture executeAsync(Statement statement) {
			return null;
		}

		@Override
		public PreparedStatement prepare(String query) {
			return null;
		}

		@Override
		public PreparedStatement prepare(RegularStatement statement) {
			return null;
		}

		@Override
		public ListenableFuture<PreparedStatement> prepareAsync(String query) {
			return null;
		}

		@Override
		public ListenableFuture<PreparedStatement> prepareAsync(
				RegularStatement statement) {
			return null;
		}

		@Override
		public CloseFuture closeAsync() {
			return null;
		}

		@Override
		public void close() {

		}

		@Override
		public boolean isClosed() {
			return false;
		}

		@Override
		public Cluster getCluster() {
			return null;
		}

		@Override
		public Session.State getState() {
			return null;
		}

	}

}
