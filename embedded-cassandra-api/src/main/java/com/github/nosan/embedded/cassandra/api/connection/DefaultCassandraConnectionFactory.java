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

package com.github.nosan.embedded.cassandra.api.connection;

import java.util.Objects;

import com.github.nosan.embedded.cassandra.api.Cassandra;

/**
 * {@link CassandraConnectionFactory} that creates a {@link CassandraConnection} based on the classpath.
 *
 * @author Dmytro Nosan
 * @since 3.0.0
 */
public final class DefaultCassandraConnectionFactory implements CassandraConnectionFactory {

	private static final String CQL_SESSION_CLASS = "com.datastax.oss.driver.api.core.CqlSession";

	private static final String CLUSTER_CLASS = "com.datastax.driver.core.Cluster";

	@Override
	public CassandraConnection create(Cassandra cassandra) {
		Objects.requireNonNull(cassandra, "'cassandra' must not be null");
		ClassLoader classLoader = getClass().getClassLoader();
		if (isPresent(CQL_SESSION_CLASS, classLoader)) {
			return new CqlSessionCassandraConnectionFactory().create(cassandra);
		}
		if (isPresent(CLUSTER_CLASS, classLoader)) {
			return new ClusterCassandraConnectionFactory().create(cassandra);
		}
		throw new IllegalStateException(String.format("Can not create a '%s' instance. "
						+ "Both '%s' and '%s' classes are not present on the classpath.", CassandraConnection.class,
				CQL_SESSION_CLASS, CLUSTER_CLASS));
	}

	private static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			Class.forName(className, false, classLoader);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

}
