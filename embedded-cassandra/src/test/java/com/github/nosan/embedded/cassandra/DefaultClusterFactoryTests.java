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

package com.github.nosan.embedded.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.ProtocolOptions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultClusterFactory}.
 *
 * @author Dmytro Nosan
 */
public class DefaultClusterFactoryTests {

	@Test
	public void defaultCluster() {
		Config config = new Config();
		config.setNativeTransportPort(9042);
		Cluster cluster = new DefaultClusterFactory().getCluster(config, Version.LATEST);
		Configuration configuration = cluster.getConfiguration();
		ProtocolOptions protocolOptions = configuration.getProtocolOptions();
		assertThat(protocolOptions.getPort()).isEqualTo(9042);
	}
}
