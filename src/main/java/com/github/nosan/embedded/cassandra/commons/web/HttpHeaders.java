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
 * A simple class that represents HTTP Request and Response headers.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class HttpHeaders implements Map<String, List<String>> {

	protected final HeaderValues headers;

	/**
	 * Create new empty {@link HttpHeaders}.
	 */
	public HttpHeaders() {
		this(new HeaderValues());
	}

	/**
	 * Creates a {@link HttpHeaders} with the provided headers.
	 */
	protected HttpHeaders(HeaderValues headers) {
		this.headers = headers;
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

	/**
	 * Creates HTTP headers.
	 *
	 * @param headers the headers to copy from
	 * @return HTTP headers
	 */
	public static HttpHeaders copyOf(Map<String, List<String>> headers) {
		Objects.requireNonNull(headers, "Headers must not be null");
		HttpHeaders httpHeaders = new HttpHeaders();
		headers.forEach((name, values) -> {
			if (name == null) {
				return;
			}
			httpHeaders.put(name, Optional.ofNullable(values).map(LinkedList::new).orElse(null));
		});
		return httpHeaders;
	}

	@Override
	public int size() {
		return this.headers.size();
	}

	@Override
	public boolean isEmpty() {
		return this.headers.isEmpty();
	}

	@Override
	public boolean containsKey(Object name) {
		if (!(name instanceof String)) {
			return false;
		}
		return this.headers.containsKey(name);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.headers.containsValue(value);
	}

	@Override
	public List<String> get(Object name) {
		if (!(name instanceof String)) {
			return null;
		}
		return this.headers.get(name);
	}

	/**
	 * Gets a first header value.
	 *
	 * @param name the header name
	 * @return the first string value associated with the name or empty
	 */
	public Optional<String> getFirst(String name) {
		return getOrDefault(name, Collections.emptyList()).stream().findFirst();
	}

	@Override
	public List<String> put(String name, List<String> values) {
		return this.headers.put(name, values);
	}

	/**
	 * Adds the given value to the list of headers for the given name. If the mapping does not already exist, then it is
	 * created.
	 *
	 * @param name the header name
	 * @param value the header value
	 */
	public void add(String name, String value) {
		computeIfAbsent(name, (k) -> new LinkedList<>()).add(value);
	}

	/**
	 * Sets the given value as the sole header value for the given name. If the mapping does not already exist, then it
	 * is created.
	 *
	 * @param name the header name
	 * @param value the header value to set.
	 */
	public void set(String name, String value) {
		List<String> values = new LinkedList<>();
		values.add(value);
		put(name, values);
	}

	@Override
	public List<String> remove(Object name) {
		if (!(name instanceof String)) {
			return null;
		}
		return this.headers.remove(name);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> headers) {
		headers.forEach(this::put);
	}

	@Override
	public void clear() {
		this.headers.clear();
	}

	@Override
	public Set<String> keySet() {
		return this.headers.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return this.headers.values();
	}

	@Override
	public Set<Map.Entry<String, List<String>>> entrySet() {
		return this.headers.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		return this.headers.equals(o);
	}

	@Override
	public int hashCode() {
		return this.headers.hashCode();
	}

	@Override
	public String toString() {
		return this.headers.toString();
	}

	protected static final class HeaderValues extends TreeMap<String, List<String>> {

		private static final Comparator<String> CASE_INSENSITIVE_COMPARATOR = Comparator
				.comparing(name -> name.toLowerCase(Locale.ENGLISH));

		public HeaderValues() {
			super(CASE_INSENSITIVE_COMPARATOR);
		}

	}

}
