/*
 * Copyright 2020 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.nosan.embedded.cassandra.commons.StringUtils;

/**
 * A {@link CassandraBuilderConfigurator} that configures org.apache.cassandra.locator.SimpleSeedProvider.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class SimpleSeedProviderConfigurator implements CassandraBuilderConfigurator {

	private static final String SIMPLE_SEED_PROVIDER_CLASS = "org.apache.cassandra.locator.SimpleSeedProvider";

	private final Set<String> addresses = new LinkedHashSet<>();

	/**
	 * Creates {@link SimpleSeedProviderConfigurator} with provided addresses.
	 *
	 * @param addresses list of addresses.
	 */
	public SimpleSeedProviderConfigurator(String... addresses) {
		checkAddress(addresses);
		this.addresses.addAll(Arrays.asList(addresses));
	}

	/**
	 * Sets the addresses. Setting this value will replace any previously configured addresses.
	 *
	 * @param addresses list of addresses.
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator seeds(String... addresses) {
		checkAddress(addresses);
		this.addresses.clear();
		this.addresses.addAll(Arrays.asList(addresses));
		return this;
	}

	/**
	 * Adds the addresses.
	 *
	 * @param addresses list of addresses to add
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator addSeeds(String... addresses) {
		checkAddress(addresses);
		this.addresses.addAll(Arrays.asList(addresses));
		return this;
	}

	/**
	 * Adds the address with a port.
	 *
	 * @param address the host name
	 * @param port The port number
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator addSeed(String address, int port) {
		checkAddress(address);
		checkPort(port);
		return addSeeds(getAddress(address, port));
	}

	@Override
	public void configure(CassandraBuilder builder) {
		Objects.requireNonNull(builder, "Cassandra Builder must not be null");
		if (this.addresses.isEmpty()) {
			throw new IllegalArgumentException("No seeds!");

		}
		String seeds = String.join(",", this.addresses);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("class_name", SIMPLE_SEED_PROVIDER_CLASS);
		result.put("parameters", Collections.singletonList(Collections.singletonMap("seeds", seeds)));
		builder.addConfigProperty("seed_provider", Collections.singletonList(result));
	}

	private static void checkAddress(String... addresses) {
		Objects.requireNonNull(addresses, "Addresses must not be null");
		for (String address : addresses) {
			if (!StringUtils.hasText(address)) {
				throw new IllegalArgumentException("Address must not be null or empty");
			}
		}
	}

	private static void checkPort(int port) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port out of range: " + port);
		}
	}

	private static String getAddress(String address, int port) {
		StringBuilder r = new StringBuilder(address);
		if (address.contains(":")) {
			if (r.charAt(0) != '[') {
				r.insert(0, '[');
			}
			if (r.charAt(r.length() - 1) != ']') {
				r.append(']');
			}
		}
		return r.append(':').append(port).toString();
	}

}
