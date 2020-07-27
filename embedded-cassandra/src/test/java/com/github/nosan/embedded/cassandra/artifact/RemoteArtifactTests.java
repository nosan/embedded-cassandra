/*
 * Copyright 2018-2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.artifact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.commons.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RemoteArtifact}.
 *
 * @author Dmytro Nosan
 */
class RemoteArtifactTests {

	private static final Version VERSION = Version.of("4.0-beta1");

	private final ByteArrayOutputStream output = new ByteArrayOutputStream();

	private final TeeOutputStream out = new TeeOutputStream(System.out, this.output);

	private final HttpServer httpServer = createHttpServer();

	@BeforeEach
	void setUp() throws Exception {
		System.setOut(new PrintStream(this.out));
		this.httpServer.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		this.httpServer.start();
		byte[] content;
		try (InputStream inputStream = new ClassPathResource("apache-cassandra-4.0-beta1-bin.tar.gz")
				.getInputStream()) {
			content = IOUtils.toByteArray(inputStream);
		}
		this.httpServer.createContext("/apache-cassandra-4.0-beta1-bin.tar.gz", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, content.length);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		this.httpServer.createContext("/dist/apache-cassandra-4.0-beta1-bin.tar.gz", exchange -> {
			exchange.getResponseHeaders().put("Location",
					Collections.singletonList("/apache-cassandra-4.0-beta1-bin.tar.gz"));
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, content.length);
			exchange.close();
		});
	}

	@AfterEach
	void tearDown() {
		this.output.reset();
		this.httpServer.stop(0);
		System.setOut(this.out.original);
	}

	@Test
	void shouldDownloadArtifactProgress(@TempDir Path temporaryFolder) throws Exception {
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setUrlFactory(version -> Collections.singletonList(
				new URL(String
						.format("http:/%s/apache-cassandra-4.0-beta1-bin.tar.gz", this.httpServer.getAddress()))));
		assertDistribution(artifact.getDistribution());
		assertThat(this.output.toString()).contains("Downloaded");
		this.output.reset();
		assertDistribution(artifact.getDistribution());
		assertThat(this.output.toString()).doesNotContain("Downloaded");
	}

	@Test
	void shouldDownloadArtifactRedirection(@TempDir Path temporaryFolder) throws Exception {
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setUrlFactory(version -> Collections.singletonList(new URL(String
				.format("http:/%s/dist/apache-cassandra-4.0-beta1-bin.tar.gz", this.httpServer.getAddress()))));
		assertDistribution(artifact.getDistribution());
		assertThat(this.output.toString()).contains("Downloaded");
	}

	@Test
	void shouldNotDownloadArtifactMaxRedirection(@TempDir Path temporaryFolder) throws Exception {
		this.httpServer.createContext("/dist/apache-cassandra-4.0-beta1.zip", exchange -> {
			exchange.getResponseHeaders().put("Location",
					Collections.singletonList("/dist/apache-cassandra-4.0-beta1.zip"));
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
			exchange.close();
		});
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setUrlFactory(version -> Collections.singletonList(
				new URL(String.format("http:/%s/dist/apache-cassandra-4.0-beta1.zip", this.httpServer.getAddress()))));
		assertThatThrownBy(artifact::getDistribution).hasStackTraceContaining("Too many redirects for URL");
	}

	@Test
	void shouldNotDownloadArtifactNotFound(@TempDir Path temporaryFolder) throws Exception {
		this.httpServer.createContext("/dist/apache-cassandra-4.0-beta1.zip", exchange -> {
			exchange.sendResponseHeaders(404, 0);
			exchange.close();
		});
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setUrlFactory(version -> Collections.singletonList(
				new URL(String.format("http:/%s/dist/apache-cassandra-4.0-beta1.zip", this.httpServer.getAddress()))));
		assertThatThrownBy(artifact::getDistribution).hasStackTraceContaining("HTTP Status '404' is invalid for URL");
	}

	@Test
	void shouldNotDownloadArtifactReadTimeout(@TempDir Path temporaryFolder) throws Exception {
		this.httpServer.createContext("/dist/apache-cassandra-4.0-beta1.zip", exchange -> {
			try {
				Thread.sleep(600);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			exchange.close();
		});
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setReadTimeout(Duration.ofMillis(200));
		artifact.setUrlFactory(version -> Collections.singletonList(
				new URL(String.format("http:/%s/dist/apache-cassandra-4.0-beta1.zip", this.httpServer.getAddress()))));
		assertThatThrownBy(artifact::getDistribution).hasStackTraceContaining("Read timed out");
	}

	@Test
	void shouldNotDownloadArtifactConnectionTimeout(@TempDir Path temporaryFolder) throws Exception {
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setConnectTimeout(Duration.ofSeconds(1));
		artifact.setUrlFactory(
				version -> Collections.singletonList(new URL("http://example.com:81/apache-cassandra-4.0-beta1.zip")));
		assertThatThrownBy(artifact::getDistribution).hasStackTraceContaining("connect timed out");
	}

	@Test
	void shouldNotDownloadArtifactInvalidProxy(@TempDir Path temporaryFolder) throws Exception {
		RemoteArtifact artifact = new RemoteArtifact(VERSION);
		artifact.setDestination(temporaryFolder);
		artifact.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(1111)));
		artifact.setUrlFactory(version -> Collections.singletonList(
				new URL(String
						.format("http:/%s/apache-cassandra-4.0-beta1-bin.tar.gz", this.httpServer.getAddress()))));
		assertThatThrownBy(artifact::getDistribution).hasStackTraceContaining("Connection refused");
	}

	private void assertDistribution(Artifact.Distribution distribution) {
		Path directory = distribution.getDirectory();
		assertThat(distribution.getVersion()).isEqualTo(VERSION);
		assertThat(directory.resolve("bin")).exists();
		assertThat(directory.resolve("lib")).exists();
		assertThat(directory.resolve("conf")).exists();
	}

	private static HttpServer createHttpServer() {
		try {
			return HttpServer.create();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static final class TeeOutputStream extends OutputStream {

		private final PrintStream original;

		private final OutputStream delegate;

		TeeOutputStream(PrintStream original, OutputStream delegate) {
			this.original = original;
			this.delegate = delegate;
		}

		@Override
		public void write(int b) throws IOException {
			this.delegate.write(b);
			this.original.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.delegate.write(b, off, len);
			this.original.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
			this.original.flush();
		}

	}

}
