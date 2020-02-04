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

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;

public class CassandraMoreOneInstance {

	void source() {
		// tag::source[]
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		cassandraFactory.setPort(0);
		cassandraFactory.setSslPort(0); // if SSL client options enabled
		cassandraFactory.setRpcPort(0);
		cassandraFactory.setJmxLocalPort(0);
		cassandraFactory.setStoragePort(0);
		cassandraFactory.setSslStoragePort(0); // // if SSL server options enabled

		Cassandra cassandra1 = cassandraFactory.create();
		Cassandra cassandra2 = cassandraFactory.create();

		cassandra1.start();
		cassandra2.start();

		try {
			//...
		}
		finally {
			cassandra1.stop();
			cassandra2.stop();
		}
		// end::source[]
	}

}
