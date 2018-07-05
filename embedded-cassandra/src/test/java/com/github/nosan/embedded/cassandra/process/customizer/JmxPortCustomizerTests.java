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

package com.github.nosan.embedded.cassandra.process.customizer;

import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.ExecutableVersion;
import com.github.nosan.embedded.cassandra.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JmxPortCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class JmxPortCustomizerTests {

	private final JmxPortCustomizer customizer = new JmxPortCustomizer(9000);

	@Test
	public void customizeInvalid() throws Exception {
		Distribution distribution = new Distribution(
				new ExecutableVersion(Version.LATEST), Platform.Linux, BitSize.detect());
		FileCustomizerUtils.withFile("cassandra.sh")
				.from(FileCustomizerUtils.classpath("env.sh")).accept((file) -> {
					JmxPortCustomizerTests.this.customizer.customize(file, distribution);
					assertThat(file)
							.hasSameContentAs(FileCustomizerUtils.classpath("env.sh"));
				});
	}

	@Test
	public void customizeSh() throws Exception {
		Distribution distribution = new Distribution(
				new ExecutableVersion(Version.LATEST), Platform.Linux, BitSize.detect());
		FileCustomizerUtils.withFile("cassandra-env.sh")
				.from(FileCustomizerUtils.classpath("env.sh")).accept((file) -> {
					JmxPortCustomizerTests.this.customizer.customize(file, distribution);
					assertThat(file).hasSameContentAs(
							FileCustomizerUtils.classpath("customizers/jmx_port/env.sh"));
				});
	}

	@Test
	public void customizePs1() throws Exception {
		Distribution distribution = new Distribution(
				new ExecutableVersion(Version.LATEST), Platform.Windows,
				BitSize.detect());
		FileCustomizerUtils.withFile("cassandra-env.ps1")
				.from(FileCustomizerUtils.classpath("env.ps1")).accept((file) -> {
					JmxPortCustomizerTests.this.customizer.customize(file, distribution);
					assertThat(file).hasSameContentAs(FileCustomizerUtils
							.classpath("customizers/jmx_port/env.ps1"));
				});
	}

}
