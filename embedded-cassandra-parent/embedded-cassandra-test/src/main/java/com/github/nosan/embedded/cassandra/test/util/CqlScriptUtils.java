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

import java.util.Arrays;
import java.util.Objects;

import com.datastax.driver.core.Session;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Utility class for dealing with {@link CqlScript}.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
@API(since = "1.0.0", status = API.Status.MAINTAINED)
public abstract class CqlScriptUtils {

	private static final Logger log = LoggerFactory.getLogger(CqlScriptUtils.class);

	/**
	 * Executes the given scripts.
	 *
	 * @param cqlScripts the CQL scripts to execute.
	 * @param session a session
	 * @see CqlScript
	 */
	public static void executeScripts(Session session, CqlScript... cqlScripts) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(cqlScripts, "CQL Scripts must not be null");
		if (log.isDebugEnabled()) {
			log.debug("Executing CQL Scripts: '{}'", Arrays.toString(cqlScripts));
		}
		for (CqlScript cqlScript : cqlScripts) {
			for (String statement : cqlScript.getStatements()) {
				CqlUtils.executeStatement(session, statement);
			}
		}
	}

}
