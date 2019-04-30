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

import java.net.InetAddress;
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
	void shouldGet20RandomPorts() throws InterruptedException {
		Set<Integer> ports = new LinkedHashSet<>();
		InetAddress address = InetAddress.getLoopbackAddress();
		try (PortSupplier portSupplier = new PortSupplier(address)) {
			for (int i = 0; i < 20; i++) {
				Integer port = portSupplier.get();
				assertThat(NetworkUtils.isListen(address, port)).describedAs("Port %d is not busy", port).isTrue();
				ports.add(port);
			}
		}
		assertThat(ports).hasSize(20);
		Thread.sleep(250);
		for (Integer port : ports) {
			assertThat(NetworkUtils.isListen(address, port)).describedAs("Port %d is busy", port).isFalse();
		}
	}

}
