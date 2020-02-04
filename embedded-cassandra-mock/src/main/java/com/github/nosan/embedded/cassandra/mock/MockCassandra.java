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

package com.github.nosan.embedded.cassandra.mock;

import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.CassandraException;
import com.github.nosan.embedded.cassandra.api.Version;

/**
 * Mock {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class MockCassandra implements Cassandra {

	/**
	 * Mock Cassandra instance.
	 *
	 * @since 3.0.0
	 */
	public static final MockCassandra INSTANCE = new MockCassandra();

	private static final Version VERSION = Version.of("0.0.0-mock");

	private MockCassandra() {
	}

	@Override
	public void start() throws CassandraException {

	}

	@Override
	public void stop() throws CassandraException {

	}

	@Override
	public String getName() {
		return "Mock Cassandra";
	}

	@Override
	public Version getVersion() {
		return VERSION;
	}

	@Override
	public String toString() {
		return getName();
	}

}
