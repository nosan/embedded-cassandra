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

package com.github.nosan.embedded.cassandra.test.spring;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CqlExecutionListener}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("NullableProblems")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CqlSessionConfiguration.class)
@EmbeddedCassandra(scripts = "/init.cql")
@Cql(statements = "TRUNCATE test.users", executionPhase = Cql.ExecutionPhase.AFTER_TEST_METHOD)
class CqlExecutionListenerCqlSessionTests {

	@Autowired
	private CqlSession session;

	@Test
	@Cql(scripts = "/users-data.cql")
	void shouldHaveUser() {
		assertThat(getCount()).isEqualTo(1);
	}

	@Test
	void shouldNotHaveUser() {
		assertThat(getCount()).isZero();
	}

	private long getCount() {
		Row resultSet = this.session.execute("SELECT COUNT(*) FROM test.users").one();
		assertThat(resultSet).isNotNull();
		return resultSet.getLong(0);
	}

}
