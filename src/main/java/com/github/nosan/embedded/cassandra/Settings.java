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

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Map;
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
	 * @return the name of this {@code Cassandra} instance, never {@code null}
	 */
	String getName();

	/**
	 * Gets the {@link Version} of this {@code Cassandra} instance.
	 *
	 * @return the version, never {@code null}
	 */
	Version getVersion();

	/**
	 * Gets the Cassandra configuration file.
	 *
	 * @return the configuration file, never {@code null}
	 */
	Path getConfigurationFile();

	/**
	 * Gets the working directory.
	 *
	 * @return the working directory, never {@code null}
	 */
	Path getWorkingDirectory();

	/**
	 * Checks whether the native transport is enabled.
	 *
	 * @return {@code true} if the native transport is enabled, otherwise {@code false}
	 */
	boolean isNativeTransportEnabled();

	/**
	 * Gets the address this {@code Cassandra} instance is listening on.
	 *
	 * @return the address, or {@code null} if the native transport is disabled
	 */
	InetAddress getAddress();

	/**
	 * Gets the port this {@code Cassandra} instance is listening on.
	 *
	 * @return the port, or {@code null} if the native transport is disabled
	 */
	Integer getPort();

	/**
	 * Gets the SSL port this {@code Cassandra} instance is listening on.
	 *
	 * @return the SSL port, or {@code null} if {@code native_transport_port_ssl} is not set
	 */
	Integer getSslPort();

	/**
	 * Gets the Cassandra JVM options used at startup.
	 *
	 * @return the JVM options, never {@code null}
	 */
	Set<String> getJvmOptions();

	/**
	 * Gets the Cassandra JVM parameters used at startup.
	 *
	 * @return the JVM parameters, never {@code null}
	 */
	Map<String, String> getSystemProperties();

	/**
	 * Gets the Cassandra environment variables used at startup.
	 *
	 * @return the environment variables, never {@code null}
	 */
	Map<String, String> getEnvironmentVariables();

	/**
	 * Gets the Cassandra configuration properties.
	 *
	 * @return the configuration properties, never {@code null}
	 */
	Map<String, Object> getConfigProperties();

}
