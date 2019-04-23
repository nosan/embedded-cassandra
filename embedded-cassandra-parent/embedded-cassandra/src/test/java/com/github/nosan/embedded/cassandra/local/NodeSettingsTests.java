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
	void getRequiredAddress() {
		assertThatThrownBy(this.settings::getRequiredAddress).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("RPC and Native Transport are not enabled");
		this.settings.setTransportEnabled(true);
		assertThatThrownBy(this.settings::getRequiredAddress).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("RPC or Native transport is enabled, but Address is not present");
		this.settings.setAddress(InetAddress.getLoopbackAddress());
		assertThat(this.settings.getRequiredAddress()).isEqualTo(InetAddress.getLoopbackAddress());
	}

	@Test
	void getVersion() {
		assertThat(this.settings.getVersion()).isEqualTo(Version.parse("3.11.4"));
	}

	@Test
	void getRequiredPort() {
		assertThatThrownBy(this.settings::getRequiredPort).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("Native Transport is not enabled");
		this.settings.setTransportEnabled(true);
		assertThatThrownBy(this.settings::getRequiredPort).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("Native transport is enabled, but <unencrypted> port is not present");
		this.settings.setPort(9042);
		assertThat(this.settings.getRequiredPort()).isEqualTo(9042);
	}

	@Test
	void getRequiredSslPort() {
		assertThatThrownBy(this.settings::getRequiredSslPort).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("Native transport is not enabled");
		this.settings.setTransportEnabled(true);
		assertThatThrownBy(this.settings::getRequiredSslPort).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("Native transport is enabled, but <encrypted> port is not present");
		this.settings.setSslPort(9142);
		assertThat(this.settings.getRequiredSslPort()).isEqualTo(9142);
	}

	@Test
	void getRequiredRpcPort() {
		assertThatThrownBy(this.settings::getRequiredRpcPort).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("RPC transport is not enabled");
		this.settings.setRpcTransportEnabled(true);
		assertThatThrownBy(this.settings::getRequiredRpcPort).isInstanceOf(IllegalStateException.class)
				.hasStackTraceContaining("RPC transport is enabled, but rpc port is not present");
		this.settings.setRpcPort(9042);
		assertThat(this.settings.getRequiredRpcPort()).isEqualTo(9042);
	}

}
