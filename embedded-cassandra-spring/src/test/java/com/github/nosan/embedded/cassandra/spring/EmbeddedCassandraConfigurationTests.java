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

package com.github.nosan.embedded.cassandra.spring;

import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandraConfiguration}.
 *
 * @author Dmytro Nosan
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@EmbeddedCassandra("init.cql")
public class EmbeddedCassandraConfigurationTests {

	@Autowired
	private TestService testService;

	@Rule
	public ExpectedException throwable = ExpectedException.none();

	@Test
	public void createKeyspace() {
		this.throwable.expect(AlreadyExistsException.class);
		this.throwable.expectMessage("Keyspace test already exists");
		assertThat(this.testService.createKeyspace("test")).isFalse();
	}

	@Configuration
	@Import(TestService.class)
	static class Context {

	}

}
