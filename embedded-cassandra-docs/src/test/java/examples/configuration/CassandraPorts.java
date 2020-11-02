/*
 * Copyright 2018-2020 the original author or authors.
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

package examples.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;

class CassandraPorts {

	void source() {
		// tag::source[]
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		//set random ports
		//if start_native_transport = true
		cassandraFactory.setPort(0);
		//if client encryption options enabled
		cassandraFactory.setSslPort(0);
		//set cassandra.jmx.local.port
		cassandraFactory.setJmxLocalPort(0);
		//if rpc_start = true
		cassandraFactory.setRpcPort(0);
		cassandraFactory.setStoragePort(0);
		//if server encryption options enabled
		cassandraFactory.setSslStoragePort(0);
		//only for Cassandra version >=4
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("class_name", "org.apache.cassandra.locator.SimpleSeedProvider");
		result.put("parameters", Collections.singletonList(Collections.singletonMap("seeds",
				//localhost == listen_address
				//0 means that set storage_port
				"localhost:0")));
		cassandraFactory.getConfigProperties().put("seed_provider", Collections.singletonList(result));

		// end::source[]
	}

}
