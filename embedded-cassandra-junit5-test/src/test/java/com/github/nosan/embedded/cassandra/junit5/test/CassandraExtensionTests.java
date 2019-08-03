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

import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.nosan.embedded.cassandra.api.Cassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraExtension}.
 *
 * @author Dmytro Nosan
 */
@ExtendWith(CassandraExtension.class)
class CassandraExtensionTests {

	private final Cassandra cassandra;

	CassandraExtensionTests(Cassandra cassandra) {
		this.cassandra = cassandra;
	}

	@Test
	void testCassandra() throws Exception {
		assertThat(this.cassandra.getPort()).isNotEqualTo(-1);
		assertThat(this.cassandra.getAddress()).isNotNull();
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(this.cassandra.getAddress(), this.cassandra.getPort()));
		}
	}

}
