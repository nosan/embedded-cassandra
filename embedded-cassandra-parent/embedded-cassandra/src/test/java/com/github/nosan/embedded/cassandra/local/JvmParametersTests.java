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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JvmParameters}.
 *
 * @author Dmytro Nosan
 */
class JvmParametersTests {

	@Test
	void randomizePorts() {
		List<String> options = new ArrayList<>();
		options.add("-Dcassandra.rpc_port=0");
		options.add("-Dcassandra.storage_port=0");
		options.add("-Dcassandra.ssl_storage_port=0");
		options.add("-Dcassandra.jmx.remote.port=0");
		options.add("-Dcom.sun.management.jmxremote.port=0");
		JvmOptions jvmOptions = new JvmOptions(options);
		JvmParameters jvmParameters = new JvmParameters(jvmOptions, new Ports(0, 0, 0, 0, 0),
				new RandomPortSupplier(InetAddress::getLoopbackAddress));
		assertThat(toString(jvmParameters)).doesNotContain("=0");
		assertThat(toString(jvmParameters)).isNotEqualTo(toString(jvmParameters));
	}

	private String toString(JvmParameters jvmParameters) {
		return String.join(" ", jvmParameters.get());
	}

}
