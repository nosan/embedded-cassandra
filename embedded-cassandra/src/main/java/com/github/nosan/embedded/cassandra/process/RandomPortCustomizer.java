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
import java.util.Arrays;
import java.util.Iterator;

import de.flapdoodle.embed.process.runtime.Network;

import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * {@link ContextCustomizer Customizer} to replace '0' ports with the random one.
 *
 * @author Dmytro Nosan
 */
class RandomPortCustomizer implements ContextCustomizer {

	@Override
	public void customize(Context context) {
		ExecutableConfig executableConfig = context.getExecutableConfig();
		Config config = executableConfig.getConfig();
		Iterator<Integer> ports = ports();
		if (config.getSslStoragePort() == 0) {
			config.setSslStoragePort(ports.next());
		}
		if (config.getStoragePort() == 0) {
			config.setStoragePort(ports.next());
		}
		if (config.getNativeTransportPort() == 0) {
			config.setNativeTransportPort(ports.next());
		}
		if (config.getRpcPort() == 0) {
			config.setRpcPort(ports.next());
		}
		if (config.getJmxPort() == 0) {
			config.setJmxPort(ports.next());
		}
		if (config.getNativeTransportPortSsl() != null
				&& config.getNativeTransportPortSsl() == 0) {
			config.setNativeTransportPortSsl(ports.next());
		}
	}

	private static Iterator<Integer> ports() {
		try {
			int[] ports = Network.getFreeServerPorts(Network.getLocalHost(), 6);
			return Arrays.stream(ports).iterator();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
