/*
 * Copyright 2020 the original author or authors.
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

package com.github.nosan.embedded.cassandra.commons;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UrlResource}.
 *
 * @author Dmytro Nosan
 */
class UrlResourceTests {

	private static UrlResource resource;

	private static URL url;

	private static HttpServer httpServer;

	@BeforeAll
	static void beforeAll() throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(0), 0);
		httpServer.createContext("/api", exchange -> exchange.sendResponseHeaders(200, 0));
		httpServer.setExecutor(Executors.newCachedThreadPool());
		httpServer.start();
		URI uri = getBaseUri();
		resource = new UrlResource(uri.toURL());
		url = uri.toURL();
	}

	@AfterAll
	static void afterAll() {
		HttpServer server = httpServer;
		if (server != null) {
			server.stop(0);
			((ExecutorService) server.getExecutor()).shutdown();
		}
	}

	@Test
	void getInputStream() throws IOException {
		assertThat(resource.isReadable()).isTrue();
		resource.getInputStream().close();
	}

	@Test
	void getOutputStream() throws Exception {
		assertThat(resource.isWritable()).isTrue();
		resource.getOutputStream().close();
	}

	@Test
	void toURI() throws URISyntaxException, IOException {
		assertThat(resource.toURI()).isEqualTo(url.toURI());
	}

	@Test
	void toURL() {
		assertThat(resource.toURL()).isEqualTo(url);
	}

	@Test
	void getFileName() {
		assertThat(resource.getFileName()).hasValue("api");
	}

	@Test
	void exists() {
		assertThat(resource.exists()).isTrue();
	}

	@Test
	void testEquals() throws MalformedURLException {
		assertThat(resource.equals(resource)).isTrue();
		assertThat(resource.equals(null)).isFalse();
		assertThat(resource.equals(new ClassPathResource("1.txt"))).isFalse();
		assertThat(resource.equals(new UrlResource(url))).isTrue();
		assertThat(resource.equals(new UrlResource(new URL("http://localhost:8080")))).isFalse();
	}

	@Test
	void testHashCode() {
		assertThat(resource).hasSameHashCodeAs(resource);
	}

	@Test
	void testToString() {
		assertThat(resource.toString()).contains(getBaseUri().toString());
	}

	@Test
	void getFileNameFromLongUrl() throws MalformedURLException {
		assertThat(new UrlResource(new URL("http://localhost:8080/v1/api/file.txt")).getFileName())
				.hasValue("file.txt");
	}

	@Test
	void emptyFileName() throws MalformedURLException {
		assertThat(new UrlResource(new URL("http://localhost:8080")).getFileName()).isEmpty();
	}

	private static URI getBaseUri() {
		InetSocketAddress address = httpServer.getAddress();
		if (address.getAddress() instanceof Inet6Address) {
			return URI.create(String.format("http://[%s]:%d/api", address.getHostName(), address.getPort()));
		}
		return URI.create(String.format("http://%s:%d/api", address.getHostName(), address.getPort()));
	}

}
