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

package com.github.nosan.embedded.cassandra.local;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraException;
import com.github.nosan.embedded.cassandra.CassandraRunner;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.lang.annotation.Nullable;
import com.github.nosan.embedded.cassandra.test.CqlSessionFactory;
import com.github.nosan.embedded.cassandra.util.MDCThreadFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Abstract test-class for {@link LocalCassandra}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings("ConstantConditions")
abstract class AbstractLocalCassandraTests {

	private final LocalCassandraFactory factory;

	@Nullable
	private Path temporaryFolder;

	AbstractLocalCassandraTests(Version version) {
		this.factory = new LocalCassandraFactory();
		this.factory.setVersion(version);
		LoggerFactory.getLogger(getClass());
	}

	@BeforeEach
	void setUp(@TempDir Path temporaryFolder, TestInfo testInfo) {
		this.temporaryFolder = temporaryFolder;
		String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("");
		String methodName = testInfo.getTestMethod().map(Method::getName).orElse("");
		MDC.put("TEST", String.format("%s.%s", className, methodName));
	}

	@AfterEach
	void tearDown() {
		MDC.remove("TEST");
	}

	@Test
	void shouldOverrideConfigurationFileWithJvmOptions() {
		LocalCassandraFactory factory = this.factory;
		List<String> jvmOptions = factory.getJvmOptions();
		factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		jvmOptions.add("-Dcassandra.native_transport_port=0");
		jvmOptions.add("-Dcassandra.rpc_port=9155");
		jvmOptions.add("-Dcassandra.storage_port=7003");
		jvmOptions.add("-Dcassandra.start_rpc=true");
		jvmOptions.add("-Dcassandra.start_native_transport=true");
		CassandraRunner runner = new CassandraRunner(factory);
		runner.run(assertCreateKeyspace().andThen(cassandra -> {
			Settings settings = cassandra.getSettings();
			assertThat(SocketUtils.connect(settings.address().orElseGet(SocketUtils::getLocalhost), 9155));
		}));
	}

	@Test
	void shouldOverrideConfigurationFileWithPorts() {
		LocalCassandraFactory factory = this.factory;
		factory.setPort(0);
		factory.setRpcPort(0);
		factory.setSslStoragePort(0);
		factory.setJmxLocalPort(0);
		factory.setStoragePort(0);
		CassandraRunner runner = new CassandraRunner(factory);
		runner.run(assertCreateKeyspace());
	}

	@Test
	void shouldFailStartupTimeoutSmall() {
		this.factory.setStartupTimeout(Duration.ofSeconds(3));
		assertThatThrownBy(new CassandraRunner(this.factory)::run).isInstanceOf(CassandraException.class)
				.hasStackTraceContaining("Try to increase a startup timeout");
	}

	@Test
	void shouldFailAndCatchError() {
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-invalid.yaml"));
		assertThatThrownBy(new CassandraRunner(this.factory)::run).isInstanceOf(CassandraException.class)
				.hasStackTraceContaining("invalid_property");
	}

	@Test
	void shouldFailCassandraUseSamePorts() {
		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(cassandra -> assertThatThrownBy(new CassandraRunner(this.factory)::run)
				.isInstanceOf(CassandraException.class).hasCauseInstanceOf(IOException.class));
	}

	@Test
	void shouldStartIfTransportDisabled() {
		this.factory.setStoragePort(8555);
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-transport.yaml"));
		new CassandraRunner(this.factory)
				.run(cassandra -> assertThat(SocketUtils.connect(InetAddress.getLoopbackAddress(), 8555))
						.describedAs("Storage port is not busy")
						.isTrue());
	}

	@Test
	void shouldStartOnInterfaceIPV4() throws Exception {
		shouldStartCassandraOnInterface("/cassandra-interface.yaml", false);
	}

	@Test
	void shouldStartOnInterfaceIPV6() throws Exception {
		shouldStartCassandraOnInterface("/cassandra-interface-ipv6.yaml", true);
	}

	@Test
	void shouldOverrideJavaHome() throws IOException {
		Path javaHome = this.temporaryFolder.resolve("JAVA_HOME");
		Files.createDirectories(javaHome);
		this.factory.setJavaHome(javaHome);
		assertThatThrownBy(new CassandraRunner(this.factory)::run).isInstanceOf(CassandraException.class);
	}

	@Test
	void shouldStartOnSsl() throws URISyntaxException {
		Assumptions.assumeTrue(this.factory.getVersion().getMajor() >= 3,
				"Native SSL transport is available since 3.X.X");

		Path keystoreFile = Paths.get(getClass().getResource("/cert/keystore.node0").toURI());
		Path truststoreFile = Paths.get(getClass().getResource("/cert/truststore.node0").toURI());
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-ssl.yaml"));
		this.factory.getWorkingDirectoryCustomizers().add((workDir, v) -> {
			Files.copy(keystoreFile, workDir.resolve("conf").resolve("keystore.node0"));
			Files.copy(truststoreFile, workDir.resolve("conf").resolve("truststore.node0"));
		});
		CassandraRunner runner = new CassandraRunner(this.factory);
		CqlSessionFactory sessionFactory = new CqlSessionFactory();
		sessionFactory.setSslEnabled(true);
		sessionFactory.setKeystorePath(keystoreFile);
		sessionFactory.setTruststorePath(truststoreFile);
		sessionFactory.setHostnameValidation(false);
		sessionFactory.setTruststorePassword("cassandra");
		sessionFactory.setKeystorePassword("cassandra");
		runner.run(assertCreateKeyspace(sessionFactory));
	}

