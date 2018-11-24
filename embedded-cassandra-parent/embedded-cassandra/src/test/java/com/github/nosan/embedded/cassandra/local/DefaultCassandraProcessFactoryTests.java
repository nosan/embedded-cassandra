/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultCassandraProcessFactory}.
 *
 * @author Dmytro Nosan
 */
public class DefaultCassandraProcessFactoryTests {


	@Test
	public void shouldCreateProcess() {
		Duration startupTimeout = Duration.ofMillis(1000);
		List<String> jvmOptions = Collections.singletonList("-Xmx512m");
		Version version = Version.parse("3.11.3");
		Path javaHome = Paths.get("java.home");
		Path directory = Paths.get("directory");
		int jmxPort = 7199;
		boolean allowRoot = true;
		DefaultCassandraProcessFactory factory = new DefaultCassandraProcessFactory(startupTimeout,
				jvmOptions, version, javaHome, jmxPort, allowRoot);
		CassandraProcess cassandraProcess = factory.create(directory);
		assertThat(ReflectionUtils.getField(cassandraProcess, "jvmOptions")).isEqualTo(jvmOptions);
		assertThat(ReflectionUtils.getField(cassandraProcess, "javaHome")).isEqualTo(javaHome);
		assertThat(ReflectionUtils.getField(cassandraProcess, "version")).isEqualTo(version);
		assertThat(ReflectionUtils.getField(cassandraProcess, "timeout")).isEqualTo(startupTimeout);
		assertThat(ReflectionUtils.getField(cassandraProcess, "directory")).isEqualTo(directory);
		assertThat(ReflectionUtils.getField(cassandraProcess, "jmxPort")).isEqualTo(jmxPort);
		assertThat(ReflectionUtils.getField(cassandraProcess, "allowRoot")).isEqualTo(allowRoot);

	}
}
