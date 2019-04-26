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
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScript}.
 *
 * @author Dmytro Nosan
 */
class CqlScriptTests {

	@Test
	void assertClasspathPatterns() {
		assertStatements(CqlScript.classpathPatterns("roles.cql"));
		assertStatements(CqlScript.classpathPatterns("*.cql"));
	}

	@Test
	void assertClasspath() {
		assertStatements(CqlScript.classpath("roles.cql"));
	}

	@Test
	void assertUrls() {
		assertStatements(CqlScript.urls(getClass().getResource("/roles.cql")));
	}

	@Test
	void assertFiles() throws URISyntaxException {
		assertStatements(CqlScript.files(new File(getClass().getResource("/roles.cql").toURI())));
	}

	@Test
	void assertPaths() throws URISyntaxException {
		assertStatements(CqlScript.paths(Paths.get(getClass().getResource("/roles.cql").toURI())));

	}

	@Test
	void assertStatements() {
		assertStatements(CqlScript.statements("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)"));
	}

	private static void assertStatements(CqlScript script) {
		assertThat(script.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

}
