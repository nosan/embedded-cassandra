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

package com.github.nosan.embedded.cassandra.jupiter;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.jupiter.CassandraExtension;
import com.github.nosan.embedded.cassandra.test.jupiter.CassandraExtensionBuilder;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraExtension}.
 *
 * @author Dmytro Nosan
 */
public class CassandraExtensionBuilderTests {

	@Test
	public void shouldBuildCassandraExtension() {
		ClusterFactory clusterFactory = (settings) -> {
			throw new UnsupportedOperationException();
		};
		CassandraFactory cassandraFactory = () -> {
			throw new UnsupportedOperationException();
		};
		CqlScript script = () -> {
			throw new UnsupportedOperationException();
		};
		CassandraExtension cassandraExtension = new CassandraExtensionBuilder()
				.addScripts(script)
				.setRegisterShutdownHook(false)
				.setClusterFactory(clusterFactory)
				.setCassandraFactory(cassandraFactory)
				.build();

		assertThat(ReflectionUtils.getField(cassandraExtension, "cassandraFactory")).isEqualTo(cassandraFactory);
		assertThat(ReflectionUtils.getField(cassandraExtension, "registerShutdownHook")).isEqualTo(false);
		assertThat(ReflectionUtils.getField(cassandraExtension, "scripts")).isEqualTo(new CqlScript[]{script});
		assertThat(ReflectionUtils.getField(cassandraExtension, "clusterFactory")).isEqualTo(clusterFactory);
	}
}
