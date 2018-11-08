/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PortUtils}.
 *
 * @author Dmytro Nosan
 */
public class PortUtilsTests {

	@Test
	public void shouldGet10Ports() {
		Set<Integer> ports = new LinkedHashSet<>(PortUtils.getPorts(10));
		assertThat(ports).hasSize(10);
	}

	@Test
	public void shouldBeBusy() throws IOException {
		int port = PortUtils.getPort();
		assertThat(PortUtils.isPortBusy((InetAddress) null, port)).isFalse();
		try (ServerSocket ss = new ServerSocket(port)) {
			assertThat(PortUtils.isPortBusy(InetAddress.getLoopbackAddress(), ss.getLocalPort())).isTrue();
		}

	}
}
