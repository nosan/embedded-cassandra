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
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NativeTransportParser}.
 *
 * @author Dmytro Nosan
 */
class NativeTransportParserTests {

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
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThatThrownBy(() -> parser
				.accept("Starting listening for CQL clients on localhost/256.256.256.256:9042 (unencrypted)"))
				.hasMessageContaining("256.256.256.256");
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeStartedUnencryptedPort() throws UnknownHostException {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		parser.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (unencrypted)");
		assertThat(parser.isStarted()).isTrue();
		assertThat(parser.getAddress()).isEqualTo(InetAddress.getByName("localhost"));
		assertThat(parser.getPort()).isEqualTo(9042);
		assertThat(parser.getSslPort()).isNull();
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeStartedEncryptedPort() throws UnknownHostException {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		parser.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (encrypted)");
		assertThat(parser.isStarted()).isTrue();
		assertThat(parser.getAddress()).isEqualTo(InetAddress.getByName("localhost"));
		assertThat(parser.getPort()).isEqualTo(9042);
		assertThat(parser.getSslPort()).isNull();
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeStartedUnencryptedAndEncryptedPorts() throws UnknownHostException {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		when(this.database.getConfigProperties())
				.thenReturn(Collections.singletonMap("native_transport_port_ssl", 9142));
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		parser.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (unencrypted)");
		assertThat(parser.isStarted()).isFalse();
		parser.accept("Starting listening for CQL clients on localhost/127.0.0.1:9142 (encrypted)");
		assertThat(parser.isStarted()).isTrue();
		assertThat(parser.getAddress()).isEqualTo(InetAddress.getByName("localhost"));
		assertThat(parser.getPort()).isEqualTo(9042);
		assertThat(parser.getSslPort()).isEqualTo(9142);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeDisabledInvalidVersion() {
		when(this.database.getVersion()).thenReturn(Version.parse("1.0.0"));
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeDisabledTransportNotStarted() {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isFalse();
		parser.accept("Not starting native transport as requested.");
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeDisabledWhenBothTransportsDisabled() {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isFalse();
		parser.accept("Not starting client transports");
		assertThat(parser.isStarted()).isFalse();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

	@Test
	void shouldBeFailed() {
		when(this.database.getVersion()).thenReturn(CassandraBuilder.DEFAULT_VERSION);
		NativeTransportParser parser = new NativeTransportParser(this.database);
		verify(this.stderr).attach(parser);
		verify(this.stdout).attach(parser);
		parser.accept("Starting listening for CQL clients on localhost/127.0.0.1:9042 (unencrypted)");
		parser.accept("Failed to bind port 9042 on 127.0.0.1.");
		assertThat(parser.isFailed()).isTrue();
		assertThat(parser.isParsed()).isTrue();
		parser.close();
		verify(this.stderr).detach(parser);
		verify(this.stdout).detach(parser);
	}

}
