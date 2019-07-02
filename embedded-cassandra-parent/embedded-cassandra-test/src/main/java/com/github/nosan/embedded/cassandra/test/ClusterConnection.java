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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.util.SessionUtils;

/**
 * {@link Cluster} based connection to the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see ClusterFactory
 * @since 2.0.2
 */
public class ClusterConnection implements Connection {

	private final Cluster cluster;

	private final Session session;

	/**
	 * Creates a {@link ClusterConnection}.
	 *
	 * @param cluster a cluster
	 */
	public ClusterConnection(Cluster cluster) {
		this.cluster = Objects.requireNonNull(cluster, "Cluster must not be null");
		this.session = this.cluster.connect();
	}

	/**
	 * Creates a {@link ClusterConnection}.
	 *
	 * @param settings the settings
	 */
	public ClusterConnection(Settings settings) {
		this(new ClusterFactory().create(settings));
	}

	@Override
	public void execute(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		SessionUtils.execute(this.session, scripts);
	}

	@Override
	public Cluster get() {
		return this.cluster;
	}

	@Override
	public void close() {
		this.cluster.close();
	}

	@Override
	public boolean isClosed() {
		return this.cluster.isClosed();
	}

}
