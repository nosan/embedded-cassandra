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
 * Tests for {@link JavaCompatibilityCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class JavaCompatibilityCustomizerTests {

	private final JavaCompatibilityCustomizer customizer = new JavaCompatibilityCustomizer();

	@Test
	public void javaOptions() throws Exception {
		Distribution distribution = new Distribution(
				new ExecutableVersion(Version.LATEST), Platform.Linux, BitSize.detect());
		FileCustomizerUtils.withFile("jvm.options")
				.from(FileCustomizerUtils.classpath("jvm.options")).accept((file) -> {
					JavaCompatibilityCustomizerTests.this.customizer.customize(file,
							distribution);
					assertThat(file).hasSameContentAs(FileCustomizerUtils
							.classpath("customizers/java_compatibility/jvm.options"));
				});
	}

	@Test
	public void customizeSh() throws Exception {
		Distribution distribution = new Distribution(
				new ExecutableVersion(Version.LATEST), Platform.Linux, BitSize.detect());
		FileCustomizerUtils.withFile("cassandra-env.sh")
				.from(FileCustomizerUtils.classpath("env.sh")).accept((file) -> {
					JavaCompatibilityCustomizerTests.this.customizer.customize(file,
							distribution);
					assertThat(file).hasSameContentAs(FileCustomizerUtils
							.classpath("customizers/java_compatibility/env.sh"));
				});
	}

	@Test
	public void customizePs1() throws Exception {
		Distribution distribution = new Distribution(
				new ExecutableVersion(Version.LATEST), Platform.Windows,
				BitSize.detect());
		FileCustomizerUtils.withFile("cassandra-env.ps1")
				.from(FileCustomizerUtils.classpath("env.ps1")).accept((file) -> {
					JavaCompatibilityCustomizerTests.this.customizer.customize(file,
							distribution);
					assertThat(file).hasSameContentAs(FileCustomizerUtils
							.classpath("customizers/java_compatibility/env.ps1"));
				});
	}

}
