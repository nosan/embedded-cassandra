/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link JvmOptions}.
 *
 * @author Dmytro Nosan
 */
class JvmOptionsTests {

	@Test
	void parseJvmOptions() {
		JvmOptions jvmOptions = getJvmOptions();
		List<String> options = jvmOptions.getOptions();
		Map<String, String> systemProperties = jvmOptions.getSystemProperties();
		assertThat(systemProperties).containsExactly(entry("-Dcassandra.jmx.remote.port", "0"),
				entry("-Dcassandra.start_rpc", ""), entry("-Dcassandra.start_native_transport", null));
		assertThat(options).containsExactly("-X512m");
	}

	private JvmOptions getJvmOptions() {
		List<String> options = new ArrayList<>();
		options.add("-Dcassandra.jmx.remote.port=0");
		options.add("-Dcassandra.start_rpc=");
		options.add("-Dcassandra.start_native_transport");
		options.add("-X512m");
		return new JvmOptions(options);
	}

}
