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

package com.github.nosan.embedded.cassandra.cql;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlScript}.
 *
 * @author Dmytro Nosan
 */
public class CqlScriptTests {


	@Test
	public void classpaths() {
		assertThat(CqlScript.classpath((String[]) null)).isNotNull();
		assertStatements(CqlScript.classpath("roles.cql"));
		assertStatements(CqlScript.classpath(getClass(), "/roles.cql"));
	}


	@Test
	public void urls() {
		assertThat(CqlScript.urls((URL[]) null)).isNotNull();
		assertStatements(CqlScript.urls(ClassUtils.getClassLoader().getResource("roles.cql")));
	}


	@Test
	public void files() throws URISyntaxException {
		assertThat(CqlScript.files((File[]) null)).isNotNull();
		assertStatements(CqlScript.files(new File(ClassUtils.getClassLoader().getResource("roles.cql").toURI())));
	}

	@Test
	public void paths() throws URISyntaxException {
		assertThat(CqlScript.paths((Path[]) null)).isNotNull();
		assertStatements(CqlScript.paths(Paths.get(ClassUtils.getClassLoader().getResource("roles.cql").toURI())));

	}

	@Test
	public void statements() {
		assertThat(CqlScript.statements((String[]) null)).isNotNull();
		assertStatements(CqlScript.statements("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)"));
	}


	private static void assertStatements(CqlScript script) {
		assertThat(script.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}
}
