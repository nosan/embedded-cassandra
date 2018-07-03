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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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
public class CqlScriptUtilsTest {

	@Test
	public void executeClassAwareScriptsClassPathResources() throws IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession, getClass(), "create.cql", "drop.cql");
		assertThat(mockSession.queries).containsExactly("CREATE KEYSPACE 'test'",
				"DROP KEYSPACE 'test'");
	}

	@Test(expected = IOException.class)
	public void executeClassAwareResourceNotFoundScriptsClassPathResources()
			throws IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession, "create.sql");
	}

	@Test
	public void executeSystemResourceFoundScriptsClassPathResources() throws IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession, "drop.cql");
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
	}

	@Test
	public void executeScriptsUris() throws IOException, URISyntaxException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession,
				ClassLoader.getSystemResource("drop.cql").toURI());
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
	}

	@Test
	public void executeScriptsUrls() throws IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession,
				ClassLoader.getSystemResource("drop.cql"));
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
	}

	@Test
	public void executeScriptsFiles() throws URISyntaxException, IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession,
				new File(ClassLoader.getSystemResource("drop.cql").toURI()));
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
	}

	@Test
	public void executeScriptsPaths() throws URISyntaxException, IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession,
				new File(ClassLoader.getSystemResource("drop.cql").toURI()).toPath());
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
	}

	@Test
	public void executeScriptsStreams() throws IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession,
				ClassLoader.getSystemResourceAsStream("drop.cql"));
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
	}

	@Test
	public void executeScriptsReaders() throws IOException {
		MockSession mockSession = new MockSession();
		CqlScriptUtils.executeScripts(mockSession,
				new InputStreamReader(ClassLoader.getSystemResourceAsStream("drop.cql")));
		assertThat(mockSession.queries).containsExactly("DROP KEYSPACE 'test'");
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
		public State getState() {
			return null;
		}

	}

}
