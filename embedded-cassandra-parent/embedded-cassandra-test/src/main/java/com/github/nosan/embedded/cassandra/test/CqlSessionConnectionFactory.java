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

import com.datastax.oss.driver.api.core.CqlSession;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * {@link ConnectionFactory} that creates {@link CqlSessionConnection}.
 *
 * @author Dmytro Nosan
 * @since 2.0.4
 */
public class CqlSessionConnectionFactory implements ConnectionFactory {

	private final Function<Settings, CqlSession> sessionFactory;

	/**
	 * Creates a {@link CqlSessionConnectionFactory}.
	 */
	public CqlSessionConnectionFactory() {
		this(new CqlSessionFactory()::create);
	}

	/**
	 * Creates a {@link CqlSessionConnectionFactory}.
	 *
	 * @param sessionFactory session factory
	 */
	public CqlSessionConnectionFactory(CqlSessionFactory sessionFactory) {
		this(Objects.requireNonNull(sessionFactory, "CqlSessionFactory must not be null")::create);
	}

	/**
	 * Creates a {@link CqlSessionConnectionFactory}.
	 *
	 * @param sessionFactory session factory
	 */
	public CqlSessionConnectionFactory(Function<Settings, CqlSession> sessionFactory) {
		this.sessionFactory = Objects.requireNonNull(sessionFactory, "CqlSessionFactory must not be null");
	}

	@Override
	public CqlSessionConnection create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		return new CqlSessionConnection(this.sessionFactory.apply(settings));
	}

}
