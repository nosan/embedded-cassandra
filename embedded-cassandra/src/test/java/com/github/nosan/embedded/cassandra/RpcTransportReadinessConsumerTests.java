/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.api.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RpcTransportReadinessConsumer}.
 *
 * @author Dmytro Nosan
 */
class RpcTransportReadinessConsumerTests {

	private final RpcTransportReadinessConsumer readiness = new RpcTransportReadinessConsumer(Version.of("4.0-beta1"));

	@Test
	void isReady() {
		assertThat(this.readiness.isReady()).isFalse();
		this.readiness.accept("ThriftServer.java:116 - Binding thrift service to localhost/127.0.0.1:9160");
		assertThat(this.readiness.isReady()).isTrue();
	}

	@Test
	void isReadyWhenVersion4() {
		assertThat(new RpcTransportReadinessConsumer(Version.of("4.0.0")).isReady()).isTrue();
	}

	@Test
	void isReadyWhenRpcNotStarted() {
		assertThat(this.readiness.isReady()).isFalse();
		this.readiness.accept("Not starting RPC server as requested");
		assertThat(this.readiness.isReady()).isTrue();

	}

	@Test
	void getRpcPort() throws UnknownHostException {
		this.readiness.accept("ThriftServer.java:116 - Binding thrift service to localhost/127.0.0.1:9160");
		assertThat(this.readiness.getRpcPort()).isEqualTo(9160);
		assertThat(this.readiness.getAddress()).isEqualTo(InetAddress.getByName("127.0.0.1"));
	}

}
