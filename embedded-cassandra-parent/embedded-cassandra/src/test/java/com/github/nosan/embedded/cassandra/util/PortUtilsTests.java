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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PortUtils}.
 *
 * @author Dmytro Nosan
 */
public class PortUtilsTests {

	/*@Rule
	public ExpectedException throwable = ExpectedException.none();*/

	@Test
	public void shouldGetRandomPort() throws IOException {
		Set<Integer> ports = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			int port = PortUtils.getPort();
			ports.add(port);
			try (ServerSocket ss = new ServerSocket()) {
				ss.setReuseAddress(true);
				ss.bind(new InetSocketAddress(InetAddress.getLocalHost(), port), 1);
				ss.getLocalPort();
			}
		}
		assertThat(ports.size()).isBetween(90, 100);
	}

	/*
	* Run me if you change the class! This test is heavy.
	@Test
	public void shouldNotGetRandomPort() throws Exception {
		this.throwable.expectMessage("Could not find an available port in the range [49152, 65535])");
		List<ServerSocket> sockets = new ArrayList<>();
		try {
			for (int i = 0; i < 30000; i++) {
				sockets.add(new ServerSocket(PortUtils.getPort(), 1));
			}
		}
		finally {
			sockets.forEach(IOUtils::closeQuietly);
		}
	}
	*/
}
