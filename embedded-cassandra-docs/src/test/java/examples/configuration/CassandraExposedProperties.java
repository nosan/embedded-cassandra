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

package examples.configuration;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;

class CassandraExposedProperties {

	void source() {
		// tag::source[]
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		Cassandra cassandra = cassandraFactory.create();
		cassandra.start();
		try {
			String port = System.getProperty("embedded.cassandra.port");
			String address = System.getProperty("embedded.cassandra.address");
			String version = System.getProperty("embedded.cassandra.version");
			String sslPort = System.getProperty("embedded.cassandra.ssl-port");
			String rpcPort = System.getProperty("embedded.cassandra.rpc-port");
			//...
		}
		finally {
			cassandra.stop();
		}

		// end::source[]
	}

}
