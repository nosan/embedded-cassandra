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
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link NodeSettings}.
 *
 * @author Dmytro Nosan
 */
class NodeSettingsTests {

	private final NodeSettings settings = new NodeSettings(Version.parse("3.11.4"));

	@Test
	void getVersion() {
		assertThat(this.settings.getVersion()).isEqualTo(Version.parse("3.11.4"));
	}

	@Test
	void getRequiredAddress() {
		assertThatThrownBy(this.settings::getRequiredAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		this.settings.setAddress(InetAddress.getLoopbackAddress());
		assertThat(this.settings.getRequiredAddress()).isEqualTo(InetAddress.getLoopbackAddress());
	}

	@Test
	void getRequiredPort() {
		assertThatThrownBy(this.settings::getRequiredPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Port is not present");
		this.settings.setPort(9042);
		assertThat(this.settings.getRequiredPort()).isEqualTo(9042);
	}

	@Test
	void getRequiredSslPort() {
		assertThatThrownBy(this.settings::getRequiredSslPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("SSL port is not present");
		this.settings.setSslPort(9142);
		assertThat(this.settings.getRequiredSslPort()).isEqualTo(9142);
	}

	@Test
	void getRequiredRpcPort() {
		assertThatThrownBy(this.settings::getRequiredRpcPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("RPC port is not present");
		this.settings.setRpcPort(9042);
		assertThat(this.settings.getRequiredRpcPort()).isEqualTo(9042);
	}

	@Test
	void toStringTest() {
		this.settings.setRpcPort(9160);
		this.settings.setPort(9042);
		this.settings.setSslPort(9142);
		this.settings.setAddress(InetAddress.getLoopbackAddress());
		assertThat(this.settings.toString())
				.isEqualTo(String.format("NodeSettings [version=%s, address=%s, port=%d, sslPort=%d, rpcPort=%d]",
						"3.11.4", InetAddress.getLoopbackAddress(), 9042, 9142, 9160));

	}

}
