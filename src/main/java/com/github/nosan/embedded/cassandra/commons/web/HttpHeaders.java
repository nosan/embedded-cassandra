/*
 * Copyright 2020-2025 the original author or authors.
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
 * Represents HTTP request and response headers in a case-insensitive manner.
 *
 * @author Dmytro Nosan
 * @since 4.0.0
 */
public class HttpHeaders implements Map<String, List<String>> {

	protected final HeaderValues headers;

	/**
	 * Creates a new empty {@link HttpHeaders} instance.
	 */
	public HttpHeaders() {
		this(new HeaderValues());
	}

	/**
	 * Creates an {@link HttpHeaders} instance with the specified header values.
	 *
	 * @param headers The initial header values
	 */
	protected HttpHeaders(HeaderValues headers) {
		this.headers = headers;
	}

	/**
	 * Creates a read-only view of the specified HTTP headers.
	 *
	 * @param headers The headers to create a read-only view for
	 * @return A read-only {@link HttpHeaders} instance
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
	 * Creates an {@link HttpHeaders} instance by copying the provided headers.
	 *
	 * @param headers The headers to copy
	 * @return A new {@link HttpHeaders} instance
	 * @throws NullPointerException If {@code headers} is {@code null}
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
	 * Retrieves the first value associated with the given header name.
	 *
	 * @param name The header name
	 * @return An {@link Optional} containing the first header value, or {@link Optional#empty()} if no value exists
	 */
	public Optional<String> getFirst(String name) {
		return getOrDefault(name, Collections.emptyList()).stream().findFirst();
	}

	@Override
	public List<String> put(String name, List<String> values) {
		return this.headers.put(name, values);
	}

	/**
	 * Adds a value to the list of values for the specified header name. If the header does not already exist, it will
	 * be created.
	 *
	 * @param name The header name
	 * @param value The header value to add
	 */
	public void add(String name, String value) {
		computeIfAbsent(name, (k) -> new LinkedList<>()).add(value);
	}

	/**
	 * Sets a single value for the specified header name. This replaces any existing values for the header.
	 *
	 * @param name The header name
	 * @param value The header value to set
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

	/**
	 * A case-insensitive map for storing HTTP header names and values.
	 *
	 * <p>This implementation ensures that header names are treated as case-insensitive,
	 * following the HTTP specification.</p>
	 */
	protected static final class HeaderValues extends TreeMap<String, List<String>> {

		private static final Comparator<String> CASE_INSENSITIVE_COMPARATOR = Comparator
				.comparing(name -> name.toLowerCase(Locale.ENGLISH));

		/**
		 * Creates a new instance with a case-insensitive comparator.
		 */
		public HeaderValues() {
			super(CASE_INSENSITIVE_COMPARATOR);
		}

	}

}
