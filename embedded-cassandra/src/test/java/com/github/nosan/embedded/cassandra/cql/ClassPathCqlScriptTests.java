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

package com.github.nosan.embedded.cassandra.cql;

import java.io.UncheckedIOException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassPathCqlScript}.
 *
 * @author Dmytro Nosan
 */
public class ClassPathCqlScriptTests {

	@Test
	public void systemScript() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("test.cql");
		assertThat(classPathCqlScript.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )");
	}

	@Test
	public void contextScript() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("test.cql", getClass());
		assertThat(classPathCqlScript.getStatements()).containsExactly("CREATE TABLE IF NOT EXISTS test.users ( id " +
				"text PRIMARY KEY )");
	}

	@Test
	public void contextWithLeadingSlashScript() {
		ClassPathCqlScript classPathCqlScript = new ClassPathCqlScript("/test.cql", getClass());
		assertThat(classPathCqlScript.getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles ( id text PRIMARY KEY )");
	}

	@Test(expected = UncheckedIOException.class)
	public void resourceContextClassNotFound() {
		new ClassPathCqlScript("bla-bla", getClass()).getStatements();
	}


}
