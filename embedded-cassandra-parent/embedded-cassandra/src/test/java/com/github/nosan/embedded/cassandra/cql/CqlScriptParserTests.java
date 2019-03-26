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

package com.github.nosan.embedded.cassandra.cql;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CqlScriptParser}.
 *
 * @author Dmytro Nosan
 */
class CqlScriptParserTests {

	@Test
	void ignoreSpaces() {
		List<String> statements = CqlScriptParser.parse("   ");
		assertThat(statements).isEmpty();
	}

	@Test
	void parseStatement() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE '\"test\"'");
		assertThat(statements).containsExactly("USE KEYSPACE '\"test\"'");
	}

	@Test
	void parseStatements() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE \n\t test; DROP KEYSPACE \n\n   test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE test");
	}

	@Test
	void properlyHandlesBlockComment() {
		List<String> statements = CqlScriptParser.parse(
				"USE KEYSPACE test; /*DROP     KEYSPACE test*/USE KEYSPACE test;");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	void blockCommentNotClosed() {
		assertThatThrownBy(() -> CqlScriptParser.parse("USE KEYSPACE test; /*DROP KEYSPACE test")).isInstanceOf(
				IllegalArgumentException.class);
	}

	@Test
	void singleSlashCommentWithNewLine() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE test; //DROP KEYSPACE test\nUSE KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	void singleDashCommentWithNewLine() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE test; --DROP KEYSPACE test\nUSE KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	void singleDashComment() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE test; --DROP KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test");
	}

	@Test
	void singleSlashComment() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE test; //DROP KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test");
	}

	@Test
	void ignoreCommentSingleQuotes() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE test; DROP KEYSPACE '//test'");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE '//test'");
	}

	@Test
	void ignoreCommentDoubleQuotes() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE test; DROP KEYSPACE \"//test\"");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE \"//test\"");
	}

	@Test
	void ignoreStatementQuotes() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE 'test;' DROP KEYSPACE 'test'");
		assertThat(statements).containsExactly("USE KEYSPACE 'test;' DROP KEYSPACE 'test'");
	}

	@Test
	void ignoreStatementDoubleQuotes() {
		List<String> statements = CqlScriptParser.parse("USE KEYSPACE \"test;\" DROP KEYSPACE 'test'");
		assertThat(statements).containsExactly("USE KEYSPACE \"test;\" DROP KEYSPACE 'test'");
	}

	@Test
	void ignoreStatementDoubleDollars() {
		List<String> statements = CqlScriptParser.parse(
				"INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) VALUES "
						+ "  (201, '2015-02-18', '2015-02-22', $$Women's Tour of New Zealand; New England$$);");
		assertThat(statements).containsExactly(
				"INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) "
						+ "VALUES (201, '2015-02-18', '2015-02-22', $$Women's Tour of New Zealand; New England$$)");
	}

	@Test
	void ignoreDollarsInSingleQuote() {
		List<String> statements = CqlScriptParser.parse(
				"INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) VALUES "
						+ "  (201, '2015-02-18', '2015-02-22', '$$Womens Tour of New Zealand$$');");
		assertThat(statements).containsExactly(
				"INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) "
						+ "VALUES (201, '2015-02-18', '2015-02-22', '$$Womens Tour of New Zealand$$')");
	}

	@Test
	void ignoreDollarsInDoubleQuote() {
		List<String> statements = CqlScriptParser.parse(
				"INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) VALUES "
						+ "  (201, '2015-02-18', '2015-02-22', \"$$Womens Tour of New Zealand$$\");");
		assertThat(statements).containsExactly(
				"INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) "
						+ "VALUES (201, '2015-02-18', '2015-02-22', \"$$Womens Tour of New Zealand$$\")");
	}

	@Test
	void blockCommentNoStatements() {
		List<String> statements = CqlScriptParser.parse("/**/");
		assertThat(statements).isEmpty();
	}

	@Test
	void dashCommentNoStatements() {
		List<String> statements = CqlScriptParser.parse("--");
		assertThat(statements).isEmpty();
	}

	@Test
	void slashCommentNoStatements() {
		List<String> statements = CqlScriptParser.parse("//");
		assertThat(statements).isEmpty();
	}

}
