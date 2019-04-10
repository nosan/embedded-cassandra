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

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PortUtils}.
 *
 * @author Dmytro Nosan
 */
class PortUtilsTests {

	@Test
	void shouldGet100Ports() {
		Set<Integer> ports = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ports.add(PortUtils.getPort());
		}
		assertThat(ports.size()).isBetween(95, 100);
	}

	@Test
	void shouldBeBusy() throws IOException {
		int port = PortUtils.getPort();
		assertThat(PortUtils.isPortBusy(null, port)).isFalse();
		try (ServerSocket ss = new ServerSocket(port)) {
			assertThat(PortUtils.isPortBusy(ss.getInetAddress(), ss.getLocalPort())).isTrue();
		}
	}

}
