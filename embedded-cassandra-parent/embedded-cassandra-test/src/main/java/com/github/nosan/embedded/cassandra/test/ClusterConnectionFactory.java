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
 * {@link ConnectionFactory} that creates {@link ClusterConnection}.
 *
 * @author Dmytro Nosan
 * @see ClusterFactory
 * @since 2.0.4
 */
public class ClusterConnectionFactory implements ConnectionFactory {

	private final Function<Settings, Cluster> clusterFactory;

	/**
	 * Creates a {@link ClusterConnectionFactory}.
	 */
	public ClusterConnectionFactory() {
		this(new ClusterFactory()::create);
	}

	/**
	 * Creates a {@link ClusterConnectionFactory}.
	 *
	 * @param clusterFactory the cluster factory
	 */
	public ClusterConnectionFactory(ClusterFactory clusterFactory) {
		this(Objects.requireNonNull(clusterFactory, "ClusterFactory must not be null")::create);
	}

	/**
	 * Creates a {@link ClusterConnectionFactory}.
	 *
	 * @param clusterFactory the cluster factory
	 */
	public ClusterConnectionFactory(Function<Settings, Cluster> clusterFactory) {
		this.clusterFactory = Objects.requireNonNull(clusterFactory, "ClusterFactory must not be null");
	}

	@Override
	public ClusterConnection create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		return new ClusterConnection(this.clusterFactory.apply(settings));
	}

}
