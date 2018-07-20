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
 * Tests for {@link ConfigFileCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class ConfigFileCustomizerTests extends AbstractFileCustomizerTests {

	private final ConfigFileCustomizer customizer = new ConfigFileCustomizer();

	@Test
	public void customize() throws Exception {
		Config config = new Config();
		config.setNativeTransportPort(9042);
		config.setStoragePort(7000);
		config.setSslStoragePort(7001);
		config.setRpcPort(9160);
		withFile("cassandra.yaml").accept((file) -> {
			this.customizer.customize(file, new TestContext().withConfig(config));
			assertThat(file)
					.hasSameContentAs(classpath("customizers/config/cassandra.yaml"));
		});

	}

}
