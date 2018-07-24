/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.process;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;

import de.flapdoodle.embed.process.runtime.Network;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.nosan.embedded.cassandra.Config;

/**
 * Tests for {@link TransportUtils}.
 *
 * @author Dmytro Nosan
 */
public class TransportUtilsTests {

	@Rule
	public ExpectedException throwable = ExpectedException.none();

	@Test
	public void checkConnection() throws IOException {
		this.throwable.expect(IOException.class);
		this.throwable.expectMessage("Cassandra process transport has not been started correctly.");
		Config config = new Config();
		TransportUtils.check(config, 1, Duration.ZERO);
	}


	@Test
	public void shouldNotEnabledTransport() throws IOException {
		Config config = new Config();
		config.setStartRpc(false);
		config.setStartNativeTransport(false);
		TransportUtils.check(config, 1, Duration.ZERO);
	}

	@Test
	public void rpcTransport() throws IOException {
		Config config = new Config();
		config.setRpcPort(Network.getFreeServerPort());
		config.setStartRpc(true);
		config.setStartNativeTransport(false);
		try (ServerSocket ignore = new ServerSocket(config.getRpcPort())) {
			TransportUtils.check(config, 1, Duration.ZERO);
		}

	}

	@Test
	public void nativeTransport() throws IOException {
		Config config = new Config();
		config.setNativeTransportPort(Network.getFreeServerPort());
		try (ServerSocket ignore = new ServerSocket(config.getNativeTransportPort())) {
			TransportUtils.check(config, 1, Duration.ZERO);

		}
	}
}
