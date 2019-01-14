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

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Builder to create a {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 * @since 1.2.10
 */
@API(since = "1.2.10", status = API.Status.STABLE)
public final class TestCassandraBuilder extends AbstractTestCassandraBuilder<TestCassandra, TestCassandraBuilder> {

	@Nonnull
	@Override
	public TestCassandra build() {
		return new TestCassandra(isRegisterShutdownHook(), getCassandraFactory(), getClusterFactory(), getScripts());
	}
}
