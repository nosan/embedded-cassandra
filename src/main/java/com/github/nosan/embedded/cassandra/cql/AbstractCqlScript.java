/*
 * Copyright 2020-2025 the original author or authors.
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

import java.util.Collections;
import java.util.List;

import com.github.nosan.embedded.cassandra.commons.StringUtils;

/**
 * An abstract implementation of the {@link CqlScript} interface that facilitates parsing a CQL (Cassandra Query
 * Language) script into individual statements.
 *
 * <p>This class provides the base functionality for handling CQL scripts by converting them into a list of statements.
 * The actual script content is determined by the implementation of the {@link #getScript()} method in subclasses.</p>
 *
 * <p>Subclasses need only to provide the actual CQL script content by implementing the {@link #getScript()}
 * method.</p>
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public abstract class AbstractCqlScript implements CqlScript {

	/**
	 * Creates a new instance of {@link AbstractCqlScript}.
	 */
	protected AbstractCqlScript() {
	}

	/**
	 * Retrieves the list of statements parsed from the CQL script.
	 *
	 * @return the list of parsed CQL statements, or an empty list if the script is null, empty, or whitespace-only
	 */
	@Override
	public final List<String> getStatements() {
		String script = getScript();
		if (!StringUtils.hasText(script)) {
			return Collections.emptyList();
		}
		List<String> statements = new Parser(script).getStatements();
		return Collections.unmodifiableList(statements);
	}

	/**
	 * Retrieves the CQL script that will be parsed into statements.
	 *
	 * @return the CQL script to parse, or {@code null} if no script is defined
	 */
	protected abstract String getScript();

}
