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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link HttpHeaders}.
 *
 * @author Dmytro Nosan
 */
class HttpHeadersTests {

	@Test
	void testEquals() {
		assertThat(getHeaders()).isEqualTo(getHeaders());
		assertThat(getHeaders()).isNotEqualTo(new LinkedHashMap<>());
	}

	@Test
	void testHashCode() {
		assertThat(getHeaders()).hasSameHashCodeAs(getHeaders().hashCode());
	}

	@Test
	void keySet() {
		assertThat(getHeaders().keySet()).contains("content-type", "accept");
	}

	@Test
	void values() {
		assertThat(getHeaders().values())
				.contains(Arrays.asList("application/xml", "application/yaml"),
						Collections.singletonList("application/json"));
	}

	@Test
	void entrySet() {
		assertThat(getHeaders().entrySet())
				.contains(entry("Content-Type", Arrays.asList("application/xml", "application/yaml")),
						entry("Accept", Collections.singletonList("application/json")));
	}

	@Test
	void addAndGet() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.getFirst("content-type")).hasValue("application/xml");
		assertThat(httpHeaders.get("content-type")).containsExactly("application/xml", "application/yaml");
	}

	@Test
	void setAndGet() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.getFirst("content-type")).hasValue("application/xml");
		httpHeaders.set("content-type", "application/json");
		assertThat(httpHeaders.get("content-type")).containsExactly("application/json");
	}

	@Test
	void readonly() {
		HttpHeaders httpHeaders = getHeaders();
		HttpHeaders readOnly = HttpHeaders.readOnly(httpHeaders);
		assertThat(readOnly).containsAllEntriesOf(httpHeaders);
		assertThatThrownBy(readOnly::clear).isNotNull();
		assertThat(HttpHeaders.readOnly(readOnly)).isSameAs(readOnly);
	}

	@Test
	void copy() {
		HttpHeaders httpHeaders = getHeaders();
		HttpHeaders headers = HttpHeaders.copyOf(httpHeaders);
		assertThat(headers).containsAllEntriesOf(httpHeaders);
	}

	@Test
	void size() {
		HttpHeaders httpHeaders = getHeaders();
		httpHeaders.add("Accept", "application/json");
		assertThat(httpHeaders.size()).isEqualTo(2);
	}

	@Test
	void isEmpty() {
		HttpHeaders httpHeaders = new HttpHeaders();
		assertThat(httpHeaders.isEmpty()).isTrue();
		httpHeaders.add("content-type", "application/xml");
		httpHeaders.add("content-type", "application/yaml");
		httpHeaders.add("Accept", "application/json");
		assertThat(httpHeaders.isEmpty()).isFalse();
	}

	@Test
	void containsKey() {
		HttpHeaders httpHeaders = getHeaders();
		httpHeaders.add("Accept", "application/json");
		assertThat(httpHeaders.containsKey("Content-type")).isTrue();
		assertThat(httpHeaders.containsKey("contenT-type")).isTrue();
		assertThat(httpHeaders.containsKey("content-type")).isTrue();
		assertThat(httpHeaders.containsKey("content-Type")).isTrue();
		assertThat(httpHeaders.containsKey("Key2")).isFalse();
		assertThat(httpHeaders.containsKey(new StringBuilder())).isFalse();
	}

	@Test
	void containsValue() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.containsValue(Collections.singletonList("application/json"))).isTrue();
	}

	@Test
	void get() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.get("content-type")).containsExactly("application/xml", "application/yaml");
		assertThat(httpHeaders.get("Accept")).containsExactly("application/json");
		assertThat(httpHeaders.get(null)).isNull();
	}

	@Test
	void put() {
		HttpHeaders httpHeaders = getHeaders();
		httpHeaders.put("content-type", Collections.singletonList("q"));
		assertThat(httpHeaders.get("content-type")).containsExactly("q");
		assertThat(httpHeaders.get("Accept")).containsExactly("application/json");
	}

	@Test
	void remove() {
		HttpHeaders httpHeaders = getHeaders();
		httpHeaders.remove("content-type");
		httpHeaders.remove(null);
		assertThat(httpHeaders.get("content-type")).isNull();
		assertThat(httpHeaders.get("Accept")).containsExactly("application/json");
	}

	@Test
	void putAll() {
		HttpHeaders httpHeaders = getHeaders();
		HttpHeaders httpHeaders1 = new HttpHeaders();
		httpHeaders1.putAll(httpHeaders);
		assertThat(httpHeaders.get("content-type")).containsExactly("application/xml", "application/yaml");
		assertThat(httpHeaders1.get("Accept")).containsExactly("application/json");
	}

	@Test
	void clear() {
		HttpHeaders httpHeaders = getHeaders();
		httpHeaders.add("Accept", "application/json");
		httpHeaders.clear();
		assertThat(httpHeaders).isEmpty();
	}

	@Test
	void testString() {
		assertThat(getHeaders().toString()).contains("Content-Type",
				"Accept", "application/xml", "application/yaml", "application/json");
	}

	private HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.put("Content-Type", new LinkedList<>(Arrays.asList("application/xml", "application/yaml")));
		httpHeaders.add("Accept", "application/json");
		return httpHeaders;
	}

}
