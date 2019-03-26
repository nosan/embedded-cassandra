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

package com.github.nosan.embedded.cassandra.test;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactoryBuilder;

/**
 * Tests for {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 */
class TestCassandra_V_3_11_X_Tests extends AbstractTestCassandraTests {

	TestCassandra_V_3_11_X_Tests() {
		super(new TestCassandra(new LocalCassandraFactoryBuilder()
				.setDeleteWorkingDirectory(true)
				.setVersion(new Version(3, 11, 4))
				.setConfigurationFile(TestCassandra_V_3_11_X_Tests.class.getResource("/cassandra.yaml"))
				.setJvmOptions("-Dcassandra.superuser_setup_delay_ms=1850")
				.build()));
	}

}
