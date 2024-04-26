/*
 * Copyright 2020-2024 the original author or authors.
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

/**
 * Embedded Cassandra provides an easy way to start and stop Apache Cassandra.
 * <p>Public API contains the following classes and interfaces:
 * <p>
 * {@link com.github.nosan.embedded.cassandra.Cassandra} Interface that represents Cassandra instance.
 * <p>{@link com.github.nosan.embedded.cassandra.Version}</p>
 * A class that parses and represents Cassandra version.
 * <p>{@link com.github.nosan.embedded.cassandra.CassandraBuilder}</p> A builder class that can be used to configure
 * and create {@link com.github.nosan.embedded.cassandra.Cassandra} instance.
 * <p>
 * {@link com.github.nosan.embedded.cassandra.cql.CqlScript}</p> Interface that loads CQL statements from various
 * sources.
 * <p>
 * {@link com.github.nosan.embedded.cassandra.WorkingDirectoryInitializer}
 * </p>
 * A strategy interface to initialize working directory.
 * <p>
 * {@link com.github.nosan.embedded.cassandra.WorkingDirectoryDestroyer}
 * </p>
 * A strategy interface to destroy the working directory.
 * <p>
 * {@link com.github.nosan.embedded.cassandra.WorkingDirectoryCustomizer}
 * </p>
 * A customizer interface that can be used to customize the working directory.
 * <p>{@link com.github.nosan.embedded.cassandra.CassandraBuilderConfigurator}</p>
 * A callback interface to configure a {@link com.github.nosan.embedded.cassandra.CassandraBuilder}.
 * <p><strong>Example:</strong>
 * <pre>
 * Cassandra cassandra = new CassandraBuilder().build();
 * cassandra.start();
 * try {
 * 	Settings settings = cassandra.getSettings();
 * 	try (Cluster cluster = Cluster.builder().addContactPoints(settings.getAddress())
 * 			.withPort(settings.getPort()).build()) {
 * 		Session session = cluster.connect();
 * 		CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
 *        }
 * }
 * finally {
 * 	cassandra.stop();
 * }
 * </pre>
 */

package com.github.nosan.embedded.cassandra;

