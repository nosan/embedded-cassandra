/*
 * Copyright 2020-2024 the original author or authors.
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

import java.net.URI;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HttpRequest}.
 *
 * @author Dmytro Nosan
 */
class HttpRequestTests {

	@Test
	void construct() {
		URI uri = URI.create("http://localhost");
		HttpRequest httpRequest = new HttpRequest(uri);
		assertThat(httpRequest.getUri()).isEqualTo(uri);
		assertThat(httpRequest.getMethod()).isEqualTo("GET");
		assertThat(httpRequest.getHeaders()).isEmpty();
		assertThat(httpRequest.toString()).contains("GET").contains("localhost");
	}

	@Test
	void construct2() {
		URI uri = URI.create("http://localhost");
		HttpRequest httpRequest = new HttpRequest(uri, "POST");
		assertThat(httpRequest.getUri()).isEqualTo(uri);
		assertThat(httpRequest.getMethod()).isEqualTo("POST");
		assertThat(httpRequest.getHeaders()).isEmpty();
		assertThat(httpRequest.toString()).contains("POST").contains("localhost");
	}

	@Test
	void construct3() {
		URI uri = URI.create("http://localhost");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		String method = "POST";
		HttpRequest httpRequest = new HttpRequest(uri, method, headers);
		assertThat(httpRequest.getUri()).isEqualTo(uri);
		assertThat(httpRequest.getMethod()).isEqualTo(method);
		assertThat(httpRequest.getHeaders()).containsAllEntriesOf(headers);
		assertThat(httpRequest.toString()).contains("POST").contains("localhost");
	}

}
