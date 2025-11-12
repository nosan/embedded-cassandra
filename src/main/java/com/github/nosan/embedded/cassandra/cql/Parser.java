/*
 * Copyright 2020-2025 the original author or authors.
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
import java.util.List;

import com.github.nosan.embedded.cassandra.commons.StringUtils;

/**
 * A utility class for parsing a CQL script into individual CQL statements.
 *
 * @author Dmytro Nosan
 * @since 4.0.1
 */
final class Parser {

	private final char[] chars;

	/**
	 * Constructs a new {@link Parser} for the given CQL script.
	 *
	 * @param script the CQL script to be parsed (must not be {@code null})
	 * @throws NullPointerException if {@code script} is {@code null}
	 */
	Parser(String script) {
		this.chars = script.toCharArray();
	}

	/**
	 * Parses the CQL script and returns a list of valid CQL statements.
	 *
	 * <p>This method removes comments, handles quoted strings, and splits the script
	 * into statements based on the semicolon {@code ;} delimiter.</p>
	 *
	 * @return a list of CQL statements (never {@code null}, but may be empty)
	 */
	List<String> getStatements() {
		List<String> statements = new ArrayList<>(8);
		StringBuilder statement = new StringBuilder();
		Context context = Context.NONE;
		for (int i = 0; i < this.chars.length; i++) {
			char c = this.chars[i];
			if (context == Context.NONE) {
				if (c == '/' && next(i, '/')) {
					context = Context.COMMENT;
					i++;
				}
				else if (c == '-' && next(i, '-')) {
					context = Context.COMMENT;
					i++;
				}
				else if (c == '/' && next(i, '*')) {
					context = Context.MULTI_COMMENT;
					i++;
				}
				else if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
					int start = i;
					while (next(i, '\n')) {
						i++;
					}
					while (next(i, '\r')) {
						i++;
					}
					while (next(i, '\t')) {
						i++;
					}
					while (next(i, ' ')) {
						i++;
					}
					int end = i;
					if (statement.length() > 0 && i != this.chars.length - 1) {
						statement.append(this.chars, start, end - start + 1);
					}
				}
				else {
					if (c == '"') {
						statement.append(c);
						context = Context.DOUBLE_QUOTE;
					}
					else if (c == '\'') {
						statement.append(c);
						context = Context.QUOTE;
					}
					else if (c == '$' && next(i, '$')) {
						statement.append(c).append('$');
						i++;
						context = Context.DOUBLE_DOLLAR;
					}
					else if (c == ';') {
						statements.add(statement.toString());
						statement.delete(0, statement.length());
					}
					else {
						statement.append(c);
					}
				}
			}
			else if (context == Context.MULTI_COMMENT && c == '*' && next(i, '/')) {
				context = Context.NONE;
				i++;
			}
			else if (context == Context.COMMENT && c == '\n') {
				context = Context.NONE;
			}
			else if (context == Context.QUOTE) {
				statement.append(c);
				if (c == '\'') {
					context = Context.NONE;
				}
			}
			else if (context == Context.DOUBLE_QUOTE) {
				statement.append(c);
				if (c == '"') {
					context = Context.NONE;
				}
			}
			else if (context == Context.DOUBLE_DOLLAR) {
				statement.append(c);
				if (c == '$' && next(i, '$')) {
					statement.append('$');
					context = Context.NONE;
					i++;
				}
			}
		}
		if (StringUtils.hasText(statement)) {
			statements.add(statement.toString());
		}
		return statements;
	}

	/**
	 * Checks if the next character in the script matches the expected value.
	 *
	 * @param index the current index
	 * @param expected the expected character
	 * @return {@code true} if the next character matches the expected value; otherwise {@code false}
	 */
	private boolean next(int index, char expected) {
		return index + 1 < this.chars.length && this.chars[index + 1] == expected;
	}

	/**
	 * Represents the context of the parser while processing the script.
	 */
	private enum Context {

		/**
		 * Indicates that the parser is not inside any special context.
		 */
		NONE,

		/**
		 * Represents a single-line comment (e.g., starting with {@code --} or {@code //}).
		 */
		COMMENT,

		/**
		 * Represents a multi-line comment (e.g., starting with {@code /*} and ending with {@code *\/}).
		 */
		MULTI_COMMENT,

		/**
		 * Represents a single-quoted string (e.g., {@code 'text'}).
		 */
		QUOTE,

		/**
		 * Represents a double-quoted string (e.g., {@code "text"}).
		 */
		DOUBLE_QUOTE,

		/**
		 * Represents a double-dollar string (e.g., {@code $$text$$}).
		 */
		DOUBLE_DOLLAR,

	}

}
