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
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import com.github.nosan.embedded.cassandra.cql.ClassPathCqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.CqlScripts;
import com.github.nosan.embedded.cassandra.cql.StaticCqlScript;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.CauseMatcher;
import com.github.nosan.embedded.cassandra.util.FileUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract test-class for {@link LocalProcess}.
 *
 * @author Dmytro Nosan
 */
public abstract class AbstractLocalCassandraTests {

	protected final LocalCassandraFactory factory;

	@Rule
	public ExpectedException throwable = ExpectedException.none();

	@Rule
	public CaptureOutput output = new CaptureOutput();

	AbstractLocalCassandraTests(@Nonnull Version version) {
		this.factory = new LocalCassandraFactory();
		this.factory.setVersion(version);
	}

	@Test
	public void shouldStartedIfTransportDisabled() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		new CassandraRunner(this.factory).run();
	}

	@Test
	public void shouldStartStopOnlyOnce() {
		CassandraRunner runner = new CassandraRunner(this.factory);
		CqlAssert cqlAssert = new CqlAssert(new ClassPathCqlScript("../cql/keyspace.cql", getClass()));
		Cassandra c = runner.run(cqlAssert.andThen(cassandra -> {
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

		new CassandraRunner(this.factory).run();

	}

	@Test
	public void shouldRunMoreThanOneCassandra() {
		CassandraRunner runner = new CassandraRunner(this.factory);
		CqlAssert cqlAssert = new CqlAssert(new ClassPathCqlScript("../cql/keyspace.cql", getClass()));
		runner.run(cassandra -> {
			cqlAssert.accept(cassandra);
			runner.run(cqlAssert);
		});
	}

	@Test
	public void shouldKeepDataBetweenLaunches() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-static.yaml"));
		this.factory.setWorkingDirectory(FileUtils.getUserDirectory()
				.resolve("target")
				.resolve(UUID.randomUUID().toString()));
		this.factory.getJvmOptions().add("-Dcassandra.jmx.local.port=9100");
		try {
			CassandraRunner runner = new CassandraRunner(this.factory);
			OutputAssert outputAssert = new OutputAssert(this.output, "-Dcassandra.jmx.local.port=9100");
			runner.run(outputAssert.andThen(new CqlAssert(new ClassPathCqlScript("../cql/keyspace.cql", getClass()))));
			this.output.reset();
			runner.run(outputAssert.andThen(new CqlAssert(new StaticCqlScript("DROP KEYSPACE test"))));
		}
		finally {
			IOUtils.closeQuietly(() -> FileUtils.delete(this.factory.getWorkingDirectory()));
		}
	}

	/**
	 * Assert {@code Cassandra} output.
	 */
	protected static final class OutputAssert implements Consumer<Cassandra> {

		@Nonnull
		private final CaptureOutput output;

		@Nonnull
		private final String message;

		OutputAssert(@Nonnull CaptureOutput output, @Nonnull String message) {
			this.output = output;
			this.message = message;
		}

		@Override
		public void accept(Cassandra cassandra) {
			assertThat(this.output.toString()).contains(this.message);
		}
	}

	/**
	 * Assert {@code CQL} scripts could be invoked.
	 */
	protected static final class CqlAssert implements Consumer<Cassandra> {

		@Nonnull
		private final CqlScript script;

		CqlAssert(@Nullable CqlScript... scripts) {
			this.script = new CqlScripts(scripts);
		}

		private static void executeScript(Cassandra cassandra, CqlScript... cqlScript) {
			try (Cluster cluster = cluster(cassandra)) {
				Session session = cluster.connect();
				CqlScripts cqlScripts = new CqlScripts(cqlScript);
				for (String statement : cqlScripts.getStatements()) {
					assertThat(session.execute(statement).wasApplied())
							.describedAs("Statement (%s) is not applied", statement).isTrue();
				}

			}
		}

		private static Cluster cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return Cluster.builder().withPort(settings.getPort())
					.addContactPoint(settings.getAddress())
					.build();
		}

		@Override
		public void accept(@Nonnull Cassandra cassandra) {
			executeScript(cassandra, this.script);
		}


	}
}
