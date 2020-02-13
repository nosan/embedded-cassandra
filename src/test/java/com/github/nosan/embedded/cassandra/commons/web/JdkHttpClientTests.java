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

package com.github.nosan.embedded.cassandra.commons.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.nosan.embedded.cassandra.commons.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JdkHttpClient}.
 *
 * @author Dmytro Nosan
 */
class JdkHttpClientTests {

	private static HttpServer httpServer;

	private static JdkHttpClient httpClient;

	@BeforeAll
	static void beforeAll() throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(0), 0);
		httpServer.createContext("/api", exchange -> {
			exchange.getResponseHeaders().putAll(exchange.getRequestHeaders());
			exchange.getResponseHeaders().add("Http-Method", exchange.getRequestMethod());
			exchange.getResponseHeaders().add("URI", exchange.getRequestURI().toString());
			try (InputStream requestBody = exchange.getRequestBody();
					OutputStream responseBody = exchange.getResponseBody()) {
				byte[] bytes = StreamUtils.toByteArray(requestBody);
				exchange.sendResponseHeaders(200, bytes.length);
				StreamUtils.copy(new ByteArrayInputStream(bytes), responseBody);
			}
		});
		httpServer.createContext("/timeout", exchange -> {
			try {
				Thread.sleep(3000);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		});
		httpServer.setExecutor(Executors.newCachedThreadPool());
		httpServer.start();
		httpClient = new JdkHttpClient(Duration.ofSeconds(1), Duration.ofSeconds(1));
	}

	@AfterAll
	static void afterAll() {
		HttpServer httpServer = JdkHttpClientTests.httpServer;
		if (httpServer != null) {
			httpServer.stop(0);
			((ExecutorService) httpServer.getExecutor()).shutdown();
		}
	}

	@Test
	void GET() throws IOException {
		String httpMethod = "GET";
		URI uri = getBaseUri().resolve("/api");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		httpHeaders.set("Accept", null);
		HttpResponse response = httpClient.send(new HttpRequest(uri, httpMethod, httpHeaders));
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getHeaders())
				.containsEntry("Content-Type", Collections.singletonList("application/json"))
				.containsEntry("Accept", Collections.singletonList(""));
		assertThat(response.getHeaders().getFirst("Http-Method")).hasValue(httpMethod);
		assertThat(response.getUri()).isEqualTo(uri);
		assertThat(response.toString()).contains("200 OK");
	}

	@Test
	void GET_QUERY() throws IOException {
		String httpMethod = "GET";
		URI uri = getBaseUri().resolve("/api?name=test");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		httpHeaders.add("Accept", null);
		HttpResponse response = httpClient.send(new HttpRequest(uri, httpMethod, httpHeaders));
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getHeaders())
				.containsEntry("Content-Type", Collections.singletonList("application/json"))
				.containsEntry("Accept", Collections.singletonList(""));
		assertThat(response.getHeaders().getFirst("Http-Method")).hasValue(httpMethod);
		assertThat(response.getUri()).isEqualTo(uri);
		assertThat(response.getHeaders().getFirst("URI")).hasValue("/api?name=test");
		assertThat(response.toString()).contains("200 OK");
	}

	@Test
	void POST() throws IOException {
		String body = "Text";
		String httpMethod = "POST";
		URI uri = getBaseUri().resolve("/api");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpResponse response = httpClient.send(new HttpRequest(uri, httpMethod, httpHeaders),
				HttpClient.BodySuppliers.ofString(body));
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getHeaders()).containsAllEntriesOf(httpHeaders);
		assertThat(response.getHeaders().getFirst("Http-Method")).hasValue(httpMethod);
		assertThat(response.getUri()).isEqualTo(uri);
		assertThat(response.toString()).contains("200 OK");
		assertThat(read(response)).isEqualTo(body);
	}

	@Test
	void DELETE() throws IOException {
		String body = "Text";
		String httpMethod = "DELETE";
		URI uri = getBaseUri().resolve("/api");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpResponse response = httpClient.send(new HttpRequest(uri, httpMethod, httpHeaders),
				HttpClient.BodySuppliers.ofString(body));
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getHeaders()).containsAllEntriesOf(httpHeaders);
		assertThat(response.getHeaders().getFirst("Http-Method")).hasValue(httpMethod);
		assertThat(response.getUri()).isEqualTo(uri);
		assertThat(response.toString()).contains("200 OK");
		assertThat(read(response)).isEqualTo(body);
	}

	@Test
	void PUT() throws IOException {
		String body = "Text";
		String httpMethod = "PUT";
		URI uri = getBaseUri().resolve("/api");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		HttpResponse response = httpClient.send(new HttpRequest(uri, httpMethod, httpHeaders),
				HttpClient.BodySuppliers.ofString(body));
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getHeaders()).containsAllEntriesOf(httpHeaders);
		assertThat(response.getHeaders().getFirst("Http-Method")).hasValue(httpMethod);
		assertThat(response.getUri()).isEqualTo(uri);
		assertThat(response.toString()).contains("200 OK");
		assertThat(read(response)).isEqualTo(body);
	}

	@Test
	void NOT_FOUND() throws IOException {
		URI uri = getBaseUri().resolve("/not_found");
		HttpResponse response = httpClient.send(new HttpRequest(uri));
		assertThat(response.getHeaders()).isNotEmpty();
		assertThat(response.getUri()).isEqualTo(uri);
		assertThat(response.toString()).contains("404 Not Found");
		assertThat(read(response)).contains("No context found for request");
	}

	@Test
	void READ_TIMEOUT() throws IOException {
		URI uri = getBaseUri().resolve("/timeout");
		HttpResponse response = httpClient.send(new HttpRequest(uri));
		assertThatThrownBy(() -> read(response)).hasMessage("Read timed out");
	}

	@Test
	void CONNECT_TIMEOUT() {
		assertThatThrownBy(() -> httpClient.send(new HttpRequest(URI.create("http://example.com:81/"))))
				.hasStackTraceContaining("connect timed out");
	}

	@Test
	void construct1() {
		JdkHttpClient jdkHttpClient = new JdkHttpClient();
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("readTimeout", null);
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("connectTimeout", null);
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("proxy", null);
	}

	@Test
	void construct2() {
		JdkHttpClient jdkHttpClient = new JdkHttpClient(Proxy.NO_PROXY);
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("readTimeout", null);
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("connectTimeout", null);
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("proxy", Proxy.NO_PROXY);
	}

	@Test
	void construct3() {
		JdkHttpClient jdkHttpClient = new JdkHttpClient(Duration.ofSeconds(30), Duration.ofSeconds(10));
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("readTimeout", Duration.ofSeconds(10));
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("connectTimeout", Duration.ofSeconds(30));
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("proxy", null);
	}

	@Test
	void construct4() {
		JdkHttpClient jdkHttpClient = new JdkHttpClient(Duration.ofSeconds(30), Duration.ofSeconds(10), Proxy.NO_PROXY);
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("readTimeout", Duration.ofSeconds(10));
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("connectTimeout", Duration.ofSeconds(30));
		assertThat(jdkHttpClient).hasFieldOrPropertyWithValue("proxy", Proxy.NO_PROXY);
	}

	@Test
	void invalidUrl() {
		URL resource = getClass().getResource("/test.txt");
		assertThatThrownBy(() -> new JdkHttpClient().send(new HttpRequest(resource.toURI())))
				.hasStackTraceContaining("HttpURLConnection required for");
	}

	private static String read(HttpResponse response) throws IOException {
		try (InputStream inputStream = response.getInputStream()) {
			return StreamUtils.toString(inputStream, Charset.defaultCharset());
		}
	}

	private static URI getBaseUri() {
		InetSocketAddress address = httpServer.getAddress();
		if (address.getAddress() instanceof Inet6Address) {
			return URI.create(String.format("http://[%s]:%d", address.getHostName(), address.getPort()));
		}
		return URI.create(String.format("http://%s:%d", address.getHostName(), address.getPort()));
	}

}
