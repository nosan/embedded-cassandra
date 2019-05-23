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

package com.github.nosan.embedded.cassandra.cql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Utility class for parsing CQL Script into the statements.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class CqlScriptParser {

	/**
	 * Parses the given script into the statements. Statements end in a semicolon
	 * <b>statement1;statement...;statementN</b>. This method does not validate the given {@code CQL} script, just only
	 * tries to split it into the statements.
	 *
	 * @param script CQL script.
	 * @return CQL statements
	 */
	public static List<String> parse(@Nullable String script) {
		return StringUtils.hasText(script) ? parseScript(script) : Collections.emptyList();
	}

	private static List<String> parseScript(String script) {
		List<String> statements = new ArrayList<>();
		StringBuilder statement = new StringBuilder();
		boolean singleQuoteEscape = false;
		boolean doubleQuoteEscape = false;
		boolean doubleDollarEscape = false;
		int length = script.length();
		int index = 0;
		while (index < length) {
			char c = getChar(script, index);
			if (!doubleQuoteEscape && !doubleDollarEscape && c == '\'') {
				statement.append('\'');
				singleQuoteEscape = !singleQuoteEscape;
				index++;
				continue;
			}
			if (!singleQuoteEscape && !doubleDollarEscape && c == '"') {
				statement.append('"');
				doubleQuoteEscape = !doubleQuoteEscape;
				index++;
				continue;
			}
			if (!singleQuoteEscape && !doubleQuoteEscape && script.startsWith("$$", index)) {
				statement.append("$$");
				doubleDollarEscape = !doubleDollarEscape;
				index += 2;
				continue;
			}
			if (!singleQuoteEscape && !doubleQuoteEscape && !doubleDollarEscape) {
				if (script.startsWith("--", index) || script.startsWith("//", index)) {
					if (script.indexOf('\n', index) < 0) {
						break;
					}
					index = script.indexOf('\n', index) + 1;
					continue;
				}
				if (script.startsWith("/*", index)) {
					if (script.indexOf("*/", index) < 0) {
						throw new IllegalArgumentException("Missing end block comment '*/'");
					}
					index = script.indexOf("*/", index) + 2;
					continue;
				}
				if (c == ';') {
					addStatement(statement, statements);
					index++;
					continue;
				}
				if (c == ' ' && isSpaceBefore(statement)) {
					index++;
					continue;
				}
			}
			statement.append(c);
			index++;
		}
		addStatement(statement, statements);
		return Collections.unmodifiableList(statements);
	}

	private static void addStatement(StringBuilder statement, List<String> statements) {
		if (StringUtils.hasText(statement)) {
			statements.add(statement.toString().trim());
		}
		statement.delete(0, statement.length());
	}

	private static char getChar(String script, int index) {
		char c = script.charAt(index);
		if (c == '\r' || c == '\t' || c == '\n') {
			return ' ';
		}
		return c;
	}

	private static boolean isSpaceBefore(StringBuilder statement) {
		return StringUtils.hasLength(statement) && statement.charAt(statement.length() - 1) == ' ';
	}

}

