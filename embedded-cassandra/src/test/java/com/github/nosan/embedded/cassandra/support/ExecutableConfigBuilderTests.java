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

package com.github.nosan.embedded.cassandra.support;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;
import com.github.nosan.embedded.cassandra.JvmOptions;
import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ExecutableConfigBuilder}.
 *
 * @author Dmytro Nosan
 */
public class ExecutableConfigBuilderTests {

	@Test
	public void buildDefaults() {
		ExecutableConfig executableConfig = new ExecutableConfigBuilder().build();
		assertThat(executableConfig.getConfig()).isNotNull();
		assertThat(executableConfig.getTimeout()).isEqualTo(Duration.ofMinutes(1));
		assertThat(executableConfig.getVersion()).isEqualTo(Version.LATEST);

	}

	@Test
	public void build() throws URISyntaxException, MalformedURLException {
		Config config = new Config();
		JvmOptions jvmOptions = new JvmOptions();
		URL logback = new URI("http://localhost:8080").toURL();
		ExecutableConfig executableConfig = new ExecutableConfigBuilder()
				.version(Version.LATEST).timeout(Duration.ofMinutes(15)).config(config)
				.jvmOptions(jvmOptions)
				.logback(logback)
				.build();
		assertThat(executableConfig.getConfig()).isEqualTo(config);
		assertThat(executableConfig.getTimeout()).isEqualTo(Duration.ofMinutes(15));
		assertThat(executableConfig.getVersion()).isEqualTo(Version.LATEST);
		assertThat(executableConfig.getLogback()).isEqualTo(logback);
		assertThat(executableConfig.getJvmOptions()).isEqualTo(jvmOptions);
	}

}
