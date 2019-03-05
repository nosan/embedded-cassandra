/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.testng;

import org.testng.annotations.Test;

import com.github.nosan.embedded.cassandra.cql.ClassPathCqlScript;
import com.github.nosan.embedded.cassandra.test.testng.CassandraTestNG;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CassandraTestNG}.
 *
 * @author Dmytro Nosan
 */
public class CassandraTestNGTests extends CassandraTestNG {

	public CassandraTestNGTests() {
		super(new ClassPathCqlScript("init.cql"));
	}

	@Test
	public void selectRoles() {
		assertThat(executeStatement("SELECT * FROM  test.roles").all())
				.isEmpty();
	}

}
