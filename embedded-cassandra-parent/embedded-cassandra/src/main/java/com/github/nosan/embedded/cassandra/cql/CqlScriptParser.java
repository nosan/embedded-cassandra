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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.util.StringUtils;

/**
 * Utility class for parsing CQL Script into the statements.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.MAINTAINED)
public abstract class CqlScriptParser {

	private static final char SINGLE_QUOTE = '\'';

	private static final char DOUBLE_QUOTE = '"';

	private static final char STATEMENT = ';';

	private static final char LINE_SEPARATOR = '\n';

	private static final String DOUBLE_DOLLAR = "$$";

	private static final String SINGLE_DASH_COMMENT = "--";

	private static final String SINGLE_SLASH_COMMENT = "//";

	private static final String BLOCK_START_COMMENT = "/*";

	private static final String BLOCK_END_COMMENT = "*/";

	/**
	 * Parses script into the statements. Statements end in a semicolon (<b>statement1;statement..;statementN</b>).
	 * <p>Use the following notation to include comments in CQL code:
	 * <ol>
	 * <li>For a single line or end of line put a double hyphen before the text (<b>-- comment here</b>)</li>
	 * <li>For a single line or end of line put a double forward slash before the text (<b>// comment here</b>)</li>
	 * <li>For a block of comments put a forward slash asterisk at the beginning of the comment and then asterisk
	 * forward slash at the end (<b>/&#42; comment here &#42;/ </b>).</li>
	 * </ol>
	 * <p>Column names that contain characters that CQL cannot parse need to be enclosed in double quotation (<b>"</b>)
	 * marks in
	 * CQL.
	 * Dates, IP addresses, and strings need to be enclosed in single quotation (<b>'</b>) marks. To use a single
	 * quotation mark
	 * itself in a string literal, escape it using a single quotation mark. An alternative is to use dollar-quoted
	 * (<b>$$</b>)
	 * strings. Dollar-quoted string constants can be used to create functions, insert data, and select data when
	 * complex quoting is needed. Use double dollar signs to enclose the desired string.
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
		boolean doubleDollar = false;

		for (int index = 0; index < script.length(); index++) {

			char c = script.charAt(index);

			//single quote 'text'
			if (!doubleQuote && !doubleDollar && c == SINGLE_QUOTE) {
				result.append(SINGLE_QUOTE);
				singleQuote = !singleQuote;
				continue;
			}
			//double quote " text "
			else if (!singleQuote && !doubleDollar && c == DOUBLE_QUOTE) {
				result.append(DOUBLE_QUOTE);
				doubleQuote = !doubleQuote;
				continue;
			}
			//double dollars $$ some text $$
			else if (!singleQuote && !doubleQuote && script.startsWith(DOUBLE_DOLLAR, index)) {
				result.append(DOUBLE_DOLLAR);
				index++;
				doubleDollar = !doubleDollar;
				continue;
			}

			if (!singleQuote && !doubleQuote && !doubleDollar) {
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
				//statement
				else if (c == STATEMENT) {
					if (StringUtils.hasText(result)) {
						statements.add(result.toString());
						result = new StringBuilder();
					}
					continue;
				}
				//trim whitespaces and break lines.
				else if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
					if (StringUtils.isEmpty(result) || result.charAt(result.length() - 1) == ' ') {
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
		return Collections.unmodifiableList(statements);

	}

}
