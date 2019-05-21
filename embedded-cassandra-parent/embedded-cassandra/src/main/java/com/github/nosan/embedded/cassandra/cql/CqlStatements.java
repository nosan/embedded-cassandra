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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * {@link CqlScript} implementation for a given CQL {@code statements}.
 *
 * @author Dmytro Nosan
 * @see CqlScript#statements(String...)
 * @since 1.0.0
 */
public final class CqlStatements implements CqlScript {

	private final List<String> statements;

	/**
	 * Create a new {@link CqlStatements} based on statements.
	 *
	 * @param statements CQL statements
	 */
	public CqlStatements(@Nullable String... statements) {
		this((statements != null) ? Arrays.asList(statements) : Collections.emptyList());
	}

	/**
	 * Create a new {@link CqlStatements} based on statements.
	 *
	 * @param statements CQL statements
	 */
	public CqlStatements(@Nullable Collection<String> statements) {
		this.statements = Collections
				.unmodifiableList(new ArrayList<>((statements != null) ? statements : Collections.emptyList()));
	}

	@Override
	public List<String> getStatements() {
		return this.statements;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.statements);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		CqlStatements that = (CqlStatements) other;
		return Objects.equals(this.statements, that.statements);
	}

	@Override
	public String toString() {
		return this.statements.stream()
				.collect(Collectors.joining(",", getClass().getSimpleName() + " [", "]"));
	}

}
