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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link NetworkUtils}.
 *
 * @author Dmytro Nosan
 */
class NetworkUtilsTests {

	@Test
	void isListen() throws IOException {
		int port;
		InetAddress address;
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			port = serverSocket.getLocalPort();
			address = serverSocket.getInetAddress();
			assertThat(NetworkUtils.isListen(address, port)).isTrue();
		}
		assertThat(NetworkUtils.isListen(address, port)).isFalse();
	}

	@Test
	void shouldGetLocalhost() throws UnknownHostException {
		InetAddress localhost = NetworkUtils.getLocalhost();
		assertThat(localhost).isEqualTo(InetAddress.getByName("localhost"));
	}

	@Test
	void shouldGetPort() {
		assertThat(NetworkUtils.getPort("9140")).isEqualTo(9140);
	}

	@Test
	void shouldGetAddress() {
		assertThat(NetworkUtils.getAddress(InetAddress.getLoopbackAddress().getHostAddress())).
				isEqualTo(InetAddress.getLoopbackAddress());
	}

	@Test
	void shouldNotGetPort() {
		assertThatThrownBy(() -> NetworkUtils.getPort("em...?")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldNotGetAddress() {
		assertThatThrownBy(() -> NetworkUtils.getAddress("em...?")).isInstanceOf(IllegalArgumentException.class);
	}

}
