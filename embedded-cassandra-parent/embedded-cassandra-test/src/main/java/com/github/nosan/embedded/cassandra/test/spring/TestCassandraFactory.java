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

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.TestCassandra;

/**
 * Factory that creates {@link TestCassandra}. This bean will be used for creating {@link TestCassandra} before {@link
 * TestCassandra} itself is started.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 * @deprecated since 2.0.4 with no replacement
 */
@FunctionalInterface
@Deprecated
public interface TestCassandraFactory {

	/**
	 * Creates {@link TestCassandra}.
	 *
	 * @param cassandraFactory factory that creates {@link Cassandra}
	 * @param scripts the {@link CqlScript scripts} to execute
	 * @return the {@link TestCassandra}
	 */
	TestCassandra create(CassandraFactory cassandraFactory, CqlScript... scripts);

}
