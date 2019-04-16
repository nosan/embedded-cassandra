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

package com.github.nosan.embedded.cassandra;

import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * Exceptions thrown by Cassandra.
 *
 * @author Dmytro Nosan
 * @since 1.0.0
 */
public class CassandraException extends RuntimeException {

	/**
	 * Creates a new exception.
	 *
	 * @param message exception message
	 */
	public CassandraException(@Nullable String message) {
		super(message);
	}

	/**
	 * Creates a new exception.
	 *
	 * @param cause cause exception
	 */
	public CassandraException(@Nullable Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new exception.
	 *
	 * @param cause cause exception
	 * @param message exception message
	 */
	public CassandraException(@Nullable String message, @Nullable Throwable cause) {
		super(message, cause);
	}

}
