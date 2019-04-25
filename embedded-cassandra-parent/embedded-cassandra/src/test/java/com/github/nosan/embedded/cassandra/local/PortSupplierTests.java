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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PortSupplier}.
 *
 * @author Dmytro Nosan
 */
class PortSupplierTests {

	@Test
	void shouldGet100RandomPorts() {
		Set<Integer> ports = new LinkedHashSet<>();
		try (PortSupplier portSupplier = new PortSupplier()) {
			for (int i = 0; i < 100; i++) {
				Integer port = portSupplier.get();
				assertThat(isListen(port)).isTrue();
				ports.add(port);

			}
		}
		assertThat(ports).hasSize(100);
		assertThat(ports).doesNotContain(7000, 7001, 7199, 9042, 9142, 9160);
		for (Integer port : ports) {
			assertThat(isListen(port)).isFalse();
		}
	}

	private boolean isListen(int port) {
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1000);
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

}
