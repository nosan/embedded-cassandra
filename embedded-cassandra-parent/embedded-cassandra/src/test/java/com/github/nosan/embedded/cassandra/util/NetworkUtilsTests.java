/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NetworkUtils}.
 *
 * @author Dmytro Nosan
 */
class NetworkUtilsTests {

	@Test
	void shouldResolveAddressV6ByInterfaceName() throws Exception {
		NetworkInterface iface = getInterface(true);
		assertThat(NetworkUtils.getAddressByInterface(iface.getName(), true))
				.isEqualTo(getAddress(iface, true));
	}

	@Test
	void shouldResolveAddressV4ByInterfaceName() throws Exception {
		NetworkInterface iface = getInterface(false);
		assertThat(NetworkUtils.getAddressByInterface(iface.getName(), false))
				.isEqualTo(getAddress(iface, false));
	}

	private static Optional<InetAddress> getAddress(NetworkInterface networkInterface, boolean ipv6) {
		Predicate<InetAddress> test = ipv6 ? Inet6Address.class::isInstance : Inet4Address.class::isInstance;
		return Collections.list(networkInterface.getInetAddresses())
				.stream()
				.filter(test)
				.findFirst();
	}

	private static NetworkInterface getInterface(boolean ipv6) throws SocketException {
		Predicate<InetAddress> test = ipv6 ? Inet6Address.class::isInstance : Inet4Address.class::isInstance;
		return Collections.list(NetworkInterface.getNetworkInterfaces())
				.stream()
				.filter(it -> Collections.list(it.getInetAddresses())
						.stream()
						.anyMatch(test))
				.findFirst()
				.orElseThrow(IllegalStateException::new);
	}

}
