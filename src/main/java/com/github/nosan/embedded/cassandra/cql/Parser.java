/*
 * Copyright 2020 the original author or authors.
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

final class Parser {

	private final char[] chars;

	Parser(String script) {
		this.chars = script.toCharArray();
	}

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
		if (!statement.toString().isEmpty()) {
			statements.add(statement.toString());
		}
		return statements;
	}

	private boolean next(int index, char expected) {
		return index + 1 < this.chars.length && this.chars[index + 1] == expected;
	}

	private enum Context {
		NONE,
		COMMENT,
		MULTI_COMMENT,
		QUOTE,
		DOUBLE_QUOTE,
		DOUBLE_DOLLAR,
	}

}
