/*
 * Copyright 2018-2018 the original author or authors.
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * Utility methods for dealing with network.
 *
 * @author Dmytro Nosan
 * @since 1.1.0
 */
public abstract class NetworkUtils {
	/**
	 * Determines the IP address of a host, given the host's name.
	 *
	 * @param address the specified host
	 * @return an IP address for the given host name.
	 * @throws IllegalArgumentException if no IP address for the host could be found
	 */
	@Nonnull
	public static InetAddress getInetAddress(@Nonnull String address) {
		Objects.requireNonNull(address, "Address must not be null");
		try {
			return InetAddress.getByName(address);
		}
		catch (UnknownHostException ex) {
			throw new IllegalArgumentException(ex);
		}
	}


	/**
	 * Return the first IPv4 or IPv6 address by the interface name, if could not find IPv4 or IPv6, then first will be
	 * used.
	 *
	 * @param interfaceName the interface name (e.g. en0)
	 * @param useIpv6 whether to use IPv6 or not
	 * @return an IP address for the given interface name.
	 * @throws IllegalArgumentException if interface is unknown or could not find IPv6 or IPv4 address.
	 */
	@Nonnull
	public static InetAddress getAddressByInterface(@Nonnull String interfaceName, boolean useIpv6) {
		Objects.requireNonNull(interfaceName, "Interface name must not be null");
		Predicate<InetAddress> predicate = useIpv6 ? Inet6Address.class::isInstance : Inet4Address.class::isInstance;
		try {
			NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
			if (networkInterface == null) {
				throw new IllegalArgumentException(String.format("(%s) interface is not valid", interfaceName));
			}
			List<InetAddress> addresses = new ArrayList<>(Collections.list(networkInterface.getInetAddresses()));
			if (addresses.isEmpty()) {
				throw new IllegalArgumentException(
						String.format("Could not find IPv4 Or IPv6 address for (%s) interface", interfaceName));
			}
			for (InetAddress address : addresses) {
				if (predicate.test(address)) {
					return address;
				}
			}
			return addresses.get(0);
		}
		catch (SocketException ex) {
			throw new IllegalArgumentException(ex);
		}
	}


	/**
	 * Return the {@code localhost} address.
	 *
	 * @return the localhost
	 */
	@Nonnull
	public static InetAddress getLocalhost() {
		try {
			return InetAddress.getByName("localhost");
		}
		catch (UnknownHostException ex) {
			return InetAddress.getLoopbackAddress();
		}
	}

}
