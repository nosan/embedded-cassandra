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
 * Tests for {@link NativeTransportReadinessConsumer}.
 *
 * @author Dmytro Nosan
 */
class NativeTransportReadinessConsumerTests {

	private final NativeTransportReadinessConsumer readiness = new NativeTransportReadinessConsumer(
			Version.of("3.11.6"));

	@Test
	void isReadyWhenTransportStarted() {
		assertThat(this.readiness.isReady()).isFalse();
		this.readiness.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (unencrypted)");
		assertThat(this.readiness.isReady()).isTrue();
	}

	@Test
	void isReadyWhenTransportNotStarted() {
		assertThat(this.readiness.isReady()).isFalse();
		this.readiness.accept("Not starting native transport as requested");
		assertThat(this.readiness.isReady()).isTrue();
	}

	@Test
	void isReadyWhenLowerVersionTwo() {
		assertThat(new NativeTransportReadinessConsumer(Version.of("1.0.0")).isReady()).isTrue();
	}

	@Test
	void getPort() throws UnknownHostException {
		this.readiness.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (unencrypted)");
		assertThat(this.readiness.getPort()).isEqualTo(9042);
		assertThat(this.readiness.getSslPort()).isEqualTo(-1);
		assertThat(this.readiness.getAddress()).isEqualTo(InetAddress.getByName("127.0.0.1"));
	}

	@Test
	void getSslPort() throws UnknownHostException {
		this.readiness.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (encrypted)");
		assertThat(this.readiness.getPort()).isEqualTo(-1);
		assertThat(this.readiness.getSslPort()).isEqualTo(9042);
		assertThat(this.readiness.getAddress()).isEqualTo(InetAddress.getByName("127.0.0.1"));
	}

}
