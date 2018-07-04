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

package com.github.nosan.embedded.cassandra.customizer;

import java.util.Arrays;

import de.flapdoodle.embed.process.distribution.Distribution;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.config.Version;

/**
 * Tests for {@link JVMOptionsCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class JVMOptionsCustomizerTests {

	private final JVMOptionsCustomizer customizer = new JVMOptionsCustomizer(
			Arrays.asList("-Dcassandra.local.port=555", "-Dcassandra.remote.port=444"));

	@Test
	public void customize() throws Exception {
		JVMOptionsCustomizer customizer = this.customizer;
		FileCustomizerUtils.withFile("jvm.options")
				.from(FileCustomizerUtils.classpath("jvm.options")).accept((file) -> {
					customizer.process(file, Distribution.detectFor(Version.LATEST));
					Assertions.assertThat(file).hasSameContentAs(FileCustomizerUtils
							.classpath("customizers/jvm_options/jvm.options"));
				});
	}

}
