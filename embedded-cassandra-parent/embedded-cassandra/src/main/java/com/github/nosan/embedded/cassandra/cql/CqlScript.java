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

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * CQL Script that abstracts from the actual type of underlying source.
 *
 * @author Dmytro Nosan
 * @see CqlScripts
 * @see CqlStatements
 * @see ClassPathCqlScript
 * @see ClassPathPatternCqlScript
 * @see FileCqlScript
 * @see PathCqlScript
 * @see UrlCqlScript
 * @since 1.0.0
 */
@FunctionalInterface
public interface CqlScript {

	/**
	 * Factory method that creates {@link CqlScript} based on classpath {@code 'glob'} patterns.
	 *
	 * @param patterns classpath glob patterns
	 * @return CQL scripts
	 * @see ClassPathPatternCqlScript
	 * @since 1.2.6
	 */
	static CqlScript classpathPatterns(String... patterns) {
		return CqlScriptFactory.create(patterns, ClassPathPatternCqlScript::new);
	}

	/**
	 * Factory method that creates {@link CqlScript} based on classpath locations.
	 *
	 * @param locations classpath locations
	 * @return CQL scripts
	 * @see ClassPathCqlScript
	 */
	static CqlScript classpath(String... locations) {
		return CqlScriptFactory.create(locations, ClassPathCqlScript::new);
	}

	/**
	 * Factory method that creates {@link CqlScript} based on {@link URL}.
	 *
	 * @param locations URL locations
	 * @return CQL scripts
	 * @see UrlCqlScript
	 */
	static CqlScript urls(URL... locations) {
		return CqlScriptFactory.create(locations, UrlCqlScript::new);
	}

	/**
	 * Factory method that creates {@link CqlScript} based on {@link File}.
	 *
	 * @param locations File locations
	 * @return CQL scripts
	 * @see FileCqlScript
	 */
	static CqlScript files(File... locations) {
		return CqlScriptFactory.create(locations, FileCqlScript::new);
	}

	/**
	 * Factory method that creates {@link CqlScript} based on {@link Path}.
	 *
	 * @param locations Path locations
	 * @return CQL scripts
	 * @see PathCqlScript
	 */
	static CqlScript paths(Path... locations) {
		return CqlScriptFactory.create(locations, PathCqlScript::new);
	}

	/**
	 * Factory method that creates {@link CqlScript} based on statements.
	 *
	 * @param statements CQL statements
	 * @return CQL scripts
	 * @see CqlStatements
	 */
	static CqlScript statements(String... statements) {
		return new CqlStatements(statements);
	}

	/**
	 * Returns CQL Statements.
	 *
	 * @return CQL statements to execute.
	 */
	List<String> getStatements();

}
