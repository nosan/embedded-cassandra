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

package com.github.nosan.embedded.cassandra.test.util;

import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;

import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Utility class for dealing with {@link CqlSession}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public abstract class CqlSessionUtils {

	/**
	 * Executes the given scripts.
	 *
	 * @param scripts the CQL scripts to execute.
	 * @param session a session
	 * @see CqlScript
	 */
	public static void execute(CqlSession session, CqlScript... scripts) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(scripts, "Scripts must not be null");
		CqlUtils.execute(scripts, session::execute);
	}

}
