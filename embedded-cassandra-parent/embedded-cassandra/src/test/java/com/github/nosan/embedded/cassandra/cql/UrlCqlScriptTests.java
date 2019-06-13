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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link UrlCqlScript}.
 *
 * @author Dmytro Nosan
 */
class UrlCqlScriptTests {

	private static final String ROLES = "/roles.cql";

	private static final String KEYSPACE = "keyspace.cql";

	@Test
	void assertStatements() {
		assertThat(classpath(ROLES).getStatements())
				.containsExactly("CREATE TABLE IF NOT EXISTS test.roles (id text PRIMARY KEY)");
	}

	@Test
	void assertHashCode() {
		assertThat(classpath(ROLES)).hasSameHashCodeAs(classpath(ROLES));
		assertThat(classpath(ROLES).hashCode()).isNotEqualTo(classpath(KEYSPACE).hashCode());
	}

	@Test
	void assertEquals() {
		assertThat(classpath(ROLES)).isEqualTo(classpath(ROLES)).isNotEqualTo(classpath(ROLES, StandardCharsets.UTF_16))
				.isNotEqualTo(classpath(KEYSPACE));
	}

	@Test
	void assertToString() {
		assertThat(classpath(ROLES).toString()).contains("roles.cql");
	}

	@Test
	void assertExceptionThrown() throws IOException {
		assertThatThrownBy(new UrlCqlScript(new URL("http://unknown"))::getStatements)
				.isInstanceOf(UncheckedIOException.class);
	}

	private UrlCqlScript classpath(String url, @Nullable Charset charset) {
		return new UrlCqlScript(getClass().getResource(url), charset);
	}

	private UrlCqlScript classpath(String url) {
		return new UrlCqlScript(getClass().getResource(url));
	}

}
