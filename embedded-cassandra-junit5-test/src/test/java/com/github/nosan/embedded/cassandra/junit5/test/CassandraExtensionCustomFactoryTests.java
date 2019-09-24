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

package com.github.nosan.embedded.cassandra.junit5.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.api.CassandraFactory;
import com.github.nosan.embedded.cassandra.mock.MockCassandra;
import com.github.nosan.embedded.cassandra.mock.MockCassandraFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraExtension} with a custom {@link CassandraFactory}.
 *
 * @author Dmytro Nosan
 */
class CassandraExtensionCustomFactoryTests {

	@RegisterExtension
	static final CassandraExtension extension = new CassandraExtension()
			.withCassandraFactory(new MockCassandraFactory());

	@Test
	void testCustomFactory() {
		assertThat(extension.getCassandra()).isEqualTo(MockCassandra.INSTANCE);
	}

}
