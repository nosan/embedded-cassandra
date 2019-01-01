/*
 * Copyright 2018-2019 the original author or authors.
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

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.nosan.embedded.cassandra.Version;

/**
 * Default implementation of the {@link CassandraProcessFactory}.
 *
 * @author Dmytro Nosan
 * @see DefaultCassandraProcess
 * @since 1.0.9
 */
class DefaultCassandraProcessFactory implements CassandraProcessFactory {

	@Nonnull
	private final Duration startupTimeout;

	@Nonnull
	private final List<String> jvmOptions;

	@Nonnull
	private final Version version;

	@Nullable
	private final Path javaHome;

	private final int jmxPort;

	private final boolean allowRoot;

	/**
	 * Creates a {@link DefaultCassandraProcessFactory}.
	 *
	 * @param startupTimeout a startup timeout
	 * @param jvmOptions additional {@code JVM} options
	 * @param version a version
	 * @param javaHome java home directory
	 * @param jmxPort JMX port
	 * @param allowRoot allow running as a root
	 */
	DefaultCassandraProcessFactory(@Nonnull Duration startupTimeout, @Nonnull List<String> jvmOptions,
			@Nonnull Version version, @Nullable Path javaHome, int jmxPort, boolean allowRoot) {
		this.startupTimeout = startupTimeout;
		this.version = version;
		this.javaHome = javaHome;
		this.jvmOptions = Collections.unmodifiableList(new ArrayList<>(jvmOptions));
		this.jmxPort = jmxPort;
		this.allowRoot = allowRoot;
	}

	@Nonnull
	@Override
	public CassandraProcess create(@Nonnull Path directory) {
		return new DefaultCassandraProcess(directory, this.version, this.startupTimeout, this.jvmOptions,
				this.javaHome, this.jmxPort, this.allowRoot);
	}
}
