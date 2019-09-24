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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.annotations.Nullable;
import com.github.nosan.embedded.cassandra.commons.util.StringUtils;

/**
 * This class parses CQL script into the statements.
 *
 * @author Dmytro Nosan
 */
final class Parser {

	private final char[] chars;

	Parser(@Nullable String script) {
		this.chars = Objects.toString(script, "").toCharArray();
	}

	List<String> getStatements() {
		List<String> statements = new ArrayList<>();
		StringBuilder statement = new StringBuilder();
		State state = State.TEXT;
		for (int i = 0; i < this.chars.length; i++) {
			char c = this.chars[i];
			if (state == State.TEXT) {
				if (c == '/' && next(i, '/')) {
					state = State.COMMENT;
					i++;
				}
				else if (c == '-' && next(i, '-')) {
					state = State.COMMENT;
					i++;
				}
				else if (c == '/' && next(i, '*')) {
					state = State.MULTI_COMMENT;
					i++;
				}
				else if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
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
					if (statement.length() > 0 && i != this.chars.length - 1) {
						statement.append(' ');
					}
				}
				else {
					if (c == '"') {
						statement.append(c);
						state = State.DOUBLE_QUOTE;
					}
					else if (c == '\'') {
						statement.append(c);
						state = State.QUOTE;
					}
					else if (c == '$' && next(i, '$')) {
						statement.append(c).append('$');
						i++;
						state = State.DOUBLE_DOLLAR;
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
			else if (state == State.MULTI_COMMENT && c == '*' && next(i, '/')) {
				state = State.TEXT;
				i++;
			}
			else if (state == State.COMMENT && c == '\n') {
				state = State.TEXT;
			}
			else if (state == State.QUOTE) {
				statement.append(c);
				if (c == '\'') {
					state = State.TEXT;
				}
			}
			else if (state == State.DOUBLE_QUOTE) {
				statement.append(c);
				if (c == '"') {
					state = State.TEXT;
				}
			}
			else if (state == State.DOUBLE_DOLLAR) {
				statement.append(c);
				if (c == '$' && next(i, '$')) {
					statement.append('$');
					state = State.TEXT;
					i++;
				}
			}
		}
		if (StringUtils.hasText(statement)) {
			statements.add(statement.toString());
		}
		return statements;
	}

	private boolean next(int index, char expected) {
		return index + 1 < this.chars.length && this.chars[index + 1] == expected;
	}

	private enum State {
		TEXT,
		COMMENT,
		MULTI_COMMENT,
		QUOTE,
		DOUBLE_QUOTE,
		DOUBLE_DOLLAR,
	}

}
