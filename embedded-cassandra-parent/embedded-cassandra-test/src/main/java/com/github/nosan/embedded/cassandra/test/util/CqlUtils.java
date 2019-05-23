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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.cql.CqlScript;

/**
 * Utility class for dealing with {@code CQL}.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public abstract class CqlUtils {

	private static final Logger log = LoggerFactory.getLogger(CqlUtils.class);

	/**
	 * Executes the given scripts.
	 *
	 * @param scripts the CQL scripts to execute.
	 * @param statementCallback a callback
	 * @see CqlScript
	 */
	public static void execute(CqlScript[] scripts, StatementCallback statementCallback) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		Objects.requireNonNull(statementCallback, "Statement Callback must not be null");
		execute(Arrays.asList(scripts), statementCallback);
	}

	/**
	 * Executes the given scripts.
	 *
	 * @param scripts the CQL scripts to execute.
	 * @param statementCallback a callback
	 * @see CqlScript
	 */
	public static void execute(Iterable<? extends CqlScript> scripts, StatementCallback statementCallback) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		Objects.requireNonNull(statementCallback, "Statement Callback must not be null");
		for (CqlScript script : scripts) {
			if (log.isDebugEnabled()) {
				log.debug("Executing Script: {}", script);
			}
			for (String statement : script.getStatements()) {
				if (log.isDebugEnabled()) {
					log.debug("Executing Statement: {}", statement);
				}
				statementCallback.execute(statement);
			}
		}
	}

	/**
	 * CQL statement callback interface.
	 */
	@FunctionalInterface
	public interface StatementCallback {

		/**
		 * Execute the {@code CQL} statement.
		 *
		 * @param statement the CQL statement
		 */
		void execute(String statement);

	}

}
