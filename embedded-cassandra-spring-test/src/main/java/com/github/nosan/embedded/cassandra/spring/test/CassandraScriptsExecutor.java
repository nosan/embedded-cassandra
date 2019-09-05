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

import java.util.List;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Strategy interface for executing {@link CqlScript CassandraScripts}  against {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
@FunctionalInterface
public interface CassandraScriptsExecutor {

	/**
	 * Executes the given {@link CqlScript CassandraScripts} against {@link Cassandra}.
	 *
	 * @param cassandra {@link Cassandra} instance
	 * @param scripts CQL scripts
	 */
	void execute(Cassandra cassandra, List<? extends CqlScript> scripts);

}
