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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraRunner;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.DefaultClusterFactory;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutputExtension;
import com.github.nosan.embedded.cassandra.util.FileUtils;
import com.github.nosan.embedded.cassandra.util.NetworkUtils;
import com.github.nosan.embedded.cassandra.util.PortUtils;
import com.github.nosan.embedded.cassandra.util.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Abstract test-class for {@link LocalCassandra}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("ConstantConditions")
@ExtendWith(CaptureOutputExtension.class)
abstract class AbstractLocalCassandraTests {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final LocalCassandraFactory factory;

	@Nullable
	private Path temporaryFolder;

	@Nullable
	private CaptureOutput output;

	AbstractLocalCassandraTests(Version version) {
		this.factory = new LocalCassandraFactory();
		this.factory.setVersion(version);
	}

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder, CaptureOutput captureOutput) {
		this.temporaryFolder = temporaryFolder;
		this.output = captureOutput;
	}

	@Test
	void shouldAddJvmOptions() {
		LocalCassandraFactory factory = this.factory;
		List<String> jvmOptions = factory.getJvmOptions();
		factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		jvmOptions.add("-Dcassandra.native_transport_port=0");
		jvmOptions.add("-Dcassandra.rpc_port=9155");
		jvmOptions.add("-Dcassandra.storage_port=7003");
		jvmOptions.add("-Dcassandra.start_rpc=true");
		jvmOptions.add("-Dcassandra.start_native_transport=true");
		CassandraRunner runner = new CassandraRunner(factory);
		runner.run(assertBusyPort(Settings::getRealAddress, (settings -> 9155))
				.andThen(assertBusyPort(Settings::getRealListenAddress, (settings -> 7003))
						.andThen(assertCreateKeyspace())));
	}

	@Test
	void shouldCatchCassandraError() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-invalid.yaml"));
		assertThatThrownBy(new CassandraRunner(this.factory)::run).isInstanceOf(CassandraException.class)
				.hasMessageNotContaining("invalid_property");
	}

	@Test
	void shouldFailCassandraUseSamePorts() {
		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(cassandra -> assertThatThrownBy(new CassandraRunner(this.factory)::run)
				.isInstanceOf(CassandraException.class)
				.hasCauseInstanceOf(IOException.class));
	}

	@Test
	void shouldStartCassandraNoOutput() {
		this.factory.setLogbackFile(getClass().getResource("/logback-empty.xml"));
		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(assertCreateKeyspace());
	}

	@Test
	void shouldStartCassandraNoOutputAndNotTransportWorseCase() {
		this.factory.setLogbackFile(getClass().getResource("/logback-empty.xml"));
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(assertBusyPort(Settings::getRealListenAddress, Settings::getStoragePort));
	}

	@Test
	void shouldStartIfTransportDisabled() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		new CassandraRunner(this.factory).run(assertBusyPort(Settings::getRealListenAddress, Settings::getStoragePort));
	}

	@Test
	void shouldRunOnInterfaceIPV4() throws Exception {
		runAndAssertCassandraListenInterface("/cassandra-interface.yaml", false);
	}

	@Test
	void shouldRunOnInterfaceIPV6() throws Exception {
		runAndAssertCassandraListenInterface("/cassandra-interface-ipv6.yaml", true);
	}

	@Test
	void notEnoughTime() {
		this.factory.setStartupTimeout(Duration.ofSeconds(2L));
		assertThatThrownBy(new CassandraRunner(this.factory)::run).isInstanceOf(CassandraException.class)
				.hasStackTraceContaining("has not been started, seems like (2000) milliseconds is not enough");
	}

	@Test
	void shouldOverrideJavaHome() {
		this.factory.setJavaHome(Paths.get(UUID.randomUUID().toString()));
		assertThatThrownBy(new CassandraRunner(this.factory)::run)
				.isInstanceOf(CassandraException.class);
	}

	@Test
	void shouldStartStopOnlyOnce() {
		Cassandra cassandra = this.factory.create();
		assertThat(cassandra.getState()).isEqualTo(Cassandra.State.NEW);
		CassandraRunner runner = new CassandraRunner(cassandra);
		runner.run(assertCreateKeyspace().andThen(unused -> {
			assertThat(cassandra.getState()).isEqualTo(Cassandra.State.STARTED);
			assertThat(this.output.toString()).contains("Starts Apache Cassandra");
			this.output.reset();
			cassandra.start();
			assertThat(this.output.toString()).doesNotContain("Starts Apache Cassandra");
		}));
		assertThat(cassandra.getState()).isEqualTo(Cassandra.State.STOPPED);
		assertThat(this.output.toString()).contains("Stops Apache Cassandra");
		this.output.reset();
		cassandra.stop();
		assertThat(this.output.toString()).doesNotContain("Stops Apache Cassandra");
	}

	@Test
	void shouldNotGetSettings() {
		assertThatThrownBy(() -> this.factory.create().getSettings())
				.hasStackTraceContaining("Please start it before calling this method")
				.isInstanceOf(CassandraException.class);
	}

	@Test
	void shouldRunMoreThanOneCassandra() throws Throwable {
		this.factory.setJmxPort(0);
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-random.yaml"));
		List<Throwable> exceptions = new CopyOnWriteArrayList<>();
		Runnable runnable = () -> {
			try {
				CassandraRunner runner = new CassandraRunner(this.factory);
				runner.run(assertCreateKeyspace());
			}
			catch (Throwable ex) {
				exceptions.add(ex);
			}
		};
		Thread t = new Thread(runnable);
		Thread t1 = new Thread(runnable);
		t.start();
		t1.start();
		t.join();
		t1.join();

		exceptions.forEach(exception -> this.log.error(exception.getMessage(), exception));
		assertThat(exceptions).isEmpty();

	}

	@Test
	void shouldReInitializedCassandra() throws IOException {
		Path path = this.temporaryFolder.resolve(UUID.randomUUID().toString());
		this.factory.setWorkingDirectory(path);
		Cassandra cassandra = this.factory.create();
		try {
			cassandra.start();
			assertCreateKeyspace().accept(cassandra);
		}
		finally {
			cassandra.stop();
		}
		FileUtils.delete(path);
		this.output.reset();
		try {
			cassandra.start();
			assertCreateKeyspace().accept(cassandra);
		}
		finally {
			cassandra.stop();
		}
	}

	@Test
	void shouldInitializedOnlyOnce() {
		Path path = this.temporaryFolder.resolve(UUID.randomUUID().toString());
		this.factory.setWorkingDirectory(path);
		Cassandra cassandra = this.factory.create();
		try {
			cassandra.start();
			assertCreateKeyspace().accept(cassandra);
		}
		finally {
			cassandra.stop();
		}
		this.output.reset();
		try {
			cassandra.start();
			assertDeleteKeyspace().accept(cassandra);
		}
		finally {
			cassandra.stop();
		}
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void shouldPassAllowRootIfNecessary() {
		LocalCassandraFactory factory = this.factory;
		Version version = factory.getVersion();
		factory.setAllowRoot(true);
		CassandraRunner runner = new CassandraRunner(factory);
		runner.run(assertCreateKeyspace());
		if ((version.getMajor() > 3 || (version.getMajor() == 3 && version.getMinor() > 1))) {
			assertThat(this.output.toString()).contains("-f -R");
		}
		else {
			assertThat(this.output.toString()).doesNotContain("-f -R");
		}
	}

	private void runAndAssertCassandraListenInterface(String location,
			boolean ipv6) throws IOException {
		Path configurationFile = this.temporaryFolder.resolve("cassandra.yaml");
		String interfaceName = getInterface(ipv6);
		InetAddress address = NetworkUtils.getAddressByInterface(interfaceName, ipv6)
				.orElseThrow(IllegalStateException::new);
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

		private final String statement;

		CqlAssert(String statement) {
			this.statement = statement;
		}

		@Override
		public void accept(Cassandra cassandra) {
			try (Cluster cluster = cluster(cassandra)) {
				Session session = cluster.connect();
				assertThat(session.execute(this.statement).wasApplied())
						.describedAs("Statement '%s' is not applied", this.statement).isTrue();
			}
		}

		private static Cluster cluster(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			return new DefaultClusterFactory().create(settings);
		}

	}

	private static final class PortBusyAssert implements Consumer<Cassandra> {

		private final Function<Settings, InetAddress> addressMapper;

		private final Function<Settings, Integer> portMapper;

		PortBusyAssert(Function<Settings, InetAddress> addressMapper,
				Function<Settings, Integer> portMapper) {
			this.addressMapper = addressMapper;
			this.portMapper = portMapper;
		}

		@Override
		public void accept(Cassandra cassandra) {
			Settings settings = cassandra.getSettings();
			InetAddress address = this.addressMapper.apply(settings);
			Integer port = this.portMapper.apply(settings);
			assertThat(PortUtils.isPortBusy(address, port))
					.describedAs("Port '%s' is not busy", port)
					.isTrue();
		}

	}

}
