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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.github.nosan.embedded.cassandra.config.Config;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PortUtils}.
 *
 * @author Dmytro Nosan
 */
public class PortUtilsTests {

	@Test
	public void apply() {
		Config config = new Config();
		config.setNativeTransportPortSsl(1000);
		PortUtils.setRandomPorts(config, Objects::nonNull);

		Set<Integer> ports = new LinkedHashSet<>();
		ports.add(config.getRpcPort());
		ports.add(config.getNativeTransportPort());
		ports.add(config.getNativeTransportPortSsl());
		ports.add(config.getStoragePort());
		ports.add(config.getSslStoragePort());

		assertThat(ports).hasSize(5);
	}

	@Test
	public void applyNullFilter() {
		Config config = new Config();
		PortUtils.setRandomPorts(config, Objects::nonNull);

		Set<Integer> ports = new LinkedHashSet<>();
		ports.add(config.getRpcPort());
		ports.add(config.getNativeTransportPort());
		ports.add(config.getNativeTransportPortSsl());
		ports.add(config.getStoragePort());
		assertThat(ports).hasSize(4);
		assertThat(config.getNativeTransportPortSsl()).isNull();
	}

}
