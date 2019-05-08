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
 * Tests for {@link RandomPortSupplier}.
 *
 * @author Dmytro Nosan
 */
class RandomPortSupplierTests {

	@Test
	void shouldGet36RandomPorts() {
		Set<Integer> ports = new LinkedHashSet<>();
		RandomPortSupplier portSupplier = new RandomPortSupplier(InetAddress::getLoopbackAddress);
		for (int i = 0; i < 36; i++) {
			ports.add(portSupplier.get());
		}
		assertThat(ports).hasSize(36);
	}

}