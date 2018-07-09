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
import com.github.nosan.embedded.cassandra.JvmOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JVMOptionsFileCustomizer}.
 *
 * @author Dmytro Nosan
 */
public class JVMOptionsFileCustomizerTests extends AbstractFileCustomizerSupport {

	private final TestContext context = new TestContext();

	private final JVMOptionsFileCustomizer customizer = new JVMOptionsFileCustomizer();

	@Test
	public void customizeAppend() throws Exception {
		Config config = new Config();
		config.setJvmOptions(new JvmOptions(JvmOptions.Mode.ADD,
				"-Dcassandra.local.port=555", "-Dcassandra.remote.port=444"));
		withFile("jvm.options").from(classpath("jvm.options")).accept((file) -> {
			this.customizer.process(file, this.context.withConfig(config));
			assertThat(file).hasSameContentAs(
					classpath("customizers/jvm_options/jvm_append.options"));
		});
	}

	@Test
	public void customizeOverride() throws Exception {
		Config config = new Config();
		config.setJvmOptions(new JvmOptions(JvmOptions.Mode.REPLACE,
				"-Dcassandra.local.port=555", "-Dcassandra.remote.port=444"));
		withFile("jvm.options").from(classpath("jvm.options")).accept((file) -> {
			this.customizer.process(file, this.context.withConfig(config));
			assertThat(file).hasSameContentAs(
					classpath("customizers/jvm_options/jvm_replace.options"));
		});
	}

}
