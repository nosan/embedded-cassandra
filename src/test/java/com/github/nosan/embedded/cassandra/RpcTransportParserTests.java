/*
 * Copyright 2020-2021 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RpcTransportParser}.
 *
 * @author Dmytro Nosan
 */
class RpcTransportParserTests {

	private static final Version VERSION = Version.parse("3.11.8");

	private final Process.Output stdout = Mockito.mock(Process.Output.class);

	private final Process.Output stderr = Mockito.mock(Process.Output.class);

	private final CassandraDatabase database = Mockito.mock(CassandraDatabase.class);

	@BeforeEach
	void setUp() {
		when(this.database.getStdOut()).thenReturn(this.stdout);
		when(this.database.getStdErr()).thenReturn(this.stderr);
	}

	@Test
	void shouldFailAddressInvalid() {
		when(this.database.getVersion()).thenReturn(VERSION);
		RpcTransportParser parser = new RpcTransportParser(this.database);
		verify(this.stdout).attach(parser);
		verify(this.stderr).attach(parser);
		assertThatThrownBy(() -> parser.accept("Binding thrift service to localhost/256.256.256.256:9160"))
				.hasMessageContaining("256.256.256.256");
		parser.close();
		verify(this.stdout).detach(parser);
		verify(this.stderr).detach(parser);
	}

	@Test
	void shouldBeStartedRpcPort() throws UnknownHostException {
		when(this.database.getVersion()).thenReturn(VERSION);
		RpcTransportParser parser = new RpcTransportParser(this.database);
		verify(this.stdout).attach(parser);
		verify(this.stderr).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		parser.accept("Binding thrift service to localhost/127.0.0.1:9160");
		assertThat(parser.isStarted()).isFalse();
		parser.accept("Listening for thrift clients...");
		assertThat(parser.isStarted()).isTrue();
		assertThat(parser.getAddress()).isEqualTo(InetAddress.getByName("localhost"));
		assertThat(parser.getPort()).isEqualTo(9160);
		parser.close();
		verify(this.stdout).detach(parser);
		verify(this.stderr).detach(parser);
	}

	@Test
	void shouldBeDisabledInvalidVersion() {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		RpcTransportParser parser = new RpcTransportParser(this.database);
		verify(this.stdout).attach(parser);
		verify(this.stderr).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
		verify(this.stderr).detach(parser);
	}

	@Test
	void shouldBeDisabledTransportNotStarted() {
		when(this.database.getVersion()).thenReturn(VERSION);
		RpcTransportParser parser = new RpcTransportParser(this.database);
		verify(this.stdout).attach(parser);
		verify(this.stderr).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isFalse();
		parser.accept(" Not starting RPC server as requested");
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
		verify(this.stderr).detach(parser);
	}

	@Test
	void shouldBeDisabledWhenBothTransportsDisabled() {
		when(this.database.getVersion()).thenReturn(VERSION);
		RpcTransportParser parser = new RpcTransportParser(this.database);
		verify(this.stdout).attach(parser);
		verify(this.stderr).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isFalse();
		parser.accept("Not starting client transports");
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
		verify(this.stderr).detach(parser);
	}

	@Test
	void shouldBeFailed() {
		when(this.database.getVersion()).thenReturn(VERSION);
		RpcTransportParser parser = new RpcTransportParser(this.database);
		verify(this.stdout).attach(parser);
		verify(this.stderr).attach(parser);
		parser.accept("Unable to create thrift socket to localhost/127.0.0.1:9160");
		assertThat(parser.isFailed()).isTrue();
		parser.close();
		verify(this.stdout).detach(parser);
		verify(this.stderr).detach(parser);
	}

}
