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
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.util.ClassUtils;

/**
 * {@link ConnectionFactory} that detects the {@link Connection} implementation based on the
 * classpath.
 *
 * @author Dmytro Nosan
 * @since 2.0.2
 */
public class DefaultConnectionFactory implements ConnectionFactory {

	private static final String CQL_SESSION_CLASS = "com.datastax.oss.driver.api.core.CqlSession";

	private static final String CLUSTER_CLASS = "com.datastax.driver.core.Cluster";

	private static final String SESSION_CLASS = "com.datastax.driver.core.Session";

	@Override
	public Connection create(Settings settings) {
		ClassLoader cl = getClass().getClassLoader();
		if (ClassUtils.isPresent(CQL_SESSION_CLASS, cl)) {
			return new CqlSessionConnectionFactory().create(settings);
		}
		if (ClassUtils.isPresent(CLUSTER_CLASS, cl) && ClassUtils.isPresent(SESSION_CLASS, cl)) {
			return new ClusterConnectionFactory().create(settings);
		}
		return new NoOpConnection();
	}

	private static final class NoOpConnection implements Connection {

		@Override
		public void executeScripts(CqlScript... scripts) {
			throw new IllegalStateException(String.format("There is no way to execute '%s'."
							+ " '%s' and ('%s' or '%s') classes are not present in the classpath.",
					Arrays.stream(scripts).map(String::valueOf).collect(Collectors.joining(",")),
					CQL_SESSION_CLASS, CLUSTER_CLASS, SESSION_CLASS));
		}

		@Override
		public Object getNativeConnection() {
			return this;
		}

		@Override
		public void close() {

		}

	}

}
