/*
 * Copyright 2020-2024 the original author or authors.
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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultCqlDataSet}.
 *
 * @author Dmytro Nosan
 */
class DefaultCqlDataSetTests {

	private final ClassPathResource resource = new ClassPathResource("schema.cql");

	private final ResourceCqlScript script = new ResourceCqlScript(this.resource);

	private final DefaultCqlDataSet dataSet = new DefaultCqlDataSet(Collections.singleton(this.script));

	@Test
	void testEquals() {
		assertThat(this.dataSet.equals(this.dataSet)).isTrue();
		assertThat(this.dataSet.equals(null)).isFalse();
		assertThat(this.dataSet.equals(new DefaultCqlDataSet(
				Collections.singleton(new ResourceCqlScript(this.resource))))).isTrue();
		assertThat(this.dataSet.equals(new DefaultCqlDataSet(Collections.singleton(new ResourceCqlScript(this.resource,
				StandardCharsets.UTF_16LE))))).isFalse();
	}

	@Test
	void testToString() {
		assertThat(this.dataSet.toString()).contains(this.script.toString());
	}

	@Test
	void testHashcode() {
		assertThat(this.dataSet.hashCode())
				.isEqualTo(Collections.singleton(this.dataSet).hashCode());
	}

	@Test
	void testGetStatements() {
		List<String> statements = this.dataSet.getStatements();
		assertThat(statements).contains(
				"CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
	}

	@Test
	void testGetScripts() {
		assertThat(this.dataSet.getScripts()).contains(this.script);
	}

}
