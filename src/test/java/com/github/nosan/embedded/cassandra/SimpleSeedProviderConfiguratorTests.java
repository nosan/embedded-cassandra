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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SimpleSeedProviderConfigurator}.
 *
 * @author Dmytro Nosan
 */
class SimpleSeedProviderConfiguratorTests {

	private final SimpleSeedProviderConfigurator configurator = new SimpleSeedProviderConfigurator("localhost");

	private final CassandraBuilder builder = new CassandraBuilder();

	@Test
	void seeds() throws Exception {
		this.configurator.seeds("127.0.0.1").configure(this.builder);
		hasSeeds("127.0.0.1");
	}

	@Test
	void addSeeds() throws Exception {
		this.configurator.addSeeds("127.0.0.1").configure(this.builder);
		hasSeeds("localhost", "127.0.0.1");
	}

	@Test
	void addSeed() throws Exception {
		this.configurator.addSeed("127.0.0.1", 8080).addSeed("::1", 8080)
				.addSeeds("::1").addSeed("[::1]", 9000).configure(this.builder);
		hasSeeds("localhost", "127.0.0.1:8080", "[::1]:8080", "::1", "[::1]:9000");
	}

	@Test
	void noSeeds() {
		assertThatThrownBy(() -> this.configurator.seeds().configure(this.builder)).hasMessage("No seeds!");
	}

	@Test
	void invalidPort() {
		assertThatThrownBy(() -> this.configurator.addSeed("localhost", -1)).hasMessage("Port out of range: -1");
	}

	@SuppressWarnings("unchecked")
	private void hasSeeds(String... seeds) throws Exception {
		Try<Object> configProperties = ReflectionUtils.tryToReadFieldValue(CassandraBuilder.class, "configProperties",
				this.builder);
		Map<String, Object> properties = (Map<String, Object>) configProperties.get();
		List<Map<String, Object>> seedProvider = getSeedProvider(properties);
		assertThat(seedProvider).hasSize(1);
		List<Map<String, Object>> parameters = getParameters(seedProvider.get(0));
		assertThat(parameters).hasSize(1);
		List<String> actualSeeds = getSeeds(parameters.get(0));
		assertThat(actualSeeds).containsExactly(seeds);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> getSeedProvider(Map<String, Object> configProperties) {
		List<Map<String, Object>> seedProvider = (List<Map<String, Object>>) configProperties.get("seed_provider");
		if (seedProvider == null) {
			return Collections.emptyList();
		}
		return seedProvider;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> getParameters(Map<String, Object> seedProvider) {
		List<Map<String, Object>> parameters = (List<Map<String, Object>>) seedProvider.get("parameters");
		if (parameters == null) {
			return Collections.emptyList();
		}
		return parameters;
	}

	private static List<String> getSeeds(Map<String, Object> parameter) {
		return Optional.ofNullable(parameter)
				.map(p -> p.get("seeds"))
				.map(String::valueOf)
				.map(seeds -> Arrays.stream(seeds.split(",")).collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

}
