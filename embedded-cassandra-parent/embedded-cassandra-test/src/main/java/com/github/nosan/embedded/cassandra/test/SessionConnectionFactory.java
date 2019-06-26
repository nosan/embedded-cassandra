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

import java.util.Objects;
import java.util.function.Function;

import com.datastax.driver.core.Cluster;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * {@link ConnectionFactory} that creates {@link SessionConnection}.
 *
 * @author Dmytro Nosan
 * @since 2.0.4
 */
public class SessionConnectionFactory implements ConnectionFactory {

	private final Function<Settings, Cluster> clusterFactory;

	/**
	 * Creates a {@link SessionConnectionFactory}.
	 */
	public SessionConnectionFactory() {
		this(new ClusterFactory()::create);
	}

	/**
	 * Creates a {@link SessionConnectionFactory}.
	 *
	 * @param clusterFactory the cluster factory
	 */
	public SessionConnectionFactory(ClusterFactory clusterFactory) {
		this(Objects.requireNonNull(clusterFactory, "ClusterFactory must not be null")::create);
	}

	/**
	 * Creates a {@link SessionConnectionFactory}.
	 *
	 * @param clusterFactory the cluster factory
	 */
	public SessionConnectionFactory(Function<Settings, Cluster> clusterFactory) {
		this.clusterFactory = Objects.requireNonNull(clusterFactory, "ClusterFactory must not be null");
	}

	@Override
	public SessionConnection create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		Cluster cluster = this.clusterFactory.apply(settings);
		Objects.requireNonNull(cluster, "Cluster must not be null");
		return new ClusterSessionConnection(cluster);
	}

	private static class ClusterSessionConnection extends SessionConnection {

		private final Cluster cluster;

		ClusterSessionConnection(Cluster cluster) {
			super(cluster.connect());
			this.cluster = cluster;
		}

		@Override
		public void close() {
			try {
				super.close();
			}
			catch (Throwable ex) {
				//ignore
			}
			this.cluster.close();
		}

	}

}
