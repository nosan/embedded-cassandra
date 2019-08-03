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

import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.ClassRule;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.api.Cassandra;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraRule}.
 *
 * @author Dmytro Nosan
 */
public class CassandraRuleTests {

	@ClassRule
	public static final CassandraRule rule = new CassandraRule();

	@Test
	public void testCassandra() throws Exception {
		Cassandra cassandra = rule.getCassandra();
		assertThat(cassandra.getPort()).isNotEqualTo(-1);
		assertThat(cassandra.getAddress()).isNotNull();
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(cassandra.getAddress(), cassandra.getPort()));
		}
	}

}
