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

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.EndPoint;

import com.github.nosan.embedded.cassandra.Settings;

/**
 * {@link CqlSession} factory with a default strategy.
 *
 * @author Dmytro Nosan
 * @since 2.0.0
 */
public class CqlSessionFactory {

	private static final String USERNAME = "cassandra";

	private static final String PASSWORD = "cassandra";

	private static final String DATACENTER = "datacenter1";

	/**
	 * Creates a new configured {@link CqlSession}.
	 *
	 * @param settings the settings
	 * @return a cql session
	 */
	public CqlSession create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		return CqlSession.builder()
				.addContactPoint(new InetSocketAddress(settings.getAddress(), settings.getPort()))
				.withLocalDatacenter(DATACENTER)
				.withConfigLoader(DriverConfigLoader.programmaticBuilder()
						.withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, USERNAME)
						.withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, PASSWORD)
						.withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
						.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
						.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(3))
						.build())
				.build();
	}

	/**
	 * A simple authentication provider that extends
	 * {@link com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider} and disables the log warning message.
	 */
	public static class PlainTextAuthProvider extends com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider {

		public PlainTextAuthProvider(DriverContext context) {
			super(context);
		}

		@Override
		public void onMissingChallenge(EndPoint endPoint) {
		}

	}

}
