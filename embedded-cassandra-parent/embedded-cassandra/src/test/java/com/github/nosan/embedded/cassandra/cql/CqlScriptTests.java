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

package com.github.nosan.embedded.cassandra.cql;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
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
	void assertClasspathsGlobs() {
		assertThat(CqlScript.classpathGlobs((String[]) null)).isNotNull();
		assertStatements(CqlScript.classpathGlobs("roles.cql"));
		assertStatements(CqlScript.classpathGlobs("*.cql"));
	}

	@Test
	void assertClasspaths() {
		assertThat(CqlScript.classpath((String[]) null)).isNotNull();
		assertStatements(CqlScript.classpath("roles.cql"));
		assertStatements(CqlScript.classpath(getClass(), "/roles.cql"));
	}

	@Test
	void assertUrls() {
		assertThat(CqlScript.urls((URL[]) null)).isNotNull();
		assertStatements(CqlScript.urls(getClass().getResource("/roles.cql")));
	}

	@Test
	void assertFiles() throws URISyntaxException {
		assertThat(CqlScript.files((File[]) null)).isNotNull();
		assertStatements(CqlScript.files(new File(getClass().getResource("/roles.cql").toURI())));
	}

	@Test
	void assertPaths() throws URISyntaxException {
		assertThat(CqlScript.paths((Path[]) null)).isNotNull();
		assertStatements(CqlScript.paths(Paths.get(getClass().getResource("/roles.cql").toURI())));

	}

	@Test
	void assertStatements() {
		assertThat(CqlScript.statements((String[]) null)).isNotNull();
		assertStatements(CqlScript.statements("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)"));
	}

	private static void assertStatements(CqlScript script) {
		assertThat(script.getStatements()).containsExactly(
				"CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

}
