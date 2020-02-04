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

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

class CassandraSsl {

	void source() throws Exception {
		// tag::source[]
		EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
		Path keystore = new ClassPathResource("keystore.node0").toPath();
		Path truststore = new ClassPathResource("truststore.node0").toPath();
		Map<String, Object> sslOptions = new LinkedHashMap<>();
		sslOptions.put("enabled", true);
		sslOptions.put("require_client_auth", true);
		sslOptions.put("optional", false);
		sslOptions.put("keystore", keystore.toString());
		sslOptions.put("keystore_password", "cassandra");
		sslOptions.put("truststore", truststore.toString());
		sslOptions.put("truststore_password", "cassandra");
		cassandraFactory.getConfigProperties().put("client_encryption_options", sslOptions);
		// end::source[]
	}

}
