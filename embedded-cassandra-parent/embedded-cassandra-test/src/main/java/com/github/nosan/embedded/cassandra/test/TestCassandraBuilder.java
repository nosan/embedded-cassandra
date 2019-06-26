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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;

/**
 * A builder to configure and build a {@link TestCassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.4
 */
public final class TestCassandraBuilder {

	private final List<CqlScript> scripts = new ArrayList<>();

	@Nullable
	private CassandraFactory cassandraFactory;

	@Nullable
	private ConnectionFactory connectionFactory;

	/**
	 * Set the {@link CassandraFactory} that should be used with the {@link TestCassandra}.
	 *
	 * @param cassandraFactory the cassandra factory
	 * @return this builder
	 */
	public TestCassandraBuilder cassandraFactory(@Nullable CassandraFactory cassandraFactory) {
		this.cassandraFactory = cassandraFactory;
		return this;
	}

	/**
	 * Set the {@link ConnectionFactory} that should be used with the {@link TestCassandra}.
	 *
	 * @param connectionFactory the connection factory
	 * @return this builder
	 */
	public TestCassandraBuilder connectionFactory(@Nullable ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		return this;
	}

	/**
	 * Set the {@link CqlScript} that should be used with the {@link TestCassandra}. Setting this value will replace any
	 * previously defined scripts.
	 *
	 * @param scripts the scripts to set
	 * @return this builder
	 * @see #addScripts(Iterable)
	 */
	public TestCassandraBuilder scripts(Iterable<? extends CqlScript> scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		this.scripts.clear();
		scripts.forEach(this.scripts::add);
		return this;
	}

	/**
	 * Set the {@link CqlScript} that should be used with the {@link TestCassandra}. Setting this value will replace any
	 * previously defined scripts.
	 *
	 * @param scripts the scripts to set
	 * @return this builder
	 * @see #addScripts(CqlScript...)
	 */
	public TestCassandraBuilder scripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		return scripts(Arrays.asList(scripts));
	}

	/**
	 * Add additional {@link CqlScript} that should be used with the {@link TestCassandra}.
	 *
	 * @param scripts the scripts to add
	 * @return this builder
	 * @see #scripts(CqlScript...)
	 */
	public TestCassandraBuilder addScripts(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		return addScripts(Arrays.asList(scripts));
	}

	/**
	 * Add additional {@link CqlScript} that should be used with the {@link TestCassandra}.
	 *
	 * @param scripts the scripts to add
	 * @return this builder
	 * @see #scripts(Iterable)
	 */
	public TestCassandraBuilder addScripts(Iterable<? extends CqlScript> scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		scripts.forEach(this.scripts::add);
		return this;
	}

	/**
	 * Build a new {@link TestCassandra} instance and configure it using this builder.
	 *
	 * @return a configured {@link TestCassandra} instance.
	 * @see #build(Class)
	 */
	public TestCassandra build() {
		return build(TestCassandra.class);
	}

	/**
	 * Build a new {@link TestCassandra} instance of the specified type and configure it using this builder.
	 *
	 * @param <T> the type of {@code TestCassandra}
	 * @param testCassandraClass the {@code TestCassandra} type to create
	 * @return a configured {@link TestCassandra} instance.
	 */
	public <T extends TestCassandra> T build(Class<? extends T> testCassandraClass) {
		Objects.requireNonNull(testCassandraClass, "TestCassandra Class must not be null");
		try {
			Constructor<? extends T> constructor = testCassandraClass.getConstructor(
					CassandraFactory.class, ConnectionFactory.class, CqlScript[].class);
			return constructor.newInstance(this.cassandraFactory, this.connectionFactory,
					this.scripts.toArray(new CqlScript[0]));
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
