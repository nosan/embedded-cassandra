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

	private static final InetAddress ADDRESS = InetAddress.getLoopbackAddress();

	private static final Version VERSION = Version.parse("3.11.4");

	private final NodeSettings settings = new NodeSettings(VERSION);

	@Test
	void getVersion() {
		assertThat(this.settings.getVersion()).isEqualTo(VERSION);
	}

	@Test
	void startTransport() {
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Port is not present");
		this.settings.startTransport(ADDRESS, 9042, false);
		assertThat(this.settings.getAddress()).isEqualTo(ADDRESS);
		assertThat(this.settings.getPort()).isEqualTo(9042);
		assertThat(this.settings.transportStarted()).hasValue(true);
	}

	@Test
	void startSslTransport() {
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getSslPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("SSL port is not present");
		this.settings.startTransport(ADDRESS, 9142, true);
		assertThat(this.settings.getAddress()).isEqualTo(ADDRESS);
		assertThat(this.settings.getSslPort()).isEqualTo(9142);
		assertThat(this.settings.transportStarted()).hasValue(true);
	}

	@Test
	void startRpcTransport() {
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getRpcPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("RPC port is not present");
		this.settings.startRpcTransport(ADDRESS, 9160);
		assertThat(this.settings.getAddress()).isEqualTo(ADDRESS);
		assertThat(this.settings.getRpcPort()).isEqualTo(9160);
		assertThat(this.settings.rpcTransportStarted()).hasValue(true);
	}

	@Test
	void stopRpcTransport() {
		this.settings.startRpcTransport(ADDRESS, 9160);
		this.settings.stopRpcTransport();
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getRpcPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("RPC port is not present");
		assertThat(this.settings.rpcTransportStarted()).hasValue(false);
	}

	@Test
	void stopTransport() {
		this.settings.startTransport(ADDRESS, 9142, true);
		this.settings.stopTransport();
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Port is not present");
		assertThatThrownBy(this.settings::getSslPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("SSL port is not present");
		assertThat(this.settings.transportStarted()).hasValue(false);
	}

	@Test
	void toStringTest() {
		this.settings.startTransport(ADDRESS, 9042, false);
		this.settings.startTransport(ADDRESS, 9142, true);
		this.settings.startRpcTransport(ADDRESS, 9160);
		assertThat(this.settings.toString())
				.isEqualTo(String.format("NodeSettings [version=%s, address=%s, port=%d, sslPort=%d, rpcPort=%d,"
								+ " rpcTransportStarted=%b, transportStarted=%b]",
						VERSION, InetAddress.getLoopbackAddress(), 9042, 9142, 9160, true, true));
	}

}
