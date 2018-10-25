/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;

/**
 * Utility class to test {@link Cassandra}.
 *
 * @author Dmytro Nosan
 */
final class CassandraRunner {

	@Nonnull
	private final CassandraFactory factory;

	CassandraRunner(@Nonnull CassandraFactory factory) {
		this.factory = factory;
	}

	/**
	 * Creates and starts a new {@link Cassandra} based on the factory.
	 *
	 * @param consumer the consumer of the created {@link Cassandra}
	 * @return stopped Cassandra
	 */
	@Nonnull
	Cassandra run(@Nonnull Consumer<? super Cassandra> consumer) {
		Cassandra cassandra = this.factory.create();
		cassandra.start();
		try {
			consumer.accept(cassandra);
		}
		finally {
			cassandra.stop();
		}
		return cassandra;
	}


	/**
	 * Creates and starts a new {@link Cassandra} based on the factory.
	 *
	 * @return stopped Cassandra
	 */
	@Nonnull
	Cassandra run() {
		return run(cassandra -> {
		});
	}
}
