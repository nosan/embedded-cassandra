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
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link FileCqlScript}.
 *
 * @author Dmytro Nosan
 */
class FileCqlScriptTests {

	private static final String ROLES = "/roles.cql";

	private static final String KEYSPACE = "keyspace.cql";

	@Test
	void assertStatements() throws URISyntaxException {
		assertThat(classpath(ROLES).getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void assertHashCode() throws URISyntaxException {
		assertThat(classpath(ROLES)).hasSameHashCodeAs(classpath(ROLES));
		assertThat(classpath(ROLES).hashCode()).isNotEqualTo(classpath(KEYSPACE).hashCode());
	}

	@Test
	void assertEquals() throws URISyntaxException {
		assertThat(classpath(ROLES)).isEqualTo(classpath(ROLES)).isNotEqualTo(classpath(ROLES, StandardCharsets.UTF_16))
				.isNotEqualTo(classpath(KEYSPACE));
	}

	@Test
	void assertToString() throws URISyntaxException {
		assertThat(classpath(ROLES).toString()).contains(ROLES);
	}

	@Test
	void assertExceptionThrown() throws Exception {
		File file = new File(new URL("file:///localhost.unknown.net:8080").toURI());
		assertThatThrownBy(new FileCqlScript(file)::getStatements)
				.isInstanceOf(UncheckedIOException.class);
	}

	private FileCqlScript classpath(String url, @Nullable Charset charset) throws URISyntaxException {
		return new FileCqlScript(new File(getClass().getResource(url).toURI()), charset);
	}

	private FileCqlScript classpath(String url) throws URISyntaxException {
		return new FileCqlScript(new File(getClass().getResource(url).toURI()));
	}

}
