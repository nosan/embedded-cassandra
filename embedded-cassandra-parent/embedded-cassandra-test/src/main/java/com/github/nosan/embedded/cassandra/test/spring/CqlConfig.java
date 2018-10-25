/*
 * Copyright 2018-2018 the original author or authors.
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

import javax.annotation.Nullable;

/**
 * {@code CqlConfig} encapsulates the
 * attributes declared via {@link EmbeddedCassandra} or {@link Cql}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
class CqlConfig {

	@Nullable
	private Class<?> testClass;

	@Nullable
	private String[] scripts;

	@Nullable
	private String[] statements;

	@Nullable
	private String encoding;

	/**
	 * Return the {@code test} class.
	 *
	 * @return The value of the {@code testClass} attribute
	 */
	@Nullable
	public Class<?> getTestClass() {
		return this.testClass;
	}

	/**
	 * Initializes the value for the {@link CqlConfig#getTestClass} attribute.
	 *
	 * @param testClass The value for testClass
	 */
	public void setTestClass(@Nullable Class<?> testClass) {
		this.testClass = testClass;
	}

	/**
	 * Return CQL Scripts.
	 *
	 * @return The value of the {@code scripts} attribute
	 */
	@Nullable
	public String[] getScripts() {
		return this.scripts;
	}

	/**
	 * Initializes the value for the {@link CqlConfig#getScripts} attribute.
	 *
	 * @param scripts The value for scripts
	 */
	public void setScripts(@Nullable String[] scripts) {
		this.scripts = scripts;
	}

	/**
	 * Return CQL Statements.
	 *
	 * @return The value of the {@code statements} attribute
	 */
	@Nullable
	public String[] getStatements() {
		return this.statements;
	}

	/**
	 * Initializes the value for the {@link CqlConfig#getStatements} attribute.
	 *
	 * @param statements The value for statements
	 */
	public void setStatements(@Nullable String[] statements) {
		this.statements = statements;
	}

	/**
	 * Return the encoding of {@link #getScripts()}.
	 *
	 * @return The value of the {@code encoding} attribute
	 */
	@Nullable
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * Initializes the value for the {@link CqlConfig#getEncoding} attribute.
	 *
	 * @param encoding The value for encoding
	 */
	public void setEncoding(@Nullable String encoding) {
		this.encoding = encoding;
	}
}
