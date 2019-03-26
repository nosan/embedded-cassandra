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

package com.github.nosan.embedded.cassandra.junit;

import java.util.Collections;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.ClusterFactory;
import com.github.nosan.embedded.cassandra.test.junit.CassandraRule;
import com.github.nosan.embedded.cassandra.test.junit.CassandraRuleBuilder;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraRuleBuilder}.
 *
 * @author Dmytro Nosan
 */
public class CassandraRuleBuilderTests {

	@Test
	public void shouldBuildCassandraRule() {
		ClusterFactory clusterFactory = (settings) -> {
			throw new UnsupportedOperationException();
		};
		CassandraFactory cassandraFactory = () -> {
			throw new UnsupportedOperationException();
		};
		CqlScript script = () -> {
			throw new UnsupportedOperationException();
		};
		CassandraRule cassandraRule = new CassandraRuleBuilder().addScripts(script).setRegisterShutdownHook(false)
				.setClusterFactory(clusterFactory).setCassandraFactory(cassandraFactory).build();

		assertThat(ReflectionUtils.getField(cassandraRule, "cassandraFactory")).isEqualTo(cassandraFactory);
		assertThat(ReflectionUtils.getField(cassandraRule, "registerShutdownHook")).isEqualTo(false);
		assertThat(ReflectionUtils.getField(cassandraRule, "scripts")).isEqualTo(Collections.singletonList(script));
		assertThat(ReflectionUtils.getField(cassandraRule, "clusterFactory")).isEqualTo(clusterFactory);
	}

}
