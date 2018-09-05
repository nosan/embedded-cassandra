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

package com.github.nosan.embedded.cassandra.cql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Utility class for parsing CQL Script into the statements.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public abstract class CqlScriptParser {

	private static final char SINGLE_QUOTE = '\'';

	private static final char DOUBLE_QUOTE = '"';

	private static final char STATEMENT = ';';

	private static final char LINE_SEPARATOR = '\n';

	private static final String SINGLE_DASH_COMMENT = "--";

	private static final String SINGLE_SLASH_COMMENT = "//";

	private static final String BLOCK_START_COMMENT = "/*";

	private static final String BLOCK_END_COMMENT = "*/";

	/**
	 * Parses script into the statements.
	 *
	 * @param script CQL script.
	 * @return CQL statements
	 */
	@Nonnull
	public static List<String> parse(@Nullable String script) {
		if (!StringUtils.hasText(script)) {
			return Collections.emptyList();
		}
		List<String> statements = new ArrayList<>();
		StringBuilder result = new StringBuilder();

		boolean singleQuote = false;
		boolean doubleQuote = false;

		for (int index = 0; index < script.length(); index++) {

			char c = script.charAt(index);

			// quotes...
			if (c == SINGLE_QUOTE && !doubleQuote) {
				singleQuote = !singleQuote;
			}
			else if (c == DOUBLE_QUOTE && !singleQuote) {
				doubleQuote = !doubleQuote;
			}

			if (!singleQuote && !doubleQuote) {
				// single comments
				if (script.startsWith(SINGLE_DASH_COMMENT, index)
						|| script.startsWith(SINGLE_SLASH_COMMENT, index)) {
					if (script.indexOf(LINE_SEPARATOR, index) < 0) {
						break;
					}
					index = script.indexOf(LINE_SEPARATOR, index);
					continue;
				}
				// block comment
				else if (script.startsWith(BLOCK_START_COMMENT, index)) {
					if (script.indexOf(BLOCK_END_COMMENT, index) < 0) {
						throw new IllegalArgumentException(String.format("Missing block comment (%s)",
								BLOCK_END_COMMENT));
					}
					index = script.indexOf(BLOCK_END_COMMENT, index) + 1;
					continue;
				}
				else if (c == STATEMENT) {
					if (StringUtils.hasText(result)) {
						statements.add(result.toString());
						result = new StringBuilder();
					}
					index = script.indexOf(STATEMENT, index);
					continue;
				}
				else if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
					if (StringUtils.isEmpty(result) || last(result) == ' ') {
						continue;
					}
					c = ' ';
				}
			}
			result.append(c);
		}
		if (StringUtils.hasText(result)) {
			statements.add(result.toString());
		}
		return statements;

	}

	private static char last(StringBuilder result) {
		return result.charAt(result.length() - 1);
	}

}
