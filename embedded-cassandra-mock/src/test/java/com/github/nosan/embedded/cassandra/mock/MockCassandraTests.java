/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.mock;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.api.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MockCassandra}.
 *
 * @author Dmytro Nosan
 */
class MockCassandraTests {

	@Test
	void testMockCassandra() {
		MockCassandra cassandra = MockCassandra.INSTANCE;
		cassandra.start();
		cassandra.stop();
		assertThat(cassandra).isEqualTo(MockCassandra.INSTANCE);
		assertThat(cassandra.getName()).isEqualTo("Mock Cassandra");
		assertThat(cassandra.toString()).isEqualTo("Mock Cassandra");
		assertThat(cassandra.getPort()).isEqualTo(-1);
		assertThat(cassandra.getRpcPort()).isEqualTo(-1);
		assertThat(cassandra.getSslPort()).isEqualTo(-1);
		assertThat(cassandra.getAddress()).isNull();
		assertThat(cassandra.getVersion()).isEqualTo(Version.of("0.0.0-mock"));
	}

}
