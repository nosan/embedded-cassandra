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

import de.flapdoodle.embed.process.runtime.NUMA;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NumaFileCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class NumaFileCustomizerTests extends AbstractFileCustomizerSupport {

	private final NumaFileCustomizer customizer = new NumaFileCustomizer();

	@Test
	public void customize() throws Exception {
		TestContext testContext = new TestContext();
		if (!NUMA.isNUMA(testContext.getExecutableConfig().supportConfig(),
				testContext.getDistribution().getPlatform())) {
			withFile("jvm.options").from(classpath("jvm.options")).accept(file -> {
				this.customizer.customize(file, testContext);
				assertThat(file).hasSameContentAs(classpath("customizers/numa/jvm.options"));
			});
		}

	}
}
