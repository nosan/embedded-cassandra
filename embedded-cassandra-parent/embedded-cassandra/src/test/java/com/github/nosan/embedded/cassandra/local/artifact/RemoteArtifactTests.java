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

package com.github.nosan.embedded.cassandra.local.artifact;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.github.nosan.embedded.cassandra.Version;
import com.github.nosan.embedded.cassandra.test.support.CaptureOutput;
import com.github.nosan.embedded.cassandra.test.support.WebServer;
import com.github.nosan.embedded.cassandra.util.OS;

import static org.assertj.core.api.Assertions.assertThat;

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

	@Rule
	public final ExpectedException throwable = ExpectedException.none();

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
	public void shouldNotWorkDirectoryIsFile() throws Exception {
		this.throwable.expect(IllegalArgumentException.class);
		this.throwable.expectMessage("exists and is a file");
		this.factory.setDirectory(this.temporaryFolder.newFile().toPath());
		this.factory.create(new Version(3, 11, 3)).get();
	}


	@Test
	public void shouldNotWorkDirectoryNotWritable() throws Exception {
		if (!OS.isWindows()) {
			this.throwable.expect(IllegalArgumentException.class);
			this.throwable.expectMessage("is not writable");
			File file = this.temporaryFolder.newFolder();
			assertThat(file.setReadOnly()).describedAs("setReadOnly").isTrue();
			this.factory.setDirectory(file.toPath());
			this.factory.create(new Version(3, 11, 3)).get();
		}
	}

	@Test
	public void shouldDownloadArtifactIfNotExistsShowProgress() throws Exception {
		HttpServer server = this.webServer.get();
		byte[] content;
		try (InputStream inputStream = getClass().getResourceAsStream("/apache-cassandra-3.11.3.zip")) {
			content = IOUtils.toByteArray(inputStream);
		}
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, content.length);
			sleep(500);
			exchange.getResponseBody().write(content);
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
	public void shouldDownloadArtifactIfNotExistsNoProgress() throws Exception {
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
	public void shouldNotDownloadArtifactIfExists() throws Exception {
		this.factory.setDirectory(Paths.get(ClassLoader.getSystemResource("").toURI()));
		Artifact artifact = this.factory.create(new Version(3, 11, 3));
		Path archive = artifact.get();
		assertThat(this.output.toString()).doesNotContain("Downloaded");
		assertThat(this.output.toString()).doesNotContain("It takes a while...");
		assertThat(archive).exists().hasParent(this.factory.getDirectory());
		assertThat(archive).hasFileName("apache-cassandra-3.11.3.zip");
	}

	@Test
	public void readTimeoutIsExceeded() throws Exception {
		HttpServer server = this.webServer.get();
		server.createContext("/dist/apache-cassandra-3.1.1.zip", exchange -> {
			sleep(1200);
		});

		this.throwable.expect(SocketTimeoutException.class);
		this.throwable.expectMessage("Read timed out");

		this.factory.setReadTimeout(Duration.ofSeconds(1));
		this.factory.create(new Version(3, 1, 1)).get();
	}

	@Test
	public void connectTimeoutIsExceeded() throws Exception {
		this.factory.setUrlFactory(new UrlFactory() {
			@Nonnull
			@Override
			public URL[] create(@Nonnull Version version) throws MalformedURLException {
				return new URL[]{new URL("http://example.com:81/apache-cassandra-3.1.1.zip")};
			}
		});

		this.throwable.expect(SocketTimeoutException.class);
		this.throwable.expectMessage("connect timed out");

		this.factory.setConnectTimeout(Duration.ofSeconds(1));
		this.factory.create(new Version(3, 1, 1)).get();
	}

	@Test
	public void proxyIsInvalid() throws Exception {


		this.throwable.expect(SocketException.class);
		this.throwable.expectMessage("Connection refused");

		this.factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(1111)));
		this.factory.create(new Version(3, 1, 1)).get();

	}

	@Test
	public void urlDoesNotHaveFileName() throws Exception {
		this.throwable.expect(IllegalArgumentException.class);
		this.throwable.expectMessage("There is no way to determine");

		this.factory.setUrlFactory(new UrlFactory() {
			@Nonnull
			@Override
			public URL[] create(@Nonnull Version version) throws MalformedURLException {
				return new URL[]{new URL("http://localhost:8080/")};
			}
		});
		this.factory.create(new Version(3, 1, 1)).get();

	}

	private static void sleep(long timeout) {
		try {
			Thread.sleep(timeout);
		}
		catch (InterruptedException ex) {
			throw new IllegalStateException(ex);
		}

	}


}
