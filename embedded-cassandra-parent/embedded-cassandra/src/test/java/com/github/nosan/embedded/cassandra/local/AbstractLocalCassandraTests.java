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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.CauseMatcher;
import com.github.nosan.embedded.cassandra.util.FileUtils;
import com.github.nosan.embedded.cassandra.util.NetworkUtils;
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

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	protected final LocalCassandraFactory factory;

	AbstractLocalCassandraTests(@Nonnull Version version) {
		this.factory = new LocalCassandraFactory();
		this.factory.setVersion(version);
	}

	@Test
	public void shouldFailIfAnotherCassandraUseSamePorts() {
		this.factory.setConfigurationFile(ClassLoader.getSystemResource("cassandra-defaults.yaml"));

		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(cassandra -> {
			this.throwable.expect(CassandraException.class);
			this.throwable.expectCause(new CauseMatcher(IllegalArgumentException.class, "storage_port (7000)"));
			runner.run(new NotReachable());
		});
		assertCassandraHasBeenStopped();
		assertDirectoryHasBeenDeletedCorrectly();
	}

	@Test
	public void shouldStartedIfTransportDisabled() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		new CassandraRunner(this.factory).run(assertBusyPort(Settings::getRealListenAddress, Settings::getStoragePort));
		assertCassandraHasBeenStopped();
		assertDirectoryHasBeenDeletedCorrectly();
	}

	@Test
	public void shouldRunOnInterfaceIPV4() throws Exception {
		runAndAssertCassandraListenInterface("/cassandra-interface.yaml", false);
	}

	@Test
	public void shouldRunOnInterfaceIPV6() throws Exception {
		runAndAssertCassandraListenInterface("/cassandra-interface-ipv6.yaml", true);
	}

	@Test
	public void notEnoughTime() {
		this.throwable.expect(CassandraException.class);
		this.throwable.expectCause(new CauseMatcher(IOException.class, "(2000) milliseconds have past"));
		this.factory.setStartupTimeout(Duration.ofSeconds(2L));
		new CassandraRunner(this.factory).run(new NotReachable());
		assertDirectoryHasBeenDeletedCorrectly();
	}

	@Test
	public void shouldOverrideJavaHome() {
		this.throwable.expect(CassandraException.class);
		this.factory.setJavaHome(Paths.get(UUID.randomUUID().toString()));
		new CassandraRunner(this.factory).run(new NotReachable());
		assertDirectoryHasBeenDeletedCorrectly();
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
		assertCassandraHasBeenStopped();
		assertDirectoryHasBeenDeletedCorrectly();
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
		assertDirectoryHasBeenDeletedCorrectly();
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
	public void shouldStartOnDefaultSettingsAndBeRestarted() {
		this.factory.setConfigurationFile(ClassLoader.getSystemResource("cassandra-defaults.yaml"));
		this.factory.getJvmOptions().add("-Dcassandra.jmx.local.port=7199");
		new CassandraRunner(this.factory).run(assertCreateKeyspace());
		assertCassandraHasBeenStopped();
		assertDirectoryHasBeenDeletedCorrectly();
		this.output.reset();
		new CassandraRunner(this.factory).run(assertCreateKeyspace());
		assertCassandraHasBeenStopped();
		assertDirectoryHasBeenDeletedCorrectly();

	}

	@Test
	public void shouldKeepDataBetweenLaunches() {
		this.factory.setWorkingDirectory(FileUtils.getUserDirectory()
				.resolve("target")
				.resolve(UUID.randomUUID().toString()));
		try {
			CassandraRunner runner = new CassandraRunner(this.factory);
			runner.run(assertCreateKeyspace());
			assertCassandraHasBeenStopped();
			this.output.reset();
			runner.run(assertDeleteKeyspace());
			assertCassandraHasBeenStopped();
		}
		finally {
			IOUtils.closeQuietly(() -> FileUtils.delete(this.factory.getWorkingDirectory()));
		}
	}


	private void runAndAssertCassandraListenInterface(String location, boolean ipv6) throws IOException {
		Path configurationFile = this.temporaryFolder.newFile("cassandra.yaml").toPath();
		String interfaceName = getInterface(ipv6);
		InetAddress address = NetworkUtils.getAddressByInterface(interfaceName, ipv6);
		String yaml;
		try (InputStream stream = getClass().getResourceAsStream(location)) {
			yaml = new String(IOUtils.toByteArray(stream), StandardCharsets.UTF_8)
					.replaceAll("#seed", address.getHostAddress())
					.replaceAll("#interface", interfaceName);
		}
		Files.copy(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), configurationFile,
				StandardCopyOption.REPLACE_EXISTING);

		this.factory.getJvmOptions().add("-Djava.net.preferIPv4Stack=" + !ipv6);
		this.factory.setConfigurationFile(configurationFile.toUri().toURL());

		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(assertCreateKeyspace()
				.andThen(assertBusyPort(Settings::getRealListenAddress, Settings::getStoragePort)));
		assertDirectoryHasBeenDeletedCorrectly();
		assertCassandraHasBeenStopped();
	}

	private void assertDirectoryHasBeenDeletedCorrectly() {
		assertThat(this.output.toString()).contains("Delete recursively working");
		assertThat(this.output.toString()).doesNotContain("has not been deleted");
	}

	private void assertCassandraHasBeenStopped() {
		assertThat(this.output.toString()).contains("MessagingService has terminated");
	}

	private Consumer<Cassandra> assertDeleteKeyspace() {
		return new CqlAssert("DROP KEYSPACE test");
	}

	private Consumer<Cassandra> assertCreateKeyspace() {
		return new CqlAssert("CREATE KEYSPACE test" +
				" WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}");
	}

	private Consumer<Cassandra> assertBusyPort(Function<Settings, InetAddress> addressMapper,
			Function<Settings, Integer> portMapper) {
		return new PortBusyAssert(addressMapper, portMapper);
	}

	private String getInterface(boolean ipv6) throws SocketException {
		Predicate<InetAddress> test = ipv6 ? Inet6Address.class::isInstance : Inet4Address.class::isInstance;
		return Collections.list(NetworkInterface.getNetworkInterfaces())
				.stream()
				.filter(it -> Collections.list(it.getInetAddresses())
						.stream()
						.filter(InetAddress::isLoopbackAddress)
						.anyMatch(test))
				.map(NetworkInterface::getName)
				.findFirst()
				.orElseThrow(IllegalStateException::new);
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
					.addContactPoints(settings.getRealAddress())
					.build();
		}
	}

	private static final class PortBusyAssert implements Consumer<Cassandra> {

		@Nonnull
		private final Function<Settings, InetAddress> addressMapper;

		@Nonnull
		private final Function<Settings, Integer> portMapper;

		PortBusyAssert(@Nonnull Function<Settings, InetAddress> addressMapper,
				@Nonnull Function<Settings, Integer> portMapper) {
			this.addressMapper = addressMapper;
			this.portMapper = portMapper;
		}


		@Override
		public void accept(@Nonnull Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			InetAddress address = this.addressMapper.apply(settings);
			Integer port = this.portMapper.apply(settings);
			assertThat(PortUtils.isPortBusy(address, port))
					.describedAs("Port (%s) is not busy", port)
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
