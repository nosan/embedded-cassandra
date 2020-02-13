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

package com.github.nosan.embedded.cassandra.commons.httpclient;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * A simple class which represents HTTP Request and Response headers.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class HttpHeaders implements Map<String, List<String>> {

	protected final HeaderValues headerValues;

	/**
	 * Create new empty {@link HttpHeaders}.
	 */
	public HttpHeaders() {
		this(new HeaderValues());
	}

	protected HttpHeaders(HeaderValues headerValues) {
		this.headerValues = Objects.requireNonNull(headerValues, "HeaderValues must not be null");
	}

	/**
	 * Creates read-only HTTP headers.
	 *
	 * @param headers the headers for which read-only view is to be returned.
	 * @return read only HTTP headers
	 */
	public static HttpHeaders readOnly(Map<String, List<String>> headers) {
		if (headers instanceof ReadOnlyHttpHeaders) {
			return ((ReadOnlyHttpHeaders) headers);
		}
		if (headers instanceof HttpHeaders) {
			return new ReadOnlyHttpHeaders(((HttpHeaders) headers));
		}
		return new ReadOnlyHttpHeaders(copyOf(headers));
	}

	public static HttpHeaders copyOf(Map<String, List<String>> headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if (headers != null) {
			headers.forEach((name, values) -> {
				if (name == null) {
					return;
				}
				if (values == null) {
					httpHeaders.put(name, new LinkedList<>());
				}
				else {
					httpHeaders.put(name, new LinkedList<>(values));
				}
			});
		}
		return httpHeaders;
	}

	/**
	 * Add a header value.
	 *
	 * @param name header name
	 * @param value header value
	 */
	public void add(String name, String value) {
		computeIfAbsent(name, (key) -> new LinkedList<>()).add(value);
	}

	/**
	 * Gets a first header value.
	 *
	 * @param name the header name
	 * @return the first value or empty
	 */
	public Optional<String> getFirst(Object name) {
		return getOrDefault(name, Collections.emptyList()).stream().findFirst();
	}

	@Override
	public int size() {
		return this.headerValues.size();
	}

	@Override
	public boolean isEmpty() {
		return this.headerValues.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.headerValues.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.headerValues.containsValue(value);
	}

	@Override
	public List<String> get(Object key) {
		return this.headerValues.get(key);
	}

	@Override
	public List<String> put(String key, List<String> value) {
		return this.headerValues.put(key, value);
	}

	@Override
	public List<String> remove(Object key) {
		return this.headerValues.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> m) {
		this.headerValues.putAll(m);
	}

	@Override
	public void clear() {
		this.headerValues.clear();
	}

	@Override
	public Set<String> keySet() {
		return this.headerValues.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return this.headerValues.values();
	}

	@Override
	public Set<Entry<String, List<String>>> entrySet() {
		return this.headerValues.entrySet();
	}

	@Override
	public String toString() {
		return this.headerValues.toString();
	}

	protected static final class HeaderValues extends TreeMap<String, List<String>> {

		private static final Comparator<String> CASE_INSENSITIVE_COMPARATOR = Comparator
				.comparing(name -> name.toLowerCase(Locale.ENGLISH));

		public HeaderValues() {
			super(CASE_INSENSITIVE_COMPARATOR);
		}

	}

}
