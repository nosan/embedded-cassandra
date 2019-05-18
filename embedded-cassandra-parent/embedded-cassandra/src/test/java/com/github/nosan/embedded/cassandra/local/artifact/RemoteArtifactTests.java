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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutputExtension;
import com.github.nosan.embedded.cassandra.test.support.HttpServerExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RemoteArtifact}.
 *
 * @author Dmytro Nosan
 */
@SuppressWarnings({"NullableProblems", "ConstantConditions"})
@ExtendWith({HttpServerExtension.class, CaptureOutputExtension.class})
class RemoteArtifactTests {

	private static final Version VERSION = Version.parse("3.1.1");

	private RemoteArtifactFactory factory;

	private HttpServer httpServer;

	private CaptureOutput output;

	@BeforeEach
	void setUp(HttpServer httpServer, CaptureOutput output) {
		this.factory = new RemoteArtifactFactory();
		this.factory.setUrlFactory(version -> new URL[]{
				new URL(String.format("http:/%s/dist/apache-cassandra-%s.zip", httpServer.getAddress(), version))});
		this.output = output;
		this.httpServer = httpServer;

	}

	@Test
	void shouldDownloadArtifactProgress() throws Exception {
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, content.length);
			for (int i = 0; i < content.length; i += 8192) {
				exchange.getResponseBody().write(content, i, Math.min(8192, content.length - i));
				sleep(100);
			}
			exchange.close();
		});
		Artifact artifact = this.factory.create(VERSION);
		Path archive = artifact.getArchive();
		assertThat(this.output.toString()).contains("Downloaded");
		assertThat(archive).exists();
		assertThat(archive.toString()).endsWith("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);

	}

	@Test
	void shouldDownloadArtifactNoProgress() throws Exception {
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		Artifact artifact = this.factory.create(VERSION);
		Path archive = artifact.getArchive();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists();
		assertThat(archive.toString()).endsWith("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	void shouldDownloadArtifactUsingRedirection() throws Exception {
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		this.httpServer.createContext("/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.getResponseHeaders().put("Location", Collections.singletonList("/apache-cassandra-3.1.1.zip"));
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
			exchange.close();
		});
		Artifact artifact = this.factory.create(VERSION);
		Path archive = artifact.getArchive();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists();
		assertThat(archive.toString()).endsWith("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	void shouldNotDownloadArtifactMaxRedirection() {
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.getResponseHeaders()
					.put("Location", Collections.singletonList("/dist/apache-cassandra-3.1.1.zip"));
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
			exchange.close();
		});
		Artifact artifact = this.factory.create(VERSION);
		assertThatThrownBy(artifact::getArchive).hasStackTraceContaining("Too many redirects for URL");
	}

	@Test
	void shouldDownloadArtifactFromMultiplyUrls() throws Exception {
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		UrlFactory delegate = this.factory.getUrlFactory();
		this.factory.setUrlFactory(version -> Stream
				.concat(Stream.of(new URL(String.format("http:/%s/cassandra.zip", this.httpServer.getAddress()))),
						Arrays.stream((delegate).create(version))).toArray(URL[]::new));
		Artifact artifact = this.factory.create(VERSION);
		Path archive = artifact.getArchive();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists();
		assertThat(archive.toString()).endsWith("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	void shouldNotDownloadArtifactNotFound() {
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(404, 0);
			exchange.close();
		});
		assertThatThrownBy(() -> this.factory.create(VERSION).getArchive())
				.hasStackTraceContaining("HTTP (404 Not Found) status for URL");
	}

	@Test
	void shouldNotDownloadArtifactNoURLs() {
		this.factory.setUrlFactory(version -> new URL[0]);
		assertThatThrownBy(() -> this.factory.create(VERSION).getArchive())
				.isInstanceOf(IOException.class);
	}

	@Test
	void shouldNotDownloadArtifactReadTimeout() {
		this.httpServer.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> sleep(600));

		this.factory.setReadTimeout(Duration.ofMillis(200));
		assertThatThrownBy(() -> this.factory.create(VERSION).getArchive())
				.hasStackTraceContaining("Read timed out");
	}

	@Test
	void shouldNotDownloadArtifactConnectionTimeout() {
		this.factory.setUrlFactory(version -> new URL[]{new URL("http://example.com:81/apache-cassandra-3.1.1.zip")});
		this.factory.setConnectTimeout(Duration.ofSeconds(1));

		assertThatThrownBy(() -> this.factory.create(VERSION).getArchive())
				.hasStackTraceContaining("connect timed out");
	}

	@Test
	void shouldNotDownloadArtifactInvalidProxy() {
		this.factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(1111)));
		assertThatThrownBy(() -> this.factory.create(VERSION).getArchive())
				.hasStackTraceContaining("Connection refused");

	}

	@Test
	void shouldNotDownloadArtifactNoFileName() {
		this.httpServer.createContext("/", exchange -> exchange.sendResponseHeaders(200, 0));
		this.factory.setUrlFactory(version -> new URL[]{new URL(String
				.format("http://%s:%d/", this.httpServer.getAddress().getHostName(),
						this.httpServer.getAddress().getPort()))});
		assertThatThrownBy(() -> this.factory.create(VERSION).getArchive())
				.hasStackTraceContaining("There is no way to determine");

	}

	private static void sleep(long timeout) {
		try {
			Thread.sleep(timeout);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

	}

}
