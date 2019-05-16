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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
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
	public final CqlSession create(Settings settings) {
		Objects.requireNonNull(settings, "Settings must not be null");
		Integer port = settings.port().orElseGet(() -> settings.sslPort().orElse(null));
		InetAddress address = settings.address().orElse(null);
		if (address != null && port != null) {
			DriverConfigLoader driverConfigLoader = buildDriverConfigLoader(DriverConfigLoader.programmaticBuilder()
					.withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, USERNAME)
					.withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, PASSWORD)
					.withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
					.withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
					.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(3)));
			Objects.requireNonNull(driverConfigLoader, "Driver Config must not be null");
			CqlSession cqlSession = buildCqlSession(CqlSession.builder().addContactPoint(
					new InetSocketAddress(address, port))
					.withLocalDatacenter(DATACENTER)
					.withConfigLoader(driverConfigLoader));
			return Objects.requireNonNull(cqlSession, "Cql Session must not be null");
		}
		throw new IllegalStateException(String.format("Cql Session can not be created from %s", settings));
	}

	/**
	 * Creates a new configured {@link CqlSession}.
	 *
	 * @param builder a session builder
	 * @return a session
	 * @since 2.0.1
	 */
	protected CqlSession buildCqlSession(CqlSessionBuilder builder) {
		return builder.build();
	}

	/**
	 * Creates a new configured {@link DriverConfigLoader}.
	 *
	 * @param builder a driver builder
	 * @return a driver config
	 * @since 2.0.1
	 */
	protected DriverConfigLoader buildDriverConfigLoader(ProgrammaticDriverConfigLoaderBuilder builder) {
		return builder.build();
	}

	/**
	 * A simple authentication provider that extends
	 * {@link com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider} and disables log warning message.
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
