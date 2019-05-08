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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JvmOptions}.
 *
 * @author Dmytro Nosan
 */
class JvmOptionsTests {

	@Test
	void randomizePorts() {
		JvmOptions jvmOptions = new JvmOptions(Collections.emptyList(), new Ports(0, 0, 0, 0, 0),
				new RandomPortSupplier(InetAddress::getLoopbackAddress));
		assertThat(toString(jvmOptions)).doesNotContain("=0");
		assertThat(toString(jvmOptions)).isNotEqualTo(toString(jvmOptions));
	}

	@Test
	void parseJvmOptions() {
		List<String> options = new ArrayList<>();
		options.add("-Dcassandra.jmx.local.port=0");
		options.add("-Dcassandra.native_transport_port=0");
		options.add("-Dcassandra.rpc_port=0");
		options.add("-Dcassandra.storage_port=0");
		options.add("-Dcassandra.ssl_storage_port=0");
		options.add("-Dcassandra.jmx.remote.port=0");
		options.add("-Dcassandra.start_rpc=true");
		options.add("-Dcassandra.start_native_transport=true");
		options.add("-X512m");
		JvmOptions jvmOptions = new JvmOptions(options, new Ports(null, null, null, null, null),
				new RandomPortSupplier(InetAddress::getLoopbackAddress));
		assertThat(toString(jvmOptions)).matches(
				"-Dcassandra.jmx.local.port=\\d{4,5} -Dcassandra.native_transport_port=\\d{4,5}"
						+ " -Dcassandra.rpc_port=\\d{4,5}"
						+ " -Dcassandra.storage_port=\\d{4,5} -Dcassandra.ssl_storage_port=\\d{4,5}"
						+ " -Dcassandra.jmx.remote.port=\\d{4,5} -Dcassandra.start_rpc=true"
						+ " -Dcassandra.start_native_transport=true -X512m");
	}

	private String toString(JvmOptions jvmOptions) {
		return String.join(" ", jvmOptions.get());
	}

}
