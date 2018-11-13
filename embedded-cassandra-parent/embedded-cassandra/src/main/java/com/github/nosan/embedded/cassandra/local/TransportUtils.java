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

package com.github.nosan.embedded.cassandra.local;

import java.net.InetAddress;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.util.PortUtils;

/**
 * Utility class for dealing with {@code cassandra} ports.
 *
 * @author Dmytro Nosan
 * @since 1.1.0
 */
abstract class TransportUtils {

	/**
	 * Test whether {@code cassandra} transport is ready or not.
	 *
	 * @param settings a settings
	 * @return {@code true} if ready otherwise {@code false}
	 */
	static boolean isReady(@Nonnull Settings settings) {
		Predicate<Settings> predicate = TransportUtils::isStorageReady;
		if (settings.isStartNativeTransport()) {
			predicate = predicate.and(TransportUtils::isNativeTransportReady);
		}
		if (settings.isStartRpc()) {
			predicate = predicate.and(TransportUtils::isRpcTransportReady);
		}
		return predicate.test(settings);
	}

	/**
	 * Test whether {@code cassandra} transport is disabled or not.
	 *
	 * @param settings a settings
	 * @return {@code true} if disabled otherwise {@code false}
	 */
	static boolean isDisabled(@Nonnull Settings settings) {
		Predicate<Settings> predicate = TransportUtils::isStorageReady;
		if (settings.isStartNativeTransport()) {
			predicate = predicate.or(TransportUtils::isNativeTransportReady);
		}
		if (settings.isStartRpc()) {
			predicate = predicate.or(TransportUtils::isRpcTransportReady);
		}
		return !predicate.test(settings);
	}


	/**
	 * Verify whether {@code cassandra} transport is busy or not.
	 *
	 * @param settings a settings
	 * @throws IllegalArgumentException if one of the ports is busy
	 */
	static void verifyPorts(@Nonnull Settings settings) {
		if (isStorageReady(settings)) {
			throw new IllegalArgumentException(String.format("storage_port (%s) or ssl_storage_port (%s) is busy",
					settings.getStoragePort(), settings.getSslStoragePort()));
		}
		if (settings.isStartNativeTransport() && isNativeTransportReady(settings)) {
			throw new IllegalArgumentException(
					String.format("native_transport_port (%s) or native_transport_port_ssl (%s) is busy",
							settings.getPort(), settings.getSslPort()));
		}
		if (settings.isStartRpc() && isRpcTransportReady(settings)) {
			throw new IllegalArgumentException(String.format("rpc_port (%s)  is busy", settings.getRpcPort()));
		}

	}

	private static boolean isStorageReady(Settings settings) {
		InetAddress address = settings.getRealListenAddress();
		int storagePort = settings.getStoragePort();
		int sslStoragePort = settings.getSslStoragePort();
		return PortUtils.isPortBusy(address, storagePort) || PortUtils.isPortBusy(address, sslStoragePort);
	}


	private static boolean isNativeTransportReady(Settings settings) {
		InetAddress address = settings.getRealAddress();
		int port = settings.getPort();
		Integer sslPort = settings.getSslPort();
		if (sslPort != null) {
			return PortUtils.isPortBusy(address, port) && PortUtils.isPortBusy(address, sslPort);
		}
		return PortUtils.isPortBusy(address, port);
	}

	private static boolean isRpcTransportReady(Settings settings) {
		InetAddress address = settings.getRealAddress();
		int port = settings.getRpcPort();
		return PortUtils.isPortBusy(address, port);
	}


}
