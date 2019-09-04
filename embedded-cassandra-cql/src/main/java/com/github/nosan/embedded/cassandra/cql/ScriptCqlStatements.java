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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

/**
 * {@link CqlStatements} for a raw CQL scripts.
 *
 * @author Dmytro Nosan
 */
final class ScriptCqlStatements implements CqlStatements {

	private final List<String> scripts;

	/**
	 * Constructs a new {@link ScriptCqlStatements} with the specified CQL scripts.
	 *
	 * @param scripts the CQL scripts
	 */
	ScriptCqlStatements(String... scripts) {
		this.scripts = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(scripts)));
	}

	@Override
	public Iterator<String> iterator() {
		return this.scripts.stream().flatMap(script -> new Parser(script).getStatements().stream()).iterator();
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ScriptCqlStatements.class.getSimpleName() + "[", "]").add(
				"scripts=" + this.scripts).toString();
	}

}
