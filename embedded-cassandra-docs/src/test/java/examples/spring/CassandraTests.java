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

package examples.spring;

// tag::source[]

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.spring.test.EmbeddedCassandra;

@EmbeddedCassandra
@ExtendWith(SpringExtension.class)
class CassandraTests {

	@Test
	void testCassandra(@Autowired Cassandra cassandra) {
		//
	}

	@Configuration
	static class TestCassandraConfiguration extends AbstractCassandraConfiguration {

		private final Cassandra cassandra;

		TestCassandraConfiguration(Cassandra cassandra) {
			this.cassandra = cassandra;
		}

		@Override
		protected String getContactPoints() {
			return Optional.ofNullable(this.cassandra.getAddress()).map(InetAddress::getHostAddress).orElse(
					super.getContactPoints());
		}

		@Override
		protected int getPort() {
			return this.cassandra.getPort();
		}

		@Override
		protected String getKeyspaceName() {
			return "test";
		}

		@Override
		protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
			return Collections.singletonList(CreateKeyspaceSpecification.createKeyspace(getKeyspaceName()));
		}

	}

}
// end::source[]
