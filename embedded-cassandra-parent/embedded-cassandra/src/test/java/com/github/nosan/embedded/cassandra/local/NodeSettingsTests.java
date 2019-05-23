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
	void setPortAndAddress() {
		this.settings.setTransportStarted(false);
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Port is not present");
		this.settings.setPort(9042);
		this.settings.setAddress(ADDRESS);
		this.settings.setTransportStarted(true);
		assertThat(this.settings.getAddress()).isEqualTo(ADDRESS);
		assertThat(this.settings.getPort()).isEqualTo(9042);
		assertThat(this.settings.transportStarted()).hasValue(true);
		assertThat(this.settings.getPortOrSslPort()).isEqualTo(9042);
	}

	@Test
	void setSslPortAndAddress() {
		this.settings.setTransportStarted(false);
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getSslPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("SSL port is not present");
		this.settings.setSslPort(9142);
		this.settings.setAddress(ADDRESS);
		this.settings.setTransportStarted(true);
		assertThat(this.settings.getAddress()).isEqualTo(ADDRESS);
		assertThat(this.settings.getSslPort()).isEqualTo(9142);
		assertThat(this.settings.transportStarted()).hasValue(true);
		assertThat(this.settings.getPortOrSslPort()).isEqualTo(9142);
	}

	@Test
	void setRpcPortAndAddress() {
		this.settings.setRpcTransportStarted(false);
		assertThatThrownBy(this.settings::getAddress).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("Address is not present");
		assertThatThrownBy(this.settings::getRpcPort).isInstanceOf(NoSuchElementException.class)
				.hasStackTraceContaining("RPC port is not present");
		this.settings.setRpcPort(9160);
		this.settings.setRpcAddress(ADDRESS);
		this.settings.setRpcTransportStarted(true);
		assertThat(this.settings.getAddress()).isEqualTo(ADDRESS);
		assertThat(this.settings.getRpcPort()).isEqualTo(9160);
		assertThat(this.settings.rpcTransportStarted()).hasValue(true);
	}

}
