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

package com.github.nosan.embedded.cassandra.testng;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.nosan.embedded.cassandra.JvmOptions;
import com.github.nosan.embedded.cassandra.cql.ClassPathCqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScriptUtils;
import com.github.nosan.embedded.cassandra.support.ExecutableConfigBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractCassandraTestNG}.
 *
 * @author Dmytro Nosan
 */
public class AbstractCassandraTestNGTests extends AbstractCassandraTestNG {

	public AbstractCassandraTestNGTests() {
		super(new ExecutableConfigBuilder().jvmOptions(new JvmOptions("-Xmx384m", "-Xms384m")).build());
	}

	@BeforeMethod
	public void setUp() {
		CqlScriptUtils.executeScripts(getSession(), new ClassPathCqlScript("init.cql"));
	}

	@Test
	public void select() {
		assertThat(getSession().execute("SELECT * FROM  test.roles").wasApplied())
				.isTrue();
	}

}
