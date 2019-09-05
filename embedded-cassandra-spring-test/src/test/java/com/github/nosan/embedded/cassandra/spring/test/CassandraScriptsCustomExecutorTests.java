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

package com.github.nosan.embedded.cassandra.spring.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.commons.io.SpringResource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.mock.MockCassandraFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraScripts}.
 *
 * @author Dmytro Nosan
 */
@EmbeddedCassandra
@CassandraScripts("schema.cql")
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = {MockCassandraFactory.class,
		CassandraScriptsCustomExecutorTests.CustomCassandraScriptsExecutor.class})
class CassandraScriptsCustomExecutorTests {

	@Test
	void testCqlScriptsCustomExecutor(@Autowired CustomCassandraScriptsExecutor executor) {
		assertThat(executor.scripts).containsExactly(
				CqlScript.ofResource(new SpringResource(new ClassPathResource("schema.cql"))));
	}

	static final class CustomCassandraScriptsExecutor implements CassandraScriptsExecutor {

		private final List<CqlScript> scripts = new ArrayList<>();

		@Override
		public void execute(Cassandra cassandra, List<? extends CqlScript> scripts) {
			this.scripts.addAll(scripts);
		}

	}

}
