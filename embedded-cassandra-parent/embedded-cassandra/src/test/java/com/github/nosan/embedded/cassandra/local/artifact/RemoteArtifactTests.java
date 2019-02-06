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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.WebServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RemoteArtifact}.
 *
 * @author Dmytro Nosan
 */
public class RemoteArtifactTests {

	@Rule
	public final WebServer webServer = new WebServer();

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public final CaptureOutput output = new CaptureOutput();

	private RemoteArtifactFactory factory;

	@Before
	public void setUp() throws Exception {
		Path directory = this.temporaryFolder.newFolder().toPath().resolve(UUID.randomUUID().toString());
		this.factory = new RemoteArtifactFactory();
		this.factory.setUrlFactory(version -> {
			HttpServer server = this.webServer.get();
			return new URL[]{
					new URL(String.format("http:/%s/dist/apache-cassandra-%s.zip", server.getAddress(), version))};
		});
		this.factory.setDirectory(directory);

	}

	@Test
	public void shouldDownloadArtifactAndShowProgress() throws Exception {
		HttpServer server = this.webServer.get();
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, content.length);
			for (int i = 0; i < content.length; i += 8192) {
				exchange.getResponseBody().write(content, i, Math.min(8192, content.length - i));
				sleep(100);
			}
			exchange.close();
		});
		Artifact artifact = this.factory.create(new Version(3, 1, 1));
		Path archive = artifact.get();
		assertThat(this.output.toString()).contains("Downloaded");
		assertThat(archive).exists().hasParent(this.factory.getDirectory());
		assertThat(archive).hasFileName("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);

	}

	@Test
	public void shouldDownloadArtifact() throws Exception {
		HttpServer server = this.webServer.get();
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		Artifact artifact = this.factory.create(new Version(3, 1, 1));
		Path archive = artifact.get();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists().hasParent(this.factory.getDirectory());
		assertThat(archive).hasFileName("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	public void shouldDownloadArtifactRedirection() throws Exception {
		HttpServer server = this.webServer.get();
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		server.createContext("/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.getResponseHeaders().put("Location", Collections.singletonList("/apache-cassandra-3.1.1.zip"));
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
			exchange.close();
		});
		Artifact artifact = this.factory.create(new Version(3, 1, 1));
		Path archive = artifact.get();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists().hasParent(this.factory.getDirectory());
		assertThat(archive).hasFileName("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	public void shouldDownloadArtifactURLs() throws Exception {
		HttpServer server = this.webServer.get();
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			exchange.getResponseBody().write(content);
			exchange.close();
		});
		UrlFactory delegate = this.factory.getUrlFactory();
		this.factory.setUrlFactory(new UrlFactory() {
			@Nonnull
			@Override
			public URL[] create(@Nonnull Version version) throws MalformedURLException {
				return Stream.concat(Stream.of(new URL(String.format("http:/%s/cassandra.zip", server.getAddress()))),
						Arrays.stream(delegate.create(version))).toArray(URL[]::new);
			}
		});
		Artifact artifact = this.factory.create(new Version(3, 1, 1));
		Path archive = artifact.get();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists().hasParent(this.factory.getDirectory());
		assertThat(archive).hasFileName("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	public void shouldNotDownloadArtifactIfExists() throws Exception {
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		Files.createDirectories(this.factory.getDirectory());
		Files.copy(new ByteArrayInputStream(content), this.factory.getDirectory().
				resolve("apache-cassandra-3.1.1.zip"));

		Artifact artifact = this.factory.create(new Version(3, 1, 1));
		Path archive = artifact.get();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(archive).exists().hasParent(this.factory.getDirectory());
		assertThat(archive).hasFileName("apache-cassandra-3.1.1.zip");
		assertThat(archive).hasBinaryContent(content);
	}

	@Test
	public void shouldNotDownloadInvalidStatus() {
		HttpServer server = this.webServer.get();
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(400, 0);
			exchange.close();
		});
		assertThatThrownBy(() -> this.factory.create(new Version(3, 1, 1)).get())
				.hasStackTraceContaining("HTTP (400 Bad Request) status for URL");
	}

	@Test
	public void urlListEmpty() {
		this.factory.setUrlFactory(version -> new URL[0]);
		assertThatThrownBy(() -> this.factory.create(new Version(3, 1, 1)).get())
				.isInstanceOf(IOException.class);
	}

	@Test
	public void readTimeoutIsExceeded() {
		HttpServer server = this.webServer.get();
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> sleep(600));

		this.factory.setReadTimeout(Duration.ofMillis(200));
		assertThatThrownBy(() -> this.factory.create(new Version(3, 1, 1)).get())
				.hasStackTraceContaining("Read timed out");
	}

	@Test
	public void connectTimeoutIsExceeded() {
		this.factory.setUrlFactory(new UrlFactory() {
			@Nonnull
			@Override
			public URL[] create(@Nonnull Version version) throws MalformedURLException {
				return new URL[]{new URL("http://example.com:81/apache-cassandra-3.1.1.zip")};
			}
		});
		this.factory.setConnectTimeout(Duration.ofSeconds(1));

		assertThatThrownBy(() -> this.factory.create(new Version(3, 1, 1)).get())
				.hasStackTraceContaining("connect timed out");
	}

	@Test
	public void proxyIsInvalid() {
		this.factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(1111)));
		assertThatThrownBy(() -> this.factory.create(new Version(3, 1, 1)).get())
				.hasStackTraceContaining("Connection refused");

	}

	@Test
	public void impossibleDetermineFileName() {
		HttpServer server = this.webServer.get();
		server.createContext("/", exchange -> exchange.sendResponseHeaders(200, 0));
		this.factory.setUrlFactory(new UrlFactory() {
			@Nonnull
			@Override
			public URL[] create(@Nonnull Version version) throws MalformedURLException {
				return new URL[]{new URL(String.format(
						"http://%s:%d/", server.getAddress().getHostName(), server.getAddress().getPort()
				))};
			}
		});
		assertThatThrownBy(() -> this.factory.create(new Version(3, 1, 1)).get())
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
