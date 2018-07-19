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

import de.flapdoodle.embed.process.distribution.Platform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Java9CompatibilityFileCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class Java9CompatibilityFileCustomizerTests extends AbstractFileCustomizerTests {


	private final Java9CompatibilityFileCustomizer customizer = new Java9CompatibilityFileCustomizer();

	private final TestContext context = new TestContext();


	@Test
	public void jvmOptions() throws Exception {
		withFile("jvm.options").accept((file) -> {
			this.customizer.customize(file, this.context.withPlatform(Platform.Linux));
			assertThat(file)
					.hasSameContentAs(classpath("customizers/java_compatibility/jvm.options"));
		});
	}

	@Test
	public void customizeSh() throws Exception {
		withFile("cassandra-env.sh").from(classpath("env.sh")).accept((file) -> {
			this.customizer.customize(file, this.context.withPlatform(Platform.Linux));
			assertThat(file)
					.hasSameContentAs(classpath("customizers/java_compatibility/env.sh"));
		});
	}


}
