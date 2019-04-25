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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Ports}.
 *
 * @author Dmytro Nosan
 */
class PortsTests {

	@Test
	void shouldCreatePorts() {
		Ports ports = new Ports(9042, 9160, 7000, 7001, 7199);
		assertThat(ports.getJmxLocalPort()).isEqualTo(7199);
		assertThat(ports.getPort()).isEqualTo(9042);
		assertThat(ports.getRpcPort()).isEqualTo(9160);
		assertThat(ports.getStoragePort()).isEqualTo(7000);
		assertThat(ports.getSslStoragePort()).isEqualTo(7001);
	}

}
