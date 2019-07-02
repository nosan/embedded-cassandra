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

package com.github.nosan.embedded.cassandra.test;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.util.ClassUtils;

/**
 * {@link Connection} that detects the client implementation based on the classpath.
 *
 * @author Dmytro Nosan
 * @since 2.0.2
 * @deprecated since 2.0.4 in favor of {@link DefaultConnectionFactory}.
 */
@Deprecated
public class DefaultConnection implements Connection {

	private static final String CQL_SESSION_CLASS = "com.datastax.oss.driver.api.core.CqlSession";

	private static final String CLUSTER_CLASS = "com.datastax.driver.core.Cluster";

	private final Connection connection;

	/**
	 * Creates a {@link DefaultConnection}.
	 *
	 * @param settings the settings
	 */
	public DefaultConnection(Settings settings) {
		this.connection = create(getClass().getClassLoader(), settings);
	}

	@Override
	public void execute(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.connection.execute(scripts);
	}

	@Override
	public Object get() {
		return this.connection.get();
	}

	@Override
	public void close() {
		this.connection.close();
	}

	@Override
	public boolean isClosed() {
		return this.connection.isClosed();
	}

	private static Connection create(ClassLoader cl, Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		if (ClassUtils.isPresent(CQL_SESSION_CLASS, cl)) {
			return new CqlSessionConnection(settings);
		}
		if (ClassUtils.isPresent(CLUSTER_CLASS, cl)) {
			return new ClusterConnection(settings);
		}
		return new NoOpConnection();
	}

	private static class NoOpConnection implements Connection {

		@Override
		public void execute(CqlScript... scripts) {
			Objects.requireNonNull(scripts, "Scripts must not be null");
			throw new IllegalStateException(String.format("Failed to execute CQL scripts: '%s'."
							+ " '%s' and '%s' classes are not present in the classpath.",
					Arrays.stream(scripts).map(String::valueOf).collect(Collectors.joining(",")),
					CQL_SESSION_CLASS, CLUSTER_CLASS));
		}

		@Override
		public Object get() {
			return this;
		}

	}

}
