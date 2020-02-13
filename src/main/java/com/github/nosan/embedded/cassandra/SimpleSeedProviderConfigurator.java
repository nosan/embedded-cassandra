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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.nosan.embedded.cassandra.commons.StringUtils;

/**
 * A {@link CassandraBuilderConfigurator} which configures org.apache.cassandra.locator.SimpleSeedProvider.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public final class SimpleSeedProviderConfigurator implements CassandraBuilderConfigurator {

	private static final String SIMPLE_SEED_PROVIDER_CLASS = "org.apache.cassandra.locator.SimpleSeedProvider";

	private final List<Object> seeds = new ArrayList<>();

	/**
	 * Creates {@link SimpleSeedProviderConfigurator} with no seeds.
	 */
	public SimpleSeedProviderConfigurator() {
	}

	/**
	 * Creates {@link SimpleSeedProviderConfigurator} with provided seeds.
	 *
	 * @param seeds list of addresses.
	 */
	public SimpleSeedProviderConfigurator(InetSocketAddress... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.addAll(Arrays.asList(seeds));
	}

	/**
	 * Creates {@link SimpleSeedProviderConfigurator} with provided seeds.
	 *
	 * @param seeds list of addresses.
	 */
	public SimpleSeedProviderConfigurator(InetAddress... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.addAll(Arrays.asList(seeds));
	}

	/**
	 * Creates {@link SimpleSeedProviderConfigurator} with provided seeds.
	 *
	 * @param seeds list of addresses.
	 */
	public SimpleSeedProviderConfigurator(String... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.addAll(Arrays.asList(seeds));
	}

	/**
	 * Sets the seeds. Setting this value will replace any previously configured seeds.
	 *
	 * @param seeds list of addresses.
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator seeds(InetSocketAddress... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.clear();
		this.seeds.addAll(Arrays.asList(seeds));
		return this;
	}

	/**
	 * Sets the seeds. Setting this value will replace any previously configured seeds.
	 *
	 * @param seeds list of addresses.
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator seeds(InetAddress... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.clear();
		this.seeds.addAll(Arrays.asList(seeds));
		return this;
	}

	/**
	 * Sets the seeds. Setting this value will replace any previously configured seeds.
	 *
	 * @param seeds list of addresses.
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator seeds(String... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.clear();
		this.seeds.addAll(Arrays.asList(seeds));
		return this;
	}

	/**
	 * Adds the seeds.
	 *
	 * @param seeds list of addresses to add
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator addSeeds(InetSocketAddress... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.addAll(Arrays.asList(seeds));
		return this;
	}

	/**
	 * Adds the seeds.
	 *
	 * @param seeds list of addresses to add
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator addSeeds(InetAddress... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.addAll(Arrays.asList(seeds));
		return this;
	}

	/**
	 * Adds the seeds.
	 *
	 * @param seeds list of addresses to add
	 * @return this configurator
	 */
	public SimpleSeedProviderConfigurator addSeeds(String... seeds) {
		Objects.requireNonNull(seeds, "Seeds must not be null");
		this.seeds.addAll(Arrays.asList(seeds));
		return this;
	}

	@Override
	public CassandraBuilder configure(CassandraBuilder cassandraBuilder) {
		Objects.requireNonNull(cassandraBuilder, "Cassandra Builder must not be null");
		String seeds = buildSeeds();
		if (!StringUtils.hasText(seeds)) {
			throw new IllegalArgumentException("Seeds must not be empty.");
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("class_name", SIMPLE_SEED_PROVIDER_CLASS);
		result.put("parameters", Collections.singletonList(Collections.singletonMap("seeds", seeds)));
		return cassandraBuilder.configProperty("seed_provider", Collections.singletonList(result));
	}

	private String buildSeeds() {
		Set<String> seeds = new LinkedHashSet<>();
		for (Object seed : this.seeds) {
			if (seed instanceof InetAddress) {
				seeds.add(((InetAddress) seed).getHostAddress());
			}
			else if (seed instanceof InetSocketAddress) {
				InetSocketAddress address = (InetSocketAddress) seed;
				String host = address.isUnresolved() ? address.getHostName() : address.getAddress().getHostAddress();
				int port = address.getPort();
				if (host.contains(":")) {
					seeds.add(String.format("[%s]:%d", host, port));
				}
				else {
					seeds.add(String.format("%s:%d", host, port));
				}
			}
			else if (seed instanceof String) {
				seeds.add(((String) seed));
			}
		}
		return String.join(",", seeds);
	}

}
