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
	 * @return name of this {@code Cassandra} instance, never {@code null}
	 */
	String getName();

	/**
	 * Gets the {@link Version} of this {@code Cassandra} instance.
	 *
	 * @return a version,  never {@code null}
	 */
	Version getVersion();

	/**
	 * Gets Cassandra configuration file.
	 *
	 * @return the configuration file,  never {@code null}
	 */
	Path getConfigurationFile();

	/**
	 * Gets the working directory.
	 *
	 * @return working directory, never {@code null}
	 */
	Path getWorkingDirectory();

	/**
	 * Checks whether native transport is enabled.
	 *
	 * @return {@code true} if native transport is enabled, otherwise {@code false}
	 */
	boolean isNativeTransportEnabled();

	/**
	 * Gets address this {@code Cassandra} is listening on.
	 *
	 * @return the address, or {@code null} if native transport is disabled.
	 */
	InetAddress getAddress();

	/**
	 * Gets port this {@code Cassandra} is listening on.
	 *
	 * @return the port, or {@code null} if native transport is disabled.
	 */
	Integer getPort();

	/**
	 * Gets SSL port this {@code Cassandra} is listening on.
	 *
	 * @return the SSL port, or {@code null} if {@code native_transport_port_ssl} was not set.
	 */
	Integer getSslPort();

	/**
	 * Gets Cassandra JVM Options used on start.
	 *
	 * @return the JVM options,  never {@code null}
	 */
	Set<String> getJvmOptions();

	/**
	 * Gets Cassandra JVM parameters used on start.
	 *
	 * @return the JVM parameters,  never {@code null}
	 */
	Map<String, String> getSystemProperties();

	/**
	 * Gets Cassandra Environment variables used on start.
	 *
	 * @return the environment variables, never {@code null}
	 */
	Map<String, String> getEnvironmentVariables();

	/**
	 * Gets Cassandra Configuration properties.
	 *
	 * @return the configuration properties, never {@code null}
	 */
	Map<String, Object> getConfigProperties();

}
