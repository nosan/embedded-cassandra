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

import com.datastax.oss.driver.api.core.CqlSession;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.util.CqlSessionUtils;

/**
 * {@link CqlSession} based connection to the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @see CqlSessionFactory
 * @since 2.0.2
 */
public class CqlSessionConnection implements Connection {

	private final CqlSession session;

	/**
	 * Creates a {@link CqlSessionConnection}.
	 *
	 * @param session a session
	 */
	public CqlSessionConnection(CqlSession session) {
		this.session = Objects.requireNonNull(session, "Cql Session must not be null");
	}

	/**
	 * Creates a {@link CqlSessionConnection}.
	 *
	 * @param settings the settings
	 */
	public CqlSessionConnection(Settings settings) {
		this(new CqlSessionFactory().create(settings));
	}

	@Override
	public void execute(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		CqlSessionUtils.execute(this.session, scripts);
	}

	@Override
	public CqlSession get() {
		return this.session;
	}

	@Override
	public void close() {
		this.session.close();
	}

	@Override
	public boolean isClosed() {
		return this.session.isClosed();
	}

}
