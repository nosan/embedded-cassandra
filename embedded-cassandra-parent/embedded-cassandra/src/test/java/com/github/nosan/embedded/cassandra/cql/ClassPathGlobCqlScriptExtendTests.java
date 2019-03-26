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

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ClassPathGlobCqlScript}.
 *
 * @author Dmytro Nosan
 */
class ClassPathGlobCqlScriptExtendTests {

	private static final String KEYSPACE = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = "
			+ "{'class':'SimpleStrategy', 'replication_factor':1}";

	@Test
	void findResourcesInJar() {
		URL url = getClass().getResource("/test.jar");
		String pattern = "nosan/**/*.cql";
		Collection<String> statements = getStatements(url, pattern);
		assertThat(statements).containsExactly(KEYSPACE);
	}

	@Test
	void findResourcesInWar() {
		URL url = getClass().getResource("/test.war");
		String pattern = "nosan/**/*.cql";
		Collection<String> statements = getStatements(url, pattern);
		assertThat(statements).containsExactly(KEYSPACE);
	}

	@Test
	void findResourcesInZip() {
		URL url = getClass().getResource("/test.zip");
		String pattern = "nosan/**/*.cql";
		Collection<String> statements = getStatements(url, pattern);
		assertThat(statements).containsExactly(KEYSPACE);
	}

	@Test
	void invalidGlobPatternSyntax() {
		URL url = getClass().getResource("/test.zip");
		String pattern = "{";
		assertThatThrownBy(() -> getStatements(url, pattern)).isInstanceOf(PatternSyntaxException.class);
	}

	@Test
	void assertHashCode() {
		assertThat(new ClassPathGlobCqlScript("**.cql")).hasSameHashCodeAs(new ClassPathGlobCqlScript("**.cql"));
		assertThat(new ClassPathGlobCqlScript("**.cql").hashCode()).isNotEqualTo(
				new ClassPathGlobCqlScript("*.cql").hashCode());
	}

	@Test
	void assertEquals() {
		assertThat(new ClassPathGlobCqlScript("**.cql")).isEqualTo(new ClassPathGlobCqlScript("**.cql")).isNotEqualTo(
				new ClassPathGlobCqlScript("**.cql", StandardCharsets.UTF_8)).isNotEqualTo(
				new ClassPathGlobCqlScript("**.cql", new URLClassLoader(new URL[0]))).isNotEqualTo(
				new ClassPathGlobCqlScript("*.cql"));
	}

	@Test
	void assertToString() {
		String pattern = "nosan/**/*.cql";
		assertThat(new ClassPathGlobCqlScript(pattern).toString()).contains(pattern);
	}

	private static Collection<String> getStatements(URL url, String pattern) {
		URLClassLoader cl = new URLClassLoader(new URL[]{url}, ClassUtils.getClassLoader());
		ClassPathGlobCqlScript script = new ClassPathGlobCqlScript(pattern, cl);
		return script.getStatements();
	}

}
