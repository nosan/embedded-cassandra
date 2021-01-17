/*
 * Copyright 2020-2021 the original author or authors.
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

package com.github.nosan.embedded.cassandra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.WebCassandraDirectoryProvider.CassandraPackage;
import com.github.nosan.embedded.cassandra.commons.ClassPathResource;
import com.github.nosan.embedded.cassandra.commons.StreamUtils;
import com.github.nosan.embedded.cassandra.commons.web.JdkHttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Tests for {@link WebCassandraDirectoryProvider}.
 *
 * @author Dmytro Nosan
 */
class WebCassandraDirectoryProviderTests {

	private static HttpServer httpServer;

	private static JdkHttpClient httpClient;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

	private PrintStream stdout;

	private PrintStream stderr;

	private WebCassandraDirectoryProvider directoryProvider;

	@BeforeAll
	static void beforeAll() throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(0), 0);
		httpServer.createContext("/", exchange -> {
			String uri = exchange.getRequestURI().toString();
			ClassPathResource resource = new ClassPathResource(uri.substring(uri.lastIndexOf('/')));
			long size = Files.size(Paths.get(resource.toURI()));
			exchange.sendResponseHeaders(200, size);
			try (InputStream inputStream = resource.getInputStream()) {
				StreamUtils.copy(inputStream, exchange.getResponseBody());
			}
			finally {
				exchange.close();
			}
		});
		httpServer.setExecutor(Executors.newCachedThreadPool());
		httpServer.start();
		httpClient = new JdkHttpClient(Duration.ofSeconds(1), Duration.ofSeconds(1));
	}

	@AfterAll
	static void afterAll() {
		HttpServer server = httpServer;
		if (server != null) {
			server.stop(0);
			((ExecutorService) server.getExecutor()).shutdown();
		}
	}

	@BeforeEach
	void setUp(@TempDir Path root) {
		this.directoryProvider = spy(new WebCassandraDirectoryProvider(httpClient, root));
		this.stdout = System.out;
		this.stderr = System.err;
		System.setOut(new PrintStream(new TeeOutputStream(System.out, this.out)));
		System.setErr(new PrintStream(new TeeOutputStream(System.err, this.err)));
	}

	@AfterEach
	void tearDown() {
		System.setOut(this.stdout);
		System.setErr(this.stderr);
	}

	@RepeatedTest(10)
	void parallelDownloadAndExtract() throws Exception {
		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), Collections.singletonMap("SHA-512",
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz.sha512", getBaseUri(),
						version)))));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		CountDownLatch l1 = new CountDownLatch(1);
		Callable<Path> callable = () -> {
			l1.await();
			return this.directoryProvider.getDirectory(version);
		};
		List<Future<Path>> futures = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		List<Path> directories = new ArrayList<>();
		try {
			for (int i = 0; i < 5; i++) {
				futures.add(executorService.submit(callable));
			}
			l1.countDown();
			for (Future<Path> future : futures) {
				directories.add(future.get());
			}
			for (Path directory : directories) {
				assertDirectory(directory);
			}
			assertThat(this.out.toString()).containsOnlyOnce("Downloading");
			assertThat(this.out.toString()).containsOnlyOnce("Extracting");
		}
		finally {
			executorService.shutdown();
		}

	}

	@Test
	void failNoPackages() {
		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		assertThatThrownBy(() -> this.directoryProvider.getDirectory(version)).hasStackTraceContaining("No Packages");
	}

	@Test
	void failDownloadAndExtract() {

		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		Map<String, URI> checksums = new LinkedHashMap<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s.tgz", getBaseUri(),
						version)), checksums));
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s.tgz", getBaseUri(),
						version)), checksums));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		assertThatThrownBy(() -> this.directoryProvider.getDirectory(version)).hasStackTraceContaining(
				" Could not download a file");

	}

	@Test
	void failToMoveDownloadedShouldBeCopy(@TempDir Path root) throws IOException {
		Version version = Version.parse("4.0-beta4");

		Files.createDirectories(root.resolve(".embedded-cassandra")
				.resolve("cassandra")
				.resolve(version.toString())
				.resolve(String.format("apache-cassandra-%s", version)));

		List<CassandraPackage> packages = new ArrayList<>();
		Map<String, URI> checksums = new LinkedHashMap<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), checksums));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		Path directory = this.directoryProvider.getDirectory(version);

		assertDirectory(directory);

		assertThat(this.out.toString()).contains("Extracting");
		assertThat(this.out.toString()).contains("Downloading");
		assertThat(this.out.toString()).contains("Verifying checksum");
		assertThat(this.out.toString()).contains("No checksum defined for");
		assertThat(this.out.toString()).contains("100%");
	}

	@Test
	void failAndThenDownload() throws IOException {

		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		Map<String, URI> checksums = new LinkedHashMap<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s.tgz", getBaseUri(),
						version)), checksums));
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), checksums));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		Path directory = this.directoryProvider.getDirectory(version);

		assertDirectory(directory);

		assertThat(this.out.toString()).contains("Extracting");
		assertThat(this.out.toString()).contains("Downloading");
		assertThat(this.out.toString()).contains("Verifying checksum");
		assertThat(this.out.toString()).contains("No checksum defined for");
		assertThat(this.out.toString()).contains("100%");

	}

	@Test
	void downloadAndExtractSha1() throws IOException {

		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		Map<String, URI> checksums = new LinkedHashMap<>();
		checksums.put("SHA-256",
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz.sha256", getBaseUri(),
						version)));
		checksums.put("SHA-1", URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz.sha1", getBaseUri(),
				version)));
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), checksums));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		Path directory = this.directoryProvider.getDirectory(version);

		assertDirectory(directory);

		assertThat(this.out.toString()).contains("Extracting");
		assertThat(this.out.toString()).contains("Downloading");
		assertThat(this.out.toString()).contains("Verifying checksum");
		assertThat(this.out.toString()).contains("Checksums are identical");
		assertThat(this.out.toString()).contains("100%");
	}

	@Test
	void downloadAndExtractChecksumsAreNotPresent() throws IOException {
		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), new LinkedHashMap<>()));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		Path directory = this.directoryProvider.getDirectory(version);
		assertDirectory(directory);

		assertThat(this.out.toString()).contains("Extracting");
		assertThat(this.out.toString()).contains("Downloading");
		assertThat(this.out.toString()).contains("No checksum defined for");
		assertThat(this.out.toString()).contains("100%");
	}

	@Test
	void downloadAndExtractFailChecksumMismatch() {
		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), Collections.singletonMap("SHA-512",
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz.sha1", getBaseUri(),
						version)))));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		assertThatThrownBy(() -> this.directoryProvider.getDirectory(version))
				.hasStackTraceContaining("Checksum mismatch");

		assertThat(this.out.toString()).doesNotContain("Extracting");
		assertThat(this.out.toString()).contains("Downloading");
		assertThat(this.out.toString()).contains("Verifying checksum");
		assertThat(this.out.toString()).contains("100%");
		assertThat(this.out.toString()).doesNotContain("Checksums are identical");
	}

	@Test
	void downloadAndExtractCouldNotDownloadChecksum() throws IOException {
		Version version = Version.parse("4.0-beta4");

		List<CassandraPackage> packages = new ArrayList<>();
		packages.add(new CassandraPackage(String.format("apache-cassandra-%1$s-bin.tar.gz", version),
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz", getBaseUri(),
						version)), Collections.singletonMap("SHA-256",
				URI.create(String.format("%1$s/%2$s/apache-cassandra-%2$s-bin.tar.gz.sha256", getBaseUri(),
						version)))));
		doReturn(packages).when(this.directoryProvider).getCassandraPackages(version);

		Path directory = this.directoryProvider.getDirectory(version);
		assertDirectory(directory);

		assertThat(this.out.toString()).contains("Extracting");
		assertThat(this.out.toString()).contains("Downloading");
		assertThat(this.out.toString()).contains("Verifying checksum");
		assertThat(this.out.toString()).contains("100%");
		assertThat(this.out.toString()).doesNotContain("Checksums are identical");
		assertThat(this.out.toString()).contains("No checksum downloaded for");
	}

	@Test
	void directoryPresentJustReturn(@TempDir Path root) throws IOException {
		Version version = Version.parse("4.0-beta4");
		Path expected = Files.createDirectories(root.resolve(".embedded-cassandra")
				.resolve("cassandra")
				.resolve(version.toString())
				.resolve(String.format("apache-cassandra-%s", version)));

		Files.createDirectories(root.resolve(".embedded-cassandra")
				.resolve("cassandra")
				.resolve(version.toString()).resolve(".success"));

		Path actual = this.directoryProvider.getDirectory(version);

		assertThat(actual).isEqualTo(expected);
		assertThat(this.out.toString()).doesNotContain("Extracting");
		assertThat(this.out.toString()).doesNotContain("Downloading");
		assertThat(this.out.toString()).doesNotContain("Verifying checksum");
		assertThat(this.out.toString()).doesNotContain("Checksums are identical");
	}

	@Test
	void failToLock() throws IOException {
		doReturn(false).when(this.directoryProvider).tryLock(any());

		assertThatThrownBy(() -> this.directoryProvider.getDirectory(Version.parse("4.0-beta4")))
				.hasStackTraceContaining(" File lock could not be acquire");
	}

	@Test
	void construct1() {
		WebCassandraDirectoryProvider wcdp = new WebCassandraDirectoryProvider();
		assertThat(wcdp).extracting("httpClient").isInstanceOf(JdkHttpClient.class);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient.proxy", null);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient.readTimeout", null);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient.connectTimeout", null);
		assertThat(wcdp).hasFieldOrPropertyWithValue("downloadDirectory", Paths.get(System.getProperty("user.home")));
	}

	@Test
	void construct2() {
		JdkHttpClient httpClient = new JdkHttpClient();
		WebCassandraDirectoryProvider wcdp = new WebCassandraDirectoryProvider(httpClient);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient", httpClient);
		assertThat(wcdp).hasFieldOrPropertyWithValue("downloadDirectory", Paths.get(System.getProperty("user.home")));
	}

	@Test
	void construct3(@TempDir Path dir) {
		WebCassandraDirectoryProvider wcdp = new WebCassandraDirectoryProvider(dir);
		assertThat(wcdp).extracting("httpClient").isInstanceOf(JdkHttpClient.class);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient.proxy", null);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient.readTimeout", null);
		assertThat(wcdp).hasFieldOrPropertyWithValue("httpClient.connectTimeout", null);
		assertThat(wcdp).hasFieldOrPropertyWithValue("downloadDirectory", dir);
	}

	private static void assertDirectory(Path directory) {
		assertThat(directory.resolve("bin")).exists();
		assertThat(directory.resolve("lib")).exists();
		assertThat(directory.resolve("conf")).exists();
	}

	private static URI getBaseUri() {
		InetSocketAddress address = httpServer.getAddress();
		if (address.getAddress() instanceof Inet6Address) {
			return URI.create(String.format("http://[%s]:%d", address.getHostName(), address.getPort()));
		}
		return URI.create(String.format("http://%s:%d", address.getHostName(), address.getPort()));
	}

}