	@Test
	void shouldStartStopOnlyOnce() {
		Cassandra cassandra = this.factory.create();
		assertThat(cassandra.getState()).isEqualTo(Cassandra.State.NEW);
		CassandraRunner runner = new CassandraRunner(cassandra);
		runner.run(assertCreateKeyspace().andThen(c -> {
			assertThat(cassandra.getState()).isEqualTo(Cassandra.State.STARTED);
			cassandra.start();
		}));
		assertThat(cassandra.getState()).isEqualTo(Cassandra.State.STOPPED);
		cassandra.stop();
	}

	@Test
	void shouldNotGetSettings() {
		assertThatThrownBy(() -> this.factory.create().getSettings())
				.hasStackTraceContaining("is not running")
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void shouldStartMoreThanOneCassandra() throws Throwable {
		this.factory.setJmxLocalPort(0);
		this.factory.setConfigurationFile(getClass().getResource("/cassandra-random.yaml"));
		ExecutorService executorService = Executors.newFixedThreadPool(2, new MDCThreadFactory("test"));
		List<Future<?>> futures = new ArrayList<>();
		try {
			for (int i = 0; i < 2; i++) {
				futures.add(executorService.submit(() -> {
					CassandraRunner runner = new CassandraRunner(this.factory);
					runner.run(assertCreateKeyspace());
				}));
			}
			for (Future<?> future : futures) {
				future.get(5, TimeUnit.MINUTES);
			}
		}
		finally {
			executorService.shutdown();
		}
	}

	@Test
	void shouldDeleteWorkingDirectory() {
		CassandraRunner runner = new CassandraRunner(this.factory.create());
		runner.run(assertCreateKeyspace());
		runner.run(assertCreateKeyspace());
	}

	@Test
	void shouldNotDeleteWorkingDirectory() {
		Path path = this.temporaryFolder.resolve(UUID.randomUUID().toString());
		this.factory.setWorkingDirectory(path);
		this.factory.setDeleteWorkingDirectory(false);
		CassandraRunner runner = new CassandraRunner(this.factory.create());
		runner.run(assertCreateKeyspace());
		runner.run(assertDeleteKeyspace());
	}

	private static InetAddress getAddressByInterface(String interfaceName, boolean useIpv6) {
		Objects.requireNonNull(interfaceName, "Interface name must not be null");
		Predicate<InetAddress> condition = useIpv6 ? Inet6Address.class::isInstance : Inet4Address.class::isInstance;
		return getAddressesByInterface(interfaceName).stream().filter(condition).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Can not find an address for %s and IPv6 : %s", interfaceName, useIpv6)));
	}

	private static List<InetAddress> getAddressesByInterface(String interfaceName) {
		Objects.requireNonNull(interfaceName, "Interface name must not be null");
		try {
			NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
			if (networkInterface == null) {
				throw new SocketException(String.format("'%s' interface is not valid", interfaceName));
			}
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			return Collections.unmodifiableList(Collections.list(addresses));
		}
		catch (SocketException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static Consumer<Cassandra> assertDeleteKeyspace() {
		return assertDeleteKeyspace(new CqlSessionFactory());
	}

	private static Consumer<Cassandra> assertCreateKeyspace() {
		return assertCreateKeyspace(new CqlSessionFactory());
	}

	private static Consumer<Cassandra> assertDeleteKeyspace(CqlSessionFactory sessionFactory) {
		return new CqlAssert("DROP KEYSPACE test", sessionFactory);
	}

	private static Consumer<Cassandra> assertCreateKeyspace(CqlSessionFactory sessionFactory) {
		return new CqlAssert(
				"CREATE KEYSPACE test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1}",
				sessionFactory);
	}

	private static String getInterface(boolean ipv6) throws SocketException {
		Predicate<InetAddress> test = ipv6 ? Inet6Address.class::isInstance : Inet4Address.class::isInstance;
		return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
				.filter(it -> Collections.list(it.getInetAddresses()).stream().filter(InetAddress::isLoopbackAddress)
						.anyMatch(test)).map(NetworkInterface::getName).findFirst()
				.orElseThrow(IllegalStateException::new);
	}

	private void shouldStartCassandraOnInterface(String location, boolean ipv6) throws IOException {
		Path configurationFile = this.temporaryFolder.resolve("cassandra.yaml");
		String interfaceName = getInterface(ipv6);
		InetAddress address = getAddressByInterface(interfaceName, ipv6);
		String yaml;
		try (InputStream stream = getClass().getResourceAsStream(location)) {
			yaml = new String(IOUtils.toByteArray(stream), StandardCharsets.UTF_8)
					.replaceAll("#seed", address.getHostAddress()).replaceAll("#interface", interfaceName);
		}
		Files.copy(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), configurationFile,
				StandardCopyOption.REPLACE_EXISTING);

		this.factory.getJvmOptions().add("-Djava.net.preferIPv4Stack=" + !ipv6);
		this.factory.setConfigurationFile(configurationFile.toUri().toURL());

		CassandraRunner runner = new CassandraRunner(this.factory);
		runner.run(assertCreateKeyspace());
	}

	private static final class CqlAssert implements Consumer<Cassandra> {

		private final String statement;

		private final CqlSessionFactory sessionFactory;

		CqlAssert(String statement, CqlSessionFactory sessionFactory) {
			this.statement = statement;
			this.sessionFactory = sessionFactory;
		}

		@Override
		public void accept(Cassandra cassandra) {
			try (CqlSession session = this.sessionFactory.create(cassandra.getSettings())) {
				assertThat(session.execute(this.statement).wasApplied())
						.describedAs("Statement '%s' is not applied", this.statement).isTrue();
			}
		}

	}

}
