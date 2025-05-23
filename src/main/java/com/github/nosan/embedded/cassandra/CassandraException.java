/*
 * Copyright 2020-2025 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

/**
 * An exception thrown by a {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class CassandraException extends RuntimeException {

	/**
	 * Constructs a new {@link CassandraException} with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause the cause of the exception
	 */
	public CassandraException(String message, Throwable cause) {
		super(message, cause);
	}

}
