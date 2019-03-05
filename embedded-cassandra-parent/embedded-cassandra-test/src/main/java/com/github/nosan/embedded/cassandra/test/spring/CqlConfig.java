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

package com.github.nosan.embedded.cassandra.test.spring;

import com.github.nosan.embedded.cassandra.lang.Nullable;

/**
 * {@code CqlConfig} encapsulates the
 * attributes declared via {@link EmbeddedCassandra} or {@link Cql}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class CqlConfig {

	@Nullable
	private final Class<?> testClass;

	@Nullable
	private final String[] scripts;

	@Nullable
	private final String[] statements;

	@Nullable
	private final String encoding;

	/**
	 * Creates a {@link CqlConfig}.
	 *
	 * @param testClass a test class
	 * @param encoding The encoding for the supplied CQL scripts
	 * @param scripts The paths to the CQL scripts to execute.
	 * @param statements CQL statements to execute.
	 */
	CqlConfig(@Nullable Class<?> testClass, @Nullable String[] scripts, @Nullable String[] statements,
			@Nullable String encoding) {
		this.testClass = testClass;
		this.scripts = scripts;
		this.statements = statements;
		this.encoding = encoding;
	}

	/**
	 * Return the {@code test} class.
	 *
	 * @return The value of the {@code testClass} attribute
	 */
	@Nullable
	Class<?> getTestClass() {
		return this.testClass;
	}

	/**
	 * The paths to the CQL scripts to execute.
	 *
	 * @return The value of the {@code scripts} attribute
	 */
	@Nullable
	String[] getScripts() {
		return this.scripts;
	}

	/**
	 * CQL statements to execute.
	 *
	 * @return The value of the {@code statements} attribute
	 */
	@Nullable
	String[] getStatements() {
		return this.statements;
	}

	/**
	 * The encoding for the supplied CQL scripts.
	 *
	 * @return The value of the {@code encoding} attribute
	 */
	@Nullable
	String getEncoding() {
		return this.encoding;
	}

}
