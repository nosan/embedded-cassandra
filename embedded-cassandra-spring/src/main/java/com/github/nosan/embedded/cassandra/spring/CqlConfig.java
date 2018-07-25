/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.spring;

/**
 * {@code CqlScriptConfig} encapsulates the
 * attributes declared via {@link EmbeddedCassandra} or {@link Cql}.
 *
 * @author Dmytro Nosan
 */
class CqlConfig {

	private final Class<?> testClass;

	private final String[] scripts;

	private final String[] statements;

	private final String encoding;

	CqlConfig(Class<?> testClass, String[] scripts, String[] statements, String encoding) {
		this.testClass = testClass;
		this.scripts = scripts;
		this.statements = statements;
		this.encoding = encoding;
	}

	Class<?> getTestClass() {
		return this.testClass;
	}

	String[] getScripts() {
		return this.scripts;
	}

	String[] getStatements() {
		return this.statements;
	}

	String getEncoding() {
		return this.encoding;
	}
}
