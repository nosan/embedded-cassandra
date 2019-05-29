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

/**
 * Strategy interface for customizing {@link CassandraFactory }. Any beans of this type will get a callback with the
 * {@link CassandraFactory} before the {@link Cassandra} itself is started, so you can set the port, version, etc.
 *
 * @param <T> the configurable {@link CassandraFactory}
 * @author Dmytro Nosan
 * @since 2.0.3
 */
@FunctionalInterface
public interface CassandraFactoryCustomizer<T extends CassandraFactory> {

	/**
	 * Customize the specified {@link CassandraFactory}.
	 *
	 * @param factory {@link CassandraFactory factory} to customize
	 */
	void customize(T factory);

}
