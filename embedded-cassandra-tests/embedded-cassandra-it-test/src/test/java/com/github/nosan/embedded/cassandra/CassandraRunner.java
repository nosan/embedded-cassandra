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

import java.util.function.Consumer;

/**
 * Utility class to test {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
public final class CassandraRunner {

	private final CassandraFactory factory;

	/**
	 * Creates a {@link CassandraRunner}.
	 *
	 * @param factory the factory to create a new instance.
	 */
	public CassandraRunner(CassandraFactory factory) {
		this.factory = factory;
	}

	/**
	 * Creates a {@link CassandraRunner}.
	 *
	 * @param cassandra the newly created instance
	 */
	public CassandraRunner(Cassandra cassandra) {
		this(() -> cassandra);
	}

	/**
	 * Starts and stops {@link Cassandra} based on the factory.
	 *
	 * @param consumer the consumer of the created {@link Cassandra}
	 */
	public void run(Consumer<? super Cassandra> consumer) {
		Cassandra cassandra = this.factory.create();
		cassandra.start();
		try {
			consumer.accept(cassandra);
		}
		finally {
			cassandra.stop();
		}
	}

	/**
	 * Starts and stops {@link Cassandra}.
	 */
	public void run() {
		run(cassandra -> {
		});
	}

}
