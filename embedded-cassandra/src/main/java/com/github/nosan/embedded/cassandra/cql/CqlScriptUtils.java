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

package com.github.nosan.embedded.cassandra.cql;

import java.util.Arrays;
import java.util.Objects;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for working with {@link CqlScript Scripts}.
 *
 * @author Dmytro Nosan
 */
public abstract class CqlScriptUtils {

	private static final Logger log = LoggerFactory.getLogger(CqlScriptUtils.class);

	/**
	 * Executes the provided scripts.
	 *
	 * @param cqlScripts the CQL resources to execute.
	 * @param session Cassandra's Session to use.
	 * @see CqlScript
	 */
	public static void executeScripts(Session session, CqlScript... cqlScripts) {
		Objects.requireNonNull(session, "Session must not be null");
		Objects.requireNonNull(cqlScripts, "CQL Scripts must not be null");
		log.debug("Executing CQL Scripts: {}", Arrays.toString(cqlScripts));
		for (CqlScript cqlScript : cqlScripts) {
			for (String statement : cqlScript.getStatements()) {
				log.debug("Execute Statement:  {}", statement);
				session.execute(statement);
			}
		}
	}
}
