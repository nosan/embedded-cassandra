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

package com.github.nosan.embedded.cassandra.commons.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class ReadOnlyHttpHeadersTests {

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
	void add() {
		HttpHeaders httpHeaders = HttpHeaders.readOnly(new LinkedHashMap<>());
		assertThatThrownBy(() -> httpHeaders.add("Content-Type", "application/xml"))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void set() {
		HttpHeaders httpHeaders = HttpHeaders.readOnly(new LinkedHashMap<>());
		assertThatThrownBy(() -> httpHeaders.set("Content-Type", "application/xml"))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void getFirst() {
		assertThat(getHeaders().getFirst("Content-Type")).hasValue("application/yaml");
	}

	@Test
	void keySet() {
		assertThat(getHeaders().keySet()).containsExactly("Accept", "Content-Type");
	}

	@Test
	void values() {
		assertThat(getHeaders().values()).contains(Arrays.asList("application/yaml", "application/xml"),
				Collections.singletonList("application/json"));
	}

	@Test
	void entrySet() {
		assertThat(getHeaders().entrySet())
				.contains(entry("Accept", Collections.singletonList("application/json")),
						entry("Content-Type", Arrays.asList("application/yaml", "application/xml")));
	}

	@Test
	void size() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.size()).isEqualTo(2);
	}

	@Test
	void isEmpty() {
		assertThat(HttpHeaders.readOnly(new LinkedHashMap<>()).isEmpty()).isTrue();
		assertThat(getHeaders().isEmpty()).isFalse();
	}

	@Test
	void containsKey() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.containsKey("Content-Type")).isTrue();
		assertThat(httpHeaders.containsKey("content-type")).isTrue();
		assertThat(httpHeaders.containsKey("content-Type")).isTrue();
		assertThat(httpHeaders.containsKey("ConTent-Type")).isTrue();
		assertThat(httpHeaders.containsKey("Key2")).isFalse();
	}

	@Test
	void containsValue() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.containsValue(Collections.singletonList("application/json"))).isTrue();
	}

	@Test
	void get() {
		HttpHeaders httpHeaders = getHeaders();
		assertThat(httpHeaders.get("Content-Type")).containsExactly("application/yaml", "application/xml");
		assertThat(httpHeaders.get("Accept")).containsExactly("application/json");
	}

	@Test
	void put() {
		HttpHeaders httpHeaders = getHeaders();
		assertThatThrownBy(() -> httpHeaders.put("Content-Type", Collections.singletonList("q")))
				.isInstanceOf(UnsupportedOperationException.class);

	}

	@Test
	void remove() {
		HttpHeaders httpHeaders = getHeaders();
		assertThatThrownBy(() -> httpHeaders.remove("Content-Type")).isInstanceOf(UnsupportedOperationException.class);

	}

	@Test
	void putAll() {
		HttpHeaders httpHeaders = getHeaders();
		assertThatThrownBy(() -> httpHeaders.putAll(new LinkedHashMap<>()))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void clear() {
		HttpHeaders httpHeaders = getHeaders();
		assertThatThrownBy(httpHeaders::clear)
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void testString() {
		assertThat(getHeaders().toString()).contains("Content-Type",
				"Accept", "application/xml", "application/yaml", "application/json");
	}

	private HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/yaml");
		httpHeaders.add("content-type", "application/xml");
		httpHeaders.add("Accept", "application/json");
		return HttpHeaders.readOnly(httpHeaders);
	}

}
