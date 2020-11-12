/*
 * Copyright 2020 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ResourceCqlScript}.
 *
 * @author Dmytro Nosan
 */
class ResourceCqlScriptTests {

	private final ResourceCqlScript script = new ResourceCqlScript(new ClassPathResource("schema.cql"));

	@Test
	void testEquals() {
		assertThat(this.script.equals(this.script)).isTrue();
		assertThat(this.script.equals(null)).isFalse();
		assertThat(this.script.equals(new ResourceCqlScript(new ClassPathResource("schema.cql")))).isTrue();
		assertThat(this.script.equals(new ResourceCqlScript(new ClassPathResource("schema.cql"),
				StandardCharsets.UTF_16LE))).isFalse();
	}

	@Test
	void testToString() {
		assertThat(this.script.toString()).contains("schema.cql");
	}

	@Test
	void testGetStatementsFail() {
		ClassPathResource resource = new ClassPathResource(UUID.randomUUID().toString());
		assertThatThrownBy(() -> new ResourceCqlScript(resource).getStatements())
				.hasStackTraceContaining("Could not open a stream for");
	}

	@Test
	void testGetStatements() {
		List<String> statements = this.script.getStatements();
		assertThat(statements).contains(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
	}

}
