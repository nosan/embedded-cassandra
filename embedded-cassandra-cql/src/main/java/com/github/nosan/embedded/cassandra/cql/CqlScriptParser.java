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

/**
 * Utility class for parsing CQL Script into the statements.
 *
 * @author Dmytro Nosan
 */
abstract class CqlScriptParser {

	private static final char SINGLE_QUOTE = '\'';

	private static final char DOUBLE_QUOTE = '"';

	private static final char STATEMENT = ';';

	private static final char NEW_LINE = '\n';

	private static final char TAB = '\t';

	private static final String SINGLE_DASH_COMMENT = "--";

	private static final String SINGLE_SLASH_COMMENT = "//";

	private static final String BLOCK_START_COMMENT = "/*";

	private static final String BLOCK_END_COMMENT = "*/";

	private static final char SPACE = ' ';

	/**
	 * Parsing provided script into the statements using default CQL rules.
	 * @param cqlScript CQL script.
	 * @return list of the cql statements.
	 */
	static List<String> getStatements(String cqlScript) {
		List<String> statements = new ArrayList<>();
		StringBuilder result = new StringBuilder();

		boolean singleQuote = false;
		boolean doubleQuote = false;

		for (int index = 0; index < cqlScript.length(); index++) {

			char c = cqlScript.charAt(index);

			// quotes...
			if (c == SINGLE_QUOTE && !doubleQuote) {
				singleQuote = !singleQuote;
			}
			else if (c == DOUBLE_QUOTE && !singleQuote) {
				doubleQuote = !doubleQuote;
			}

			if (!singleQuote && !doubleQuote) {
				// single comments
				if (cqlScript.startsWith(SINGLE_DASH_COMMENT, index)
						|| cqlScript.startsWith(SINGLE_SLASH_COMMENT, index)) {
					if (cqlScript.indexOf(NEW_LINE, index) < 0) {
						break;
					}
					index = cqlScript.indexOf(NEW_LINE, index);
					continue;
				}
				// block comment
				else if (cqlScript.startsWith(BLOCK_START_COMMENT, index)) {
					if (cqlScript.indexOf(BLOCK_END_COMMENT, index) < 0) {
						throw new IllegalArgumentException(
								"Missing block comment '" + BLOCK_END_COMMENT + "'");
					}
					index = cqlScript.indexOf(BLOCK_END_COMMENT, index) + 1;
					continue;
				}
				else if (c == STATEMENT) {
					if (isNotBlank(result)) {
						statements.add(result.toString());
						result = new StringBuilder();
					}
					index = cqlScript.indexOf(STATEMENT, index);
					continue;
				}
				// superfluous spaces, new-lines and tabs.
				else if (c == NEW_LINE || c == TAB || c == SPACE) {
					if ((!isEmpty(result) && last(result) == SPACE) || isEmpty(result)) {
						continue;
					}
					// replace tab and new-line with space.
					c = SPACE;
				}
			}
			result.append(c);
		}
		if (isNotBlank(result)) {
			statements.add(result.toString());
		}
		return statements;

	}

	private static char last(StringBuilder result) {
		return result.charAt(result.length() - 1);
	}

	private static boolean isNotBlank(StringBuilder builder) {
		return !builder.toString().trim().isEmpty();
	}

	private static boolean isEmpty(StringBuilder builder) {
		return builder.toString().isEmpty();
	}

}
