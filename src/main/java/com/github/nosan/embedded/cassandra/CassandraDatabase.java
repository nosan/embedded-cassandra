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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * An abstraction for a Cassandra database, providing methods to manage its lifecycle and access its configuration,
 * process details, and runtime information.
 *
 * @author Dmytro Nosan
 */
interface CassandraDatabase {

	/**
	 * Starts the Cassandra database.
	 *
	 * @throws IOException if an error occurs during startup
	 */
	void start() throws IOException;

	/**
	 * Stops the Cassandra database.
	 *
	 * @throws IOException if an error occurs during shutdown
	 */
	void stop() throws IOException;

	/**
	 * Checks if the database process is still running.
	 *
	 * @return {@code true} if the process is alive, otherwise {@code false}
	 */
	boolean isAlive();

	/**
	 * Returns a {@link CompletableFuture} that completes when the database has exited.
	 *
	 * @return a {@code CompletableFuture} that completes when the database process terminates
	 */
	CompletableFuture<? extends CassandraDatabase> onExit();

	/**
	 * Gets the name of the Cassandra database instance.
	 *
	 * @return the database name
	 */
	String getName();

	/**
	 * Retrieves the environment variables used by the Cassandra process.
	 *
	 * @return a {@link Map} of environment variable names to values
	 */
	Map<String, String> getEnvironmentVariables();

	/**
	 * Gets the Cassandra configuration properties.
	 *
	 * @return a {@link Map} of configuration property names to their values
	 */
	Map<String, Object> getConfigProperties();

	/**
	 * Gets the system properties used by the Cassandra JVM process.
	 *
	 * @return a {@link Map} of system property names to values
	 */
	Map<String, String> getSystemProperties();

	/**
	 * Retrieves the set of JVM options used by the Cassandra process.
	 *
	 * @return a {@link Set} of JVM options
	 */
	Set<String> getJvmOptions();

	/**
	 * Gets the version of the Cassandra database.
	 *
	 * @return the Cassandra {@link Version}
	 */
	Version getVersion();

	/**
	 * Gets the working directory of the Cassandra instance.
	 *
	 * @return the {@link Path} to the working directory
	 */
	Path getWorkingDirectory();

	/**
	 * Gets the path to Cassandra's configuration file.
	 *
	 * @return the {@link Path} to the configuration file
	 */
	Path getConfigurationFile();

	/**
	 * Retrieves the process's standard output stream.
	 *
	 * @return the {@link ProcessWrapper.Output} of the standard output
	 */
	ProcessWrapper.Output getStdOut();

	/**
	 * Retrieves the process's standard error stream.
	 *
	 * @return the {@link ProcessWrapper.Output} of the standard error
	 */
	ProcessWrapper.Output getStdErr();

}
