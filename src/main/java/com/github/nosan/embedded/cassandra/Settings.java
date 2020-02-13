/*
 * Copyright 2020 the original author or authors.
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

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Cassandra settings.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public interface Settings {

	/**
	 * Gets the name of this {@code Cassandra} instance.
	 *
	 * @return name of this {@code Cassandra} instance
	 */
	String getName();

	/**
	 * Gets the {@link Version} of this {@code Cassandra} instance.
	 *
	 * @return a version
	 */
	Version getVersion();

	/**
	 * Gets address this {@code Cassandra} is listening on.
	 *
	 * @return the address
	 */
	InetAddress getAddress();

	/**
	 * Gets either port or SSL port this {@code Cassandra} is listening on.
	 *
	 * @return the port
	 */
	int getPort();

	/**
	 * Gets SSL port this {@code Cassandra} is listening on.
	 *
	 * @return the SSL port, or empty if {@code native_transport_port_ssl} was not set.
	 */
	OptionalInt getSslPort();

	/**
	 * Gets RPC port this {@code Cassandra} is listening on.
	 *
	 * @return the port
	 */
	int getRpcPort();

	/**
	 * Gets the working directory.
	 *
	 * @return working directory
	 */
	Path getWorkingDirectory();

	/**
	 * JVM options which were used during the Cassandra creation.
	 *
	 * @return the JVM options
	 */
	Set<String> getJvmOptions();

	/**
	 * Startup parameters which were used during the Cassandra creation.
	 *
	 * @return the startup parameters
	 */
	Map<String, String> getSystemProperties();

	/**
	 * Environment variables which were used during the Cassandra creation.
	 *
	 * @return the environment variables
	 */
	Map<String, String> getEnvironmentVariables();

	/**
	 * Configuration properties which were loaded from {@code cassandra.yaml} file.
	 *
	 * @return the configuration properties
	 */
	Map<String, Object> getConfigProperties();

}
