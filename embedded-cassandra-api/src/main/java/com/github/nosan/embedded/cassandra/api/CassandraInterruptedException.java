/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.api;

/**
 * Thrown when {@link Cassandra} is interrupted.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public class CassandraInterruptedException extends CassandraException {

	/**
	 * Constructs a new {@link CassandraInterruptedException} with the specified message.
	 *
	 * @param message the detail message
	 */
	public CassandraInterruptedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link CassandraInterruptedException} with the specified message and cause.
	 *
	 * @param message the detail message
	 * @param cause cause exception
	 */
	public CassandraInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

}
