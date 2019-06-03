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
 * {@link CqlScript} implementation for a given CQL {@code scripts}.
 *
 * @author Dmytro Nosan
 * @see CqlScript
 * @since 1.0.0
 */
public final class CqlScripts implements CqlScript {

	private final List<CqlScript> scripts;

	/**
	 * Create a new {@link CqlScripts} based on scripts.
	 *
	 * @param scripts CQL scripts
	 */
	public CqlScripts(@Nullable CqlScript... scripts) {
		this((scripts != null) ? Arrays.asList(scripts) : Collections.emptyList());
	}

	/**
	 * Create a new {@link CqlScripts} based on scripts.
	 *
	 * @param scripts CQL scripts
	 */
	public CqlScripts(@Nullable Collection<? extends CqlScript> scripts) {
		this.scripts = Collections
				.unmodifiableList(new ArrayList<>((scripts != null) ? scripts : Collections.emptyList()));
	}

	@Override
	public List<String> getStatements() {
		List<String> statements = new ArrayList<>();
		for (CqlScript script : this.scripts) {
			statements.addAll(script.getStatements());
		}
		return Collections.unmodifiableList(statements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.scripts);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		CqlScripts that = (CqlScripts) other;
		return Objects.equals(this.scripts, that.scripts);
	}

	@Override
	public String toString() {
		return this.scripts.stream().map(String::valueOf)
				.collect(Collectors.joining(",", getClass().getSimpleName() + " [", "]"));
	}

}
