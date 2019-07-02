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

import com.datastax.driver.core.Session;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.test.util.SessionUtils;

/**
 * {@link Session} based connection to the {@link Cassandra}.
 *
 * @author Dmytro Nosan
 * @since 2.0.2
 * @deprecated since 2.0.4 with no replacement. Use {@link ClusterConnection} instead.
 */
@Deprecated
public class SessionConnection implements Connection {

	private final Session session;

	/**
	 * Creates a {@link SessionConnection}.
	 *
	 * @param session a session
	 */
	public SessionConnection(Session session) {
		this.session = Objects.requireNonNull(session, "Session must not be null");
	}

	@Override
	public void execute(CqlScript... scripts) {
		Objects.requireNonNull(scripts, "Scripts must not be null");
		SessionUtils.execute(this.session, scripts);
	}

	@Override
	public Session get() {
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
