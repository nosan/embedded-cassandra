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

package com.github.nosan.embedded.cassandra;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Utility class for working with cassandra ports.
 *
 * @author Dmytro Nosan
 */
public abstract class CassandraPortUtils {

	/**
	 * Set up random ports.
	 * @param config cassandra config
	 * @see #setRandomPorts(CassandraConfig, Predicate)
	 */
	public static void setRandomPorts(CassandraConfig config) {
		setRandomPorts(config, Objects::nonNull);
	}

	/**
	 * Set up random ports only if the filter result was true. Following properties will
	 * be overriden. {@link CassandraConfig#setRpcPort(int)},
	 * {@link CassandraConfig#setNativeTransportPort(int)},
	 * {@link CassandraConfig#setStoragePort(int)},
	 * {@link CassandraConfig#setSslStoragePort(int)},
	 * {@link CassandraConfig#setNativeTransportPortSsl(Integer)}
	 * @param config cassandra config.
	 * @param filter port filter.
	 */
	public static void setRandomPorts(CassandraConfig config, Predicate<Integer> filter) {
		applyRandomPort(config::setRpcPort, config::getRpcPort, filter,
				CassandraPortUtils::getRandomPort);
		applyRandomPort(config::setNativeTransportPort, config::getNativeTransportPort,
				filter, CassandraPortUtils::getRandomPort);
		applyRandomPort(config::setStoragePort, config::getStoragePort, filter,
				CassandraPortUtils::getRandomPort);
		applyRandomPort(config::setSslStoragePort, config::getSslStoragePort, filter,
				CassandraPortUtils::getRandomPort);
		applyRandomPort(config::setNativeTransportPortSsl,
				config::getNativeTransportPortSsl, filter,
				CassandraPortUtils::getRandomPort);
	}

	/**
	 * Trying to find free random port.
	 * @return Free random port.
	 */
	public static Integer getRandomPort() {
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
