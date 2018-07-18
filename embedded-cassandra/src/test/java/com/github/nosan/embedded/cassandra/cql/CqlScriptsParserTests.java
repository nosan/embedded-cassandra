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

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScriptParser}.
 *
 * @author Dmytro Nosan
 */
public class CqlScriptsParserTests {

	@Test
	public void skipSpaces() {
		List<String> statements = CqlScriptParser.getStatements("   ");
		assertThat(statements).isEmpty();
	}

	@Test
	public void oneStatements() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE '\"test\"'");
		assertThat(statements).containsExactly("USE KEYSPACE '\"test\"'");
	}

	@Test
	public void multiStatements() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE \n\t test; DROP KEYSPACE \n\n   test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "DROP KEYSPACE test");
	}

	@Test
	public void blockComment() {
		List<String> statements = CqlScriptParser.getStatements(
				"USE KEYSPACE test; /*DROP     KEYSPACE test*/USE KEYSPACE test;");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void blockCommentInvalid() {
		CqlScriptParser.getStatements("USE KEYSPACE test; /*DROP KEYSPACE test");
	}

	@Test
	public void singleSlashCommentWithNewLine() {
		List<String> statements = CqlScriptParser.getStatements(
				"USE KEYSPACE test; //DROP KEYSPACE test\nUSE KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	public void singleDashCommentWithNewLine() {
		List<String> statements = CqlScriptParser.getStatements(
				"USE KEYSPACE test; --DROP KEYSPACE test\nUSE KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test", "USE KEYSPACE test");
	}

	@Test
	public void singleDashComment() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE test; --DROP KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test");
	}

	@Test
	public void singleSlashComment() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE test; //DROP KEYSPACE test");
		assertThat(statements).containsExactly("USE KEYSPACE test");
	}

	@Test
	public void ignoreCommentSingleQuotes() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE test; DROP KEYSPACE '//test'");
		assertThat(statements).containsExactly("USE KEYSPACE test",
				"DROP KEYSPACE '//test'");
	}

	@Test
	public void ignoreCommentDoubleQuotes() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE test; DROP KEYSPACE \"//test\"");
		assertThat(statements).containsExactly("USE KEYSPACE test",
				"DROP KEYSPACE \"//test\"");
	}

	@Test
	public void ignoreStatementQuotes() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE 'test;' DROP KEYSPACE 'test'");
		assertThat(statements)
				.containsExactly("USE KEYSPACE 'test;' DROP KEYSPACE 'test'");
	}

	@Test
	public void ignoreStatementDoubleQuotes() {
		List<String> statements = CqlScriptParser
				.getStatements("USE KEYSPACE \"test;\" DROP KEYSPACE 'test'");
		assertThat(statements)
				.containsExactly("USE KEYSPACE \"test;\" DROP KEYSPACE 'test'");
	}

	@Test
	public void blockCommentEmpty() {
		List<String> statements = CqlScriptParser.getStatements("/**/");
		assertThat(statements).isEmpty();
	}

	@Test
	public void singleDashCommentEmpty() {
		List<String> statements = CqlScriptParser.getStatements("//");
		assertThat(statements).isEmpty();
	}

	@Test
	public void singleSlashCommentEmpty() {
		List<String> statements = CqlScriptParser.getStatements("//");
		assertThat(statements).isEmpty();
	}

}
