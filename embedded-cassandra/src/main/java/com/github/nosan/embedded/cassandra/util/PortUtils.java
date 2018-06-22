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

package com.github.nosan.embedded.cassandra.util;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.config.Config;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Utility class for working with cassandra ports.
 *
 * @author Dmytro Nosan
 */
public abstract class PortUtils {

	/**
	 * Set up random ports only if the filter result was true. Following properties may be
	 * overriden. {@link Config#setRpcPort(int)},
	 * {@link Config#setNativeTransportPort(int)}, {@link Config#setStoragePort(int)},
	 * {@link Config#setSslStoragePort(int)},
	 * {@link Config#setNativeTransportPortSsl(Integer)}
	 * @param config cassandra config.
	 * @param portFilter port filter.
	 */
	public static void setRandomPorts(Config config, Predicate<Integer> portFilter) {
		Objects.requireNonNull(config, "Config must not be null");
		Objects.requireNonNull(portFilter, "Port Filter must not be null");
		applyRandomPort(config::setRpcPort, config::getRpcPort, portFilter,
				PortUtils::getRandomPort);
		applyRandomPort(config::setNativeTransportPort, config::getNativeTransportPort,
				portFilter, PortUtils::getRandomPort);
		applyRandomPort(config::setStoragePort, config::getStoragePort, portFilter,
				PortUtils::getRandomPort);
		applyRandomPort(config::setSslStoragePort, config::getSslStoragePort, portFilter,
				PortUtils::getRandomPort);
		applyRandomPort(config::setNativeTransportPortSsl,
				config::getNativeTransportPortSsl, portFilter, PortUtils::getRandomPort);
	}

	/**
	 * Trying to find free random port.
	 * @return Free random port.
	 */
	public static int getRandomPort() {
		try {
			return Network.getFreeServerPort();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static void applyRandomPort(Consumer<Integer> setter,
			Supplier<Integer> getter, Predicate<Integer> filter,
			Supplier<Integer> randomPort) {
		if (filter.test(getter.get())) {
			setter.accept(randomPort.get());
		}
	}

}
