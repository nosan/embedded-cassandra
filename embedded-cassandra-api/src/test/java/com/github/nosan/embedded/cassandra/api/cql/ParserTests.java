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

package com.github.nosan.embedded.cassandra.api.cql;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Parser}.
 *
 * @author Dmytro Nosan
 */
class ParserTests {

	@Test
	void shouldParseOnlyOneStatementTrimmed() {
		List<String> statements = parse(" USE KEYSPACE '\"test\"' ");
		assertThat(statements).containsExactly("USE KEYSPACE '\"test\"'");
	}

	@Test
	void shouldParseStatementsIgnoringSpecialSymbols() {
		List<String> statements = parse(" USE KEYSPACE  test; DROP KEYSPACE \n\n\r\r\t\t     test  ");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE test");
	}

	@Test
	void shouldParseStatementsIgnoringBlockComment() {
		List<String> statements = parse("USE KEYSPACE test; /*DROP     KEYSPACE test*/USE KEYSPACE test;");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	void shouldParseStatementsIgnoringSingleLineSlashComment() {
		List<String> statements = parse("USE KEYSPACE test; //DROP KEYSPACE test\nUSE KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	void shouldParseStatementsIgnoringSingleLineDashComment() {
		List<String> statements = parse("USE KEYSPACE test; --DROP KEYSPACE test\nUSE KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	void shouldParseStatementDashComment() {
		List<String> statements = parse("USE KEYSPACE test; --DROP KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test");
	}

	@Test
	void shouldParseStatementSlashComment() {
		List<String> statements = parse("USE KEYSPACE test; //DROP KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test");
	}

	@Test
	void ignoreSlashCommentSingleQuotes() {
		List<String> statements = parse("USE KEYSPACE test; DROP KEYSPACE '//test'");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE '//test'");
	}

	@Test
	void ignoreSlashCommentDoubleQuotes() {
		List<String> statements = parse("USE KEYSPACE test; DROP KEYSPACE \"//test\"");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE \"//test\"");
	}

	@Test
	void ignoreDashCommentSingleQuotes() {
		List<String> statements = parse("USE KEYSPACE test; DROP KEYSPACE '--test'");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE '--test'");
	}

	@Test
	void ignoreDashCommentDoubleQuotes() {
		List<String> statements = parse("USE KEYSPACE test; DROP KEYSPACE \"--test\"");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE \"--test\"");
	}

	@Test
	void ignoreStatementSymbolInQuotes() {
		List<String> statements = parse("USE KEYSPACE 'test;' DROP KEYSPACE 'test'");
		assertThat(statements).containsExactly("USE KEYSPACE 'test;' DROP KEYSPACE 'test'");
	}

	@Test
	void ignoreStatementSymbolInDoubleQuotes() {
		List<String> statements = parse("USE KEYSPACE \"test;\" DROP KEYSPACE 'test'");
		assertThat(statements).containsExactly("USE KEYSPACE \"test;\" DROP KEYSPACE 'test'");
	}

	@Test
	void ignoreStatementSymbolInDoubleDollars() {
		List<String> statements = parse("INSERT INTO cycling.calendar (race_id, race_name) VALUES "
				+ "  (201, $$Women's Tour of New Zealand;  New England$$);");
		assertThat(statements).containsExactly("INSERT INTO cycling.calendar (race_id, race_name) VALUES "
				+ "(201, $$Women's Tour of New Zealand;  New England$$)");
	}

	@Test
	void shouldNotTrimSpacesInDoubleQuotes() {
		List<String> statements = parse("INSERT INTO cycling.calendar (race_id, race_name) VALUES "
				+ "  (201, \"Women's Tour   of New Zealand; New England\");");
		assertThat(statements).containsExactly("INSERT INTO cycling.calendar (race_id, race_name) VALUES "
				+ "(201, \"Women's Tour   of New Zealand; New England\")");
	}

	@Test
	void shouldNotTrimSpacesInQuotes() {
		List<String> statements = parse("INSERT INTO cycling.calendar (race_id, race_name) VALUES "
				+ "  (201, 'Women''s Tour   of New Zealand; New England');");
		assertThat(statements).containsExactly("INSERT INTO cycling.calendar (race_id, race_name) VALUES "
				+ "(201, 'Women''s Tour   of New Zealand; New England')");
	}

	@Test
	void shouldParseNoStatementsBlockComment() {
		List<String> statements = parse("/**/");
		assertThat(statements).isEmpty();
	}

	@Test
	void shouldParseNoStatementsSingleDashComment() {
		List<String> statements = parse("--");
		assertThat(statements).isEmpty();
	}

	@Test
	void shouldParseNoStatementsSingleSlashComment() {
		List<String> statements = parse("//");
		assertThat(statements).isEmpty();
	}

	@Test
	void shouldParseNoStatementsNoTextScript() {
		List<String> statements = parse("   ");
		assertThat(statements).isEmpty();
	}

	private static List<String> parse(String script) {
		return new Parser(script).getStatements();
	}

}
