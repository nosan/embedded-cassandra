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

package examples.junit5.configuration.customizer;
// tag::source[]

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.junit5.test.CassandraExtension;

class CassandraTests {

	@RegisterExtension
	static final CassandraExtension extension = new CassandraExtension(
			cassandraFactory -> cassandraFactory.setPort(9042));

	@Test
	void test() {
		Cassandra cassandra = extension.getCassandra();
		//cassandra.getPort() == 9042
	}

}

// end::source[]

