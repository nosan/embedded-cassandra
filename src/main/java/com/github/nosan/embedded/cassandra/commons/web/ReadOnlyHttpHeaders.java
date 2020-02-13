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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ReadOnlyHttpHeaders extends HttpHeaders {

	ReadOnlyHttpHeaders(HttpHeaders httpHeaders) {
		super(httpHeaders.headers);
	}

	@Override
	public void add(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> getFirst(String name) {
		return super.getFirst(name);
	}

	@Override
	public int size() {
		return super.size();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public boolean containsKey(Object name) {
		return super.containsKey(name);
	}

	@Override
	public boolean containsValue(Object value) {
		return super.containsValue(value);
	}

	@Override
	public List<String> put(String key, List<String> values) {
		throw new UnsupportedOperationException();

	}

	@Override
	public List<String> remove(Object key) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> headers) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public List<String> get(Object key) {
		return toUnmodifiable(super.get(key), Collections::unmodifiableList);
	}

	@Override
	public Set<String> keySet() {
		return toUnmodifiable(super.keySet(), Collections::unmodifiableSet);
	}

	@Override
	public Collection<List<String>> values() {
		return toUnmodifiable(super.values(), Collections::unmodifiableCollection);
	}

	@Override
	public Set<Entry<String, List<String>>> entrySet() {
		return Collections.unmodifiableSet(super.entrySet().stream()
				.map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(),
						toUnmodifiable(entry.getValue(), Collections::unmodifiableList)))
				.collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet)));
	}

	private static <C, IC extends C> C toUnmodifiable(C collection, Function<C, IC> constructor) {
		return (collection != null) ? constructor.apply(collection) : null;
	}

}
