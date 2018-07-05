/*
 * Copyright 2012-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.time.Duration;
import java.util.List;

import com.github.nosan.embedded.cassandra.process.customizer.FileCustomizer;
import com.github.nosan.embedded.cassandra.support.CassandraConfigBuilder;

/**
 * An Embedded Cassandra Config.
 *
 * @author Dmytro Nosan
 * @see CassandraConfigBuilder
 */
public interface CassandraConfig {

	/**
	 * Retrieves Cassandra's {@link Config Config}.
	 * @return Config to use.
	 */
	Config getConfig();

	/**
	 * Retrieves startup timeout.
	 * @return Cassandra's startup timeout.
	 */
	Duration getTimeout();

	/**
	 * Retrieves {@link FileCustomizer FileCustomizers}.
	 * @return Cassandra's File customizers.
	 *
	 * @see FileCustomizer
	 */
	List<FileCustomizer> getFileCustomizers();

	/**
	 * Retrieves Cassandra's {@link Version Version}.
	 * @return Version to use.
	 */
	Version getVersion();

	/**
	 * Retrieves Cassandra's JMX Port.
	 * @return JMX_PORT to use.
	 * @see com.github.nosan.embedded.cassandra.process.customizer.JmxPortCustomizer
	 */
	int getJmxPort();

	/**
	 * Retrieves Cassandra's JVM Options.
	 * @return JVM Options to use.
	 * @see com.github.nosan.embedded.cassandra.process.customizer.JVMOptionsCustomizer
	 */
	List<String> getJvmOptions();

}
