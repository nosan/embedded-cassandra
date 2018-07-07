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

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RandomPortCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class RandomPortCustomizerTests {

	private final RandomPortCustomizer customizer = new RandomPortCustomizer();

	@Test
	public void customize() {
		Config config = new Config();
		assertThat(config.getNativeTransportPort()).isZero();
		assertThat(config.getRpcPort()).isZero();
		assertThat(config.getStoragePort()).isZero();
		assertThat(config.getSslStoragePort()).isZero();
		assertThat(config.getJmxPort()).isZero();
		assertThat(config.getNativeTransportPortSsl()).isNull();

		this.customizer.customize(new TestContext().withConfig(config));

		assertThat(config.getNativeTransportPort()).isNotZero();
		assertThat(config.getRpcPort()).isNotZero();
		assertThat(config.getStoragePort()).isNotZero();
		assertThat(config.getSslStoragePort()).isNotZero();
		assertThat(config.getJmxPort()).isNotZero();
		assertThat(config.getNativeTransportPortSsl()).isNull();

	}

}
