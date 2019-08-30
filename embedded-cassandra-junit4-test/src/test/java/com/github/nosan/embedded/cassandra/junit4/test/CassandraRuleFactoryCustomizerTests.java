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

package com.github.nosan.embedded.cassandra.junit4.test;

import org.junit.ClassRule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.api.CassandraFactoryCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraRule} with a custom {@link CassandraFactoryCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class CassandraRuleFactoryCustomizerTests {

	@ClassRule
	public static final CassandraRule rule = new CassandraRule(cassandraFactory -> cassandraFactory.setPort(9042));

	@Test
	public void testCustomFactoryCustomizer() {
		assertThat(rule.getCassandra().getPort()).isEqualTo(9042);
	}

}
