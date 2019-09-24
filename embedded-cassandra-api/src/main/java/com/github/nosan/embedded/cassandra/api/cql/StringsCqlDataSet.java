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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import com.github.nosan.embedded.cassandra.annotations.Nullable;

/**
 * {@link CqlDataSet} for a raw CQL scripts.
 *
 * @author Dmytro Nosan
 */
final class StringsCqlDataSet implements CqlDataSet {

	private final List<String> scripts;

	/**
	 * Constructs a new {@link StringsCqlDataSet} with the specified CQL scripts.
	 *
	 * @param scripts the CQL scripts
	 */
	StringsCqlDataSet(String... scripts) {
		this.scripts = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(scripts)));
	}

	@Override
	public List<String> getStatements() {
		List<String> statements = new ArrayList<>();
		for (String script : this.scripts) {
			statements.addAll(new Parser(script).getStatements());
		}
		return Collections.unmodifiableList(statements);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		StringsCqlDataSet that = (StringsCqlDataSet) other;
		return this.scripts.equals(that.scripts);
	}

	@Override
	public int hashCode() {
		return this.scripts.hashCode();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", StringsCqlDataSet.class.getSimpleName() + "[", "]")
				.add("scripts=" + this.scripts)
				.toString();
	}

}
