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

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.nosan.embedded.cassandra.cql.ClassPathCqlResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EmbeddedCassandra}.
 *
 * @author Dmytro Nosan
 */
public class EmbeddedCassandraTests {

	private final EmbeddedCassandra embeddedCassandra = new EmbeddedCassandra();

	@Before
	public void setUp() throws Exception {
		this.embeddedCassandra.start();
	}

	@After
	public void tearDown() throws Exception {
		this.embeddedCassandra.stop();

	}

	@Test
	public void select() throws IOException {
		this.embeddedCassandra.executeScripts(new ClassPathCqlResource("init.cql"));
		assertThat(this.embeddedCassandra.getSession().execute("SELECT * FROM  test.roles").wasApplied())
				.isTrue();
	}
}
