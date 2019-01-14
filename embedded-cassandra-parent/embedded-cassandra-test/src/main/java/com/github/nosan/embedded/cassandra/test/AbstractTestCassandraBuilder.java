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

package com.github.nosan.embedded.cassandra.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.datastax.driver.core.Cluster;
import org.apiguardian.api.API;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.junit.CassandraRuleBuilder;
import com.github.nosan.embedded.cassandra.test.jupiter.CassandraExtensionBuilder;

/**
 * Abstract Builder to create a sub-class of the {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 * @see TestCassandraBuilder
 * @see CassandraRuleBuilder
 * @see CassandraExtensionBuilder
 * @since 1.2.10
 */
@API(since = "1.2.10", status = API.Status.STABLE)
@SuppressWarnings("unchecked")
public abstract class AbstractTestCassandraBuilder<C extends TestCassandra,
		B extends AbstractTestCassandraBuilder<C, B>> {

	@Nullable
	private ClusterFactory clusterFactory;

	@Nullable
	private CassandraFactory cassandraFactory;

	@Nonnull
	private List<CqlScript> scripts = new ArrayList<>();

	private boolean registerShutdownHook = true;

	/**
	 * Add an additional {@link CqlScript} to execute.
	 *
	 * @param scripts CQL scripts
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B addScripts(@Nullable Collection<? extends CqlScript> scripts) {
		if (scripts != null) {
			this.scripts.addAll(scripts);
		}
		return (B) this;
	}

	/**
	 * Add an additional {@link CqlScript} to execute.
	 *
	 * @param scripts CQL scripts
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B addScripts(@Nullable CqlScript... scripts) {
		if (scripts != null) {
			return addScripts(Arrays.asList(scripts));
		}
		return (B) this;
	}

	/**
	 * Sets  {@link CqlScript} to execute.
	 *
	 * @param scripts CQL scripts
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B setScripts(@Nullable CqlScript... scripts) {
		this.scripts.clear();
		return addScripts(scripts);
	}

	/**
	 * Return the {@link ClusterFactory} to use.
	 *
	 * @return the cluster factory attribute
	 */
	@Nullable
	protected final ClusterFactory getClusterFactory() {
		return this.clusterFactory;
	}

	/**
	 * Set a factory to create a {@link Cluster}.
	 *
	 * @param clusterFactory factory to create a cluster
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B setClusterFactory(@Nullable ClusterFactory clusterFactory) {
		this.clusterFactory = clusterFactory;
		return (B) this;
	}

	/**
	 * Return the {@link CassandraFactory} to use.
	 *
	 * @return the cassandra factory attribute
	 */
	@Nullable
	protected final CassandraFactory getCassandraFactory() {
		return this.cassandraFactory;
	}

	/**
	 * Set a factory to create a {@link Cassandra}.
	 *
	 * @param cassandraFactory factory to create a cassandra
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B setCassandraFactory(@Nullable CassandraFactory cassandraFactory) {
		this.cassandraFactory = cassandraFactory;
		return (B) this;
	}

	/**
	 * Return the CQL scripts to execute.
	 *
	 * @return the CQL scripts attribute
	 */
	@Nonnull
	protected final CqlScript[] getScripts() {
		return this.scripts.toArray(new CqlScript[0]);
	}

	/**
	 * Sets {@link CqlScript} to execute.
	 *
	 * @param scripts CQL scripts
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B setScripts(@Nullable Collection<? extends CqlScript> scripts) {
		this.scripts.clear();
		return addScripts(scripts);
	}

	/**
	 * Return the shutdown hook attribute.
	 *
	 * @return the shutdown hook attribute
	 */
	protected final boolean isRegisterShutdownHook() {
		return this.registerShutdownHook;
	}

	/**
	 * Whether shutdown hook should be registered or not.
	 *
	 * @param registerShutdownHook shutdown hook value
	 * @return {@code this} builder for use in a chained invocation
	 */
	@Nonnull
	public final B setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
		return (B) this;
	}

	/**
	 * Builds a new {@code TestCassandra}.
	 *
	 * @return a new instance
	 */
	@Nonnull
	public abstract C build();
}
