/*
 * Copyright 2018-2018 the original author or authors.
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

package com.github.nosan.embedded.cassandra.local;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.CauseMatcher;
import com.github.nosan.embedded.cassandra.util.FileUtils;
import com.github.nosan.embedded.cassandra.util.PortUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Abstract test-class for {@link LocalCassandra}.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractLocalCassandraTests {

	@Rule
	public final ExpectedException throwable = ExpectedException.none();

	@Rule
	public final CaptureOutput output = new CaptureOutput();

	protected final LocalCassandraFactory factory;

	AbstractLocalCassandraTests(@Nonnull Version version) {
		this.factory = new LocalCassandraFactory();
		this.factory.setVersion(version);
	}

	@Test
	public void shouldStartedIfTransportDisabled() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		new CassandraRunner(this.factory).run(assertBusyPort(Settings::getStoragePort));
	}

	@Test
	public void shouldOverrideJavaHome() {
		this.throwable.expect(CassandraException.class);
		this.factory.setJavaHome(Paths.get(UUID.randomUUID().toString()));
		new CassandraRunner(this.factory).run(new NotReachable());
	}

	@Test
	public void shouldStartStopOnlyOnce() {
		CassandraRunner runner = new CassandraRunner(this.factory);
		Cassandra c = runner.run(assertCreateKeyspace().andThen(cassandra -> {
			assertThat(this.output.toString()).contains("Starts Apache Cassandra");
			this.output.reset();
			cassandra.start();
			assertThat(this.output.toString()).doesNotContain("Starts Apache Cassandra");
		}));
		assertThat(this.output.toString()).contains("Stops Apache Cassandra");
		this.output.reset();
		c.stop();
		assertThat(this.output.toString()).doesNotContain("Stops Apache Cassandra");
	}


	@Test
	public void shouldNotGetSettings() {
		this.throwable.expect(CassandraException.class);
		this.throwable.expectMessage("Please start it before calling this method");

		this.factory.create().getSettings();
	}

	@Test
	public void shouldCatchCassandraError() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-invalid.yaml"));

		this.throwable.expect(CassandraException.class);
		this.throwable.expectCause(new CauseMatcher(IOException.class, "invalid_property"));

		new CassandraRunner(this.factory).run(new NotReachable());

	}

	@Test
	public void shouldRunMoreThanOneCassandra() {
		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(cassandra -> {
			assertCreateKeyspace().accept(cassandra);
			runner.run(assertCreateKeyspace());
		});
	}


	@Test
	public void shouldBeRestarted() {
		this.factory.setConfigurationFile(ClassLoader.getSystemResource("cassandra-static.yaml"));
		this.factory.getJvmOptions().add("-Dcassandra.jmx.local.port=7199");
		new CassandraRunner(this.factory)
				.run(assertCreateKeyspace().andThen(assertBusyPort(7199, 9042)));
		new CassandraRunner(this.factory)
				.run(assertCreateKeyspace().andThen(assertBusyPort(7199, 9042)));

	}

	@Test
	public void shouldKeepDataBetweenLaunches() {
		this.factory.setWorkingDirectory(FileUtils.getUserDirectory()
				.resolve("target")
				.resolve(UUID.randomUUID().toString()));
		try {
			CassandraRunner runner = new CassandraRunner(this.factory);
			runner.run(assertCreateKeyspace());
			runner.run(assertDeleteKeyspace());
		}
		finally {
			IOUtils.closeQuietly(() -> FileUtils.delete(this.factory.getWorkingDirectory()));
		}
	}


	private static Consumer<Cassandra> assertDeleteKeyspace() {
		return new CqlAssert("DROP KEYSPACE test");
	}

	private static Consumer<Cassandra> assertCreateKeyspace() {
		return new CqlAssert("CREATE KEYSPACE test" +
				" WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}");
	}

	private static Consumer<Cassandra> assertBusyPort(Function<Settings, Integer> mapper) {
		return cassandra -> assertBusyPort(mapper.apply(cassandra.getSettings()));
	}

	private static Consumer<Cassandra> assertBusyPort(int... port) {
		Consumer<Cassandra> assertPort = new PortBusyAssert(port[0]);
		for (int i = 1; i < port.length; i++) {
			assertPort = assertPort.andThen(new PortBusyAssert(port[i]));
		}
		return assertPort;
	}


	private static final class CqlAssert implements Consumer<Cassandra> {

		@Nonnull
		private final String statement;


		CqlAssert(@Nonnull String statement) {
			this.statement = Objects.requireNonNull(statement);
		}

		@Override
		public void accept(@Nonnull Cassandra cassandra) {
			try (Cluster cluster = cluster(cassandra)) {
				Session session = cluster.connect();
				assertThat(session.execute(this.statement).wasApplied())
						.describedAs("Statement (%s) is not applied", this.statement).isTrue();
			}
		}

		private static Cluster cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return Cluster.builder().withPort(settings.getPort())
					.addContactPoint(Objects.requireNonNull(settings.getAddress()))
					.build();
		}
	}

	private static final class PortBusyAssert implements Consumer<Cassandra> {

		private final int port;

		PortBusyAssert(int port) {
			this.port = port;
		}

		@Override
		public void accept(@Nonnull Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			assertThat(PortUtils.isPortBusy(settings.getAddress(), this.port))
					.describedAs("Port (%s) is not busy", this.port)
					.isTrue();
		}
	}

	private static final class NotReachable implements Consumer<Cassandra> {

		@Override
		public void accept(Cassandra cassandra) {
			fail("This consumer must not be called.");
		}
	}
}
