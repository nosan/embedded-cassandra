/*
 * Copyright 2020-2024 the original author or authors.
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

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@link CqlScript} implementation for static statements.
 *
 * @author Dmytro Nosan
 * @since 5.0.0
 */
public class StatementsCqlScript implements CqlScript {

	private final List<String> statements;

	/**
	 * Creates {@link  StatementsCqlScript} for a given statements.
	 *
	 * @param statements the statements.
	 */
	public StatementsCqlScript(List<? extends String> statements) {
		this.statements = List.copyOf(statements);
	}

	@Override
	public List<String> getStatements() {
		return this.statements;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		StatementsCqlScript that = (StatementsCqlScript) o;
		return Objects.equals(this.statements, that.statements);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.statements);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", StatementsCqlScript.class.getSimpleName() + "[", "]").add(
				"statements=" + this.statements).toString();
	}

}
