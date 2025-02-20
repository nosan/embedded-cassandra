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

/**
 * Embedded Cassandra provides an easy way to start and stop Apache Cassandra.
 * <p>The public API includes the following classes and interfaces:</p>
 * <ul>
 *     <li>{@link com.github.nosan.embedded.cassandra.Cassandra} - An interface that represents a Cassandra
 *     instance.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.Version} - A class that parses and represents a Cassandra
 *     version.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.CassandraBuilder} - A builder class used to configure
 *         and create a {@link com.github.nosan.embedded.cassandra.Cassandra} instance.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.cql.CqlScript} - An interface that loads CQL statements from
 *     various sources.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.WorkingDirectoryInitializer} - A strategy interface to initialize
 *         the working directory.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.WorkingDirectoryDestroyer} - A strategy interface to destroy the
 *         working directory.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.WorkingDirectoryCustomizer} - A customizer interface used to
 *         modify the working directory.</li>
 *     <li>{@link com.github.nosan.embedded.cassandra.CassandraBuilderConfigurator} - A callback interface to configure
 *     a {@link com.github.nosan.embedded.cassandra.CassandraBuilder}.</li>
 * </ul>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * Cassandra cassandra = new CassandraBuilder().build();
 * cassandra.start();
 * try {
 *     Settings settings = cassandra.getSettings();
 *     try (Cluster cluster = Cluster.builder().addContactPoints(settings.getAddress())
 *                                    .withPort(settings.getPort()).build()) {
 *         Session session = cluster.connect();
 *         CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
 *     }
 * } finally {
 *     cassandra.stop();
 * }
 * </pre>
 */

package com.github.nosan.embedded.cassandra;
